package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.registry.InternalToolRegistry;
import io.github.testrail.mcp.registry.LuceneToolIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The four tools exposed to MCP clients via Spring AI's {@code @Tool} auto-discovery.
 *
 * <p>All 101 internal TestRail tools are hidden behind these gateway methods:
 * <ul>
 *   <li>{@link #searchTools(String)} — semantic search via Lucene index, returns full tool details.</li>
 *   <li>{@link #getCategories()} — lists all tool categories with tool counts.</li>
 *   <li>{@link #getToolsByCategory(String)} — lists all tools in a specific category with full details.</li>
 *   <li>{@link #executeTool(String, Map)} — executes a specific tool by name with parameters.</li>
 * </ul>
 *
 * <p>Spring AI's MCP server auto-configuration discovers these {@code @Tool} methods
 * and exposes them as MCP tool callbacks. The internal tools annotated with
 * {@code @InternalTool} are invisible to Spring AI and only accessible through this class.</p>
 */
@Component
public class McpExposedTools {

    private static final Logger log = LoggerFactory.getLogger(McpExposedTools.class);

    private final InternalToolRegistry toolRegistry;
    private final LuceneToolIndexService luceneToolIndexService;
    private final ObjectMapper objectMapper;

    public McpExposedTools(InternalToolRegistry toolRegistry,
                           LuceneToolIndexService luceneToolIndexService,
                           ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.luceneToolIndexService = luceneToolIndexService;
        this.objectMapper = objectMapper;
    }

    // ── Discovery: Semantic Search ─────────────────────────────────────────

    /**
     * Searches for TestRail tools matching the given natural language query.
     * Returns full details for each matching tool including name, description,
     * category, keywords, examples, and parameter specifications.
     */
    @Tool(name = "search_tools", description = """
            Searches for available TestRail tools matching a natural language query.
            Returns a ranked list (max 20) of matching tools with full details including:
            - name: the tool identifier to use with execute_tool
            - description: what the tool does and when to use it
            - category: the tool's functional category
            - keywords: related search terms
            - examples: usage examples showing how to call via execute_tool
            - parameters: full parameter specifications (name, type, description, required, defaultValue)
            
            Use this for fuzzy or natural language queries like 'clone a test case' or 'add test result'.
            Alternatively, use get_categories() and get_tools_by_category() for structured browsing.
            After discovering the right tool, use execute_tool to run it.
            """)
    public String searchTools(
            @ToolParam(description = "Natural language search query describing what you want to do. Examples: 'add test result', 'get test cases for project', 'create milestone'")
            String query
    ) {
        log.info("search_tools called with query='{}'", query);

        if (query == null || query.isBlank()) {
            return toJson(Map.of("error", "Query must not be empty"));
        }

        List<LuceneToolIndexService.ToolSearchResult> searchResults = luceneToolIndexService.search(query, 20);

        if (searchResults.isEmpty()) {
            return toJson(Map.of("message", "No tools found matching query: " + query, "tools", List.of()));
        }

        List<Map<String, Object>> toolDetailsList = new ArrayList<>();
        for (LuceneToolIndexService.ToolSearchResult result : searchResults) {
            Map<String, Object> toolDetails = result.getToolDefinition().toFullDetails();
            toolDetails.put("relevanceScore", result.getScore());
            toolDetailsList.add(toolDetails);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("matchCount", toolDetailsList.size());
        response.put("tools", toolDetailsList);

        return toJson(response);
    }

    // ── Discovery: Category Browsing ───────────────────────────────────────

    /**
     * Returns all tool categories with the number of tools in each category.
     */
    @Tool(name = "get_categories", description = """
            Returns all available tool categories with the number of tools in each.
            Use this to explore what the TestRail MCP server can do, then call
            get_tools_by_category() to see the tools in a specific category.
            
            Example response:
            {"categories": [{"name": "test-cases", "toolCount": 6}, {"name": "test-runs", "toolCount": 6}, ...]}
            
            Alternatively, use search_tools() for natural language discovery.
            """)
    public String getCategories() {
        log.info("get_categories called");

        Map<String, List<InternalToolRegistry.ToolDefinition>> grouped = groupToolsByCategory();

        List<Map<String, Object>> categories = grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> cat = new LinkedHashMap<>();
                    cat.put("name", entry.getKey());
                    cat.put("toolCount", entry.getValue().size());
                    return cat;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalCategories", categories.size());
        response.put("totalTools", toolRegistry.getToolNames().size());
        response.put("categories", categories);

        return toJson(response);
    }

    /**
     * Returns all tools in a specific category with full details.
     */
    @Tool(name = "get_tools_by_category", description = """
            Returns all tools in a specific category with full details including:
            - name: the tool identifier to use with execute_tool
            - description: what the tool does
            - keywords: related search terms
            - examples: usage examples
            - parameters: full parameter specifications (name, type, description, required, defaultValue)
            
            Use get_categories() first to see available categories, then call this tool
            with a category name. After finding the right tool, use execute_tool to run it.
            
            Alternatively, use search_tools() for natural language discovery.
            """)
    public String getToolsByCategory(
            @ToolParam(description = "The category name exactly as returned by get_categories (e.g., 'test-cases', 'test-runs', 'projects')")
            String category
    ) {
        log.info("get_tools_by_category called with category='{}'", category);

        if (category == null || category.isBlank()) {
            return toJson(Map.of("error", "Category must not be empty"));
        }

        Map<String, List<InternalToolRegistry.ToolDefinition>> grouped = groupToolsByCategory();

        List<InternalToolRegistry.ToolDefinition> toolsInCategory = grouped.get(category);

        if (toolsInCategory == null) {
            List<String> validCategories = grouped.keySet().stream().sorted().collect(Collectors.toList());
            return toJson(Map.of(
                    "error", "Category not found: " + category,
                    "validCategories", validCategories
            ));
        }

        List<Map<String, Object>> toolDetailsList = toolsInCategory.stream()
                .sorted(Comparator.comparing(InternalToolRegistry.ToolDefinition::getName))
                .map(InternalToolRegistry.ToolDefinition::toFullDetails)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category);
        response.put("toolCount", toolDetailsList.size());
        response.put("tools", toolDetailsList);

        return toJson(response);
    }

    // ── Execution ──────────────────────────────────────────────────────────

    /**
     * Executes a TestRail tool by name with the provided parameters.
     */
    @Tool(name = "execute_tool", description = """
            Executes a specific TestRail tool by name with the provided parameters.
            Use search_tools, get_categories + get_tools_by_category to discover tool names
            and their required parameters before calling this tool.
            
            Parameters should be provided as a flat key-value map matching the parameter
            names returned by the discovery tools. For example:
            - execute_tool(toolName: "get_case", parameters: {"caseId": 123})
            - execute_tool(toolName: "add_result", parameters: {"testId": 5, "statusId": 1, "comment": "Passed"})
            - execute_tool(toolName: "get_cases", parameters: {"projectId": 1, "suiteId": 5})
            """)
    public String executeTool(
            @ToolParam(description = "The exact tool name as returned by search_tools or get_tools_by_category (e.g., 'get_case', 'add_result')")
            String toolName,
            @ToolParam(description = "A flat key-value map of parameters for the tool. Keys are parameter names, values are the parameter values.")
            Map<String, Object> parameters
    ) {
        log.info("execute_tool called with toolName='{}', parameters={}", toolName, parameters);

        if (toolName == null || toolName.isBlank()) {
            return toJson(Map.of("error", "Tool name must not be empty"));
        }

        InternalToolRegistry.ToolDefinition toolDef = toolRegistry.getTool(toolName);
        if (toolDef == null) {
            return toJson(Map.of(
                    "error", "Tool not found: " + toolName,
                    "suggestion", "Use search_tools or get_tools_by_category to find available tools"
            ));
        }

        if (parameters == null) {
            parameters = Collections.emptyMap();
        }

        try {
            Object[] args = buildMethodArguments(toolDef, parameters);
            Method method = toolDef.getMethod();
            method.setAccessible(true);
            Object result = method.invoke(toolDef.getBean(), args);

            return toJson(Map.of(
                    "tool", toolName,
                    "success", true,
                    "result", result != null ? result : "null"
            ));

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Tool '{}' execution failed: {}", toolName, cause.getMessage(), cause);
            return toJson(Map.of(
                    "tool", toolName,
                    "success", false,
                    "error", cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName()
            ));
        } catch (Exception e) {
            log.error("Failed to invoke tool '{}': {}", toolName, e.getMessage(), e);
            return toJson(Map.of(
                    "tool", toolName,
                    "success", false,
                    "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
            ));
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    /**
     * Groups all tools from the registry by their category.
     */
    Map<String, List<InternalToolRegistry.ToolDefinition>> groupToolsByCategory() {
        return toolRegistry.getAllTools().stream()
                .collect(Collectors.groupingBy(InternalToolRegistry.ToolDefinition::getCategory));
    }

    /**
     * Builds the method argument array by mapping the flat parameter map to the
     * tool's declared parameter definitions, performing type conversion as needed.
     */
    Object[] buildMethodArguments(InternalToolRegistry.ToolDefinition toolDef,
                                  Map<String, Object> parameters) {
        List<InternalToolRegistry.ParameterDefinition> paramDefs = toolDef.getParameters();
        Object[] args = new Object[paramDefs.size()];

        for (int i = 0; i < paramDefs.size(); i++) {
            InternalToolRegistry.ParameterDefinition paramDef = paramDefs.get(i);
            String paramName = paramDef.getName();
            Class<?> targetType = paramDef.getType();

            Object value = parameters.get(paramName);

            if (value == null && !paramDef.getDefaultValue().isEmpty()) {
                value = paramDef.getDefaultValue();
            }

            if (value == null) {
                if (paramDef.isRequired() && targetType.isPrimitive()) {
                    throw new IllegalArgumentException(
                            "Required parameter '" + paramName + "' is missing for tool '" + toolDef.getName() + "'");
                }
                args[i] = getDefaultPrimitiveValue(targetType);
            } else {
                args[i] = convertValue(value, targetType, paramName);
            }
        }

        return args;
    }

    /**
     * Converts a value from the parameter map to the expected Java type.
     * Handles common conversions: Number → int/long/Integer/Long, String → int/long,
     * Object → Map, etc.
     */
    Object convertValue(Object value, Class<?> targetType, String paramName) {
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Number conversions
        if (value instanceof Number number) {
            if (targetType == int.class || targetType == Integer.class) {
                return number.intValue();
            }
            if (targetType == long.class || targetType == Long.class) {
                return number.longValue();
            }
            if (targetType == double.class || targetType == Double.class) {
                return number.doubleValue();
            }
            if (targetType == float.class || targetType == Float.class) {
                return number.floatValue();
            }
            if (targetType == boolean.class || targetType == Boolean.class) {
                return number.intValue() != 0;
            }
            if (targetType == String.class) {
                return number.toString();
            }
        }

        // String to number conversions
        if (value instanceof String str) {
            try {
                if (targetType == int.class || targetType == Integer.class) {
                    return Integer.parseInt(str);
                }
                if (targetType == long.class || targetType == Long.class) {
                    return Long.parseLong(str);
                }
                if (targetType == double.class || targetType == Double.class) {
                    return Double.parseDouble(str);
                }
                if (targetType == float.class || targetType == Float.class) {
                    return Float.parseFloat(str);
                }
                if (targetType == boolean.class || targetType == Boolean.class) {
                    return Boolean.parseBoolean(str);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Cannot convert parameter '" + paramName + "' value '" + str + "' to " + targetType.getSimpleName());
            }
        }

        // Boolean conversions
        if (value instanceof Boolean bool) {
            if (targetType == boolean.class || targetType == Boolean.class) {
                return bool;
            }
            if (targetType == String.class) {
                return bool.toString();
            }
        }

        // Map stays as Map (for complex parameters like request bodies)
        if (value instanceof Map && Map.class.isAssignableFrom(targetType)) {
            return value;
        }

        // Fallback: try ObjectMapper conversion for complex types
        try {
            return objectMapper.convertValue(value, targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot convert parameter '" + paramName + "' from " + value.getClass().getSimpleName()
                            + " to " + targetType.getSimpleName());
        }
    }

    /**
     * Returns the default value for primitive types (0 for numbers, false for boolean).
     * Returns null for reference types.
     */
    Object getDefaultPrimitiveValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        return null;
    }

    /**
     * Serializes an object to a JSON string. Falls back to toString() on error.
     */
    String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON, falling back to toString()", e);
            return obj.toString();
        }
    }
}
