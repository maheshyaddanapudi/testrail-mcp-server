package io.github.testrail.mcp.registry;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that discovers and catalogs all methods annotated with {@link InternalTool}.
 *
 * <p>At startup, this component scans all Spring-managed beans for methods carrying the
 * {@code @InternalTool} annotation. For each discovered method it builds a
 * {@link ToolDefinition} containing the tool's metadata (name, description, category,
 * keywords, examples) together with the reflective handles needed for invocation
 * (the owning bean instance, the {@link Method} reference, and parameter descriptors).</p>
 *
 * <p>The registry is the single source of truth for tool metadata and is consumed by
 * both the Lucene indexing service (for search) and the {@code execute_tool} MCP
 * callback (for invocation).</p>
 */
@Component
public class InternalToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(InternalToolRegistry.class);

    private final ApplicationContext applicationContext;
    private final Map<String, ToolDefinition> toolsByName = new ConcurrentHashMap<>();

    public InternalToolRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        scanForTools();
        log.info("InternalToolRegistry initialized with {} tools", toolsByName.size());
    }

    /**
     * Scans all Spring beans for methods annotated with {@link InternalTool} and registers
     * each one as a {@link ToolDefinition}.
     */
    private void scanForTools() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception e) {
                // Skip beans that cannot be instantiated (e.g., infrastructure beans)
                continue;
            }

            Class<?> beanClass = bean.getClass();
            // Handle Spring proxies by getting the actual target class
            if (beanClass.getName().contains("$$")) {
                beanClass = beanClass.getSuperclass();
            }

            for (Method method : beanClass.getDeclaredMethods()) {
                InternalTool annotation = method.getAnnotation(InternalTool.class);
                if (annotation == null) {
                    continue;
                }

                String toolName = annotation.name().isEmpty()
                        ? toSnakeCase(method.getName())
                        : annotation.name();

                List<ParameterDefinition> paramDefs = extractParameters(method);

                ToolDefinition definition = new ToolDefinition(
                        toolName,
                        annotation.description(),
                        annotation.category(),
                        Arrays.asList(annotation.examples()),
                        Arrays.asList(annotation.keywords()),
                        bean,
                        method,
                        paramDefs
                );

                if (toolsByName.containsKey(toolName)) {
                    log.warn("Duplicate tool name '{}' found in {} and {}. Keeping first registration.",
                            toolName,
                            toolsByName.get(toolName).getMethod().getDeclaringClass().getSimpleName(),
                            method.getDeclaringClass().getSimpleName());
                } else {
                    toolsByName.put(toolName, definition);
                    log.debug("Registered tool: {} (category: {}, params: {})",
                            toolName, annotation.category(), paramDefs.size());
                }
            }
        }
    }

    /**
     * Extracts parameter metadata from a method's parameters using {@link InternalToolParam}
     * annotations and Java reflection.
     */
    private List<ParameterDefinition> extractParameters(Method method) {
        List<ParameterDefinition> params = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            InternalToolParam paramAnnotation = param.getAnnotation(InternalToolParam.class);

            String description = paramAnnotation != null ? paramAnnotation.description() : "";
            boolean required = paramAnnotation == null || paramAnnotation.required();
            String defaultValue = paramAnnotation != null ? paramAnnotation.defaultValue() : "";

            params.add(new ParameterDefinition(
                    param.getName(),
                    param.getType(),
                    description,
                    required,
                    defaultValue
            ));
        }

        return params;
    }

    /**
     * Converts a camelCase method name to snake_case for tool naming.
     */
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Returns the tool definition for the given tool name, or {@code null} if not found.
     */
    public ToolDefinition getTool(String name) {
        return toolsByName.get(name);
    }

    /**
     * Returns an unmodifiable view of all registered tool definitions.
     */
    public Collection<ToolDefinition> getAllTools() {
        return Collections.unmodifiableCollection(toolsByName.values());
    }

    /**
     * Returns the total number of registered tools.
     */
    public int getToolCount() {
        return toolsByName.size();
    }

    /**
     * Checks whether a tool with the given name is registered.
     */
    public boolean hasTool(String name) {
        return toolsByName.containsKey(name);
    }

    /**
     * Returns all registered tool names.
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(toolsByName.keySet());
    }

    // ── Inner classes ───────────────────────────────────────────────────────

    /**
     * Immutable descriptor for a single registered tool, combining annotation metadata
     * with the reflective handles required for invocation.
     */
    public static class ToolDefinition {
        private final String name;
        private final String description;
        private final String category;
        private final List<String> examples;
        private final List<String> keywords;
        private final Object bean;
        private final Method method;
        private final List<ParameterDefinition> parameters;

        public ToolDefinition(String name, String description, String category,
                              List<String> examples, List<String> keywords,
                              Object bean, Method method, List<ParameterDefinition> parameters) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.examples = Collections.unmodifiableList(examples);
            this.keywords = Collections.unmodifiableList(keywords);
            this.bean = bean;
            this.method = method;
            this.parameters = Collections.unmodifiableList(parameters);
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public List<String> getExamples() { return examples; }
        public List<String> getKeywords() { return keywords; }
        public Object getBean() { return bean; }
        public Method getMethod() { return method; }
        public List<ParameterDefinition> getParameters() { return parameters; }

        /**
         * Converts this tool definition to a Map containing all annotation metadata,
         * suitable for serialization and returning to the LLM via search_tools.
         * Includes name, description, category, keywords, examples, and full parameter details.
         */
        public Map<String, Object> toFullDetails() {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("name", name);
            details.put("description", description);
            details.put("category", category);
            details.put("keywords", keywords);
            details.put("examples", examples);

            List<Map<String, Object>> paramList = new ArrayList<>();
            for (ParameterDefinition p : parameters) {
                Map<String, Object> paramMap = new LinkedHashMap<>();
                paramMap.put("name", p.getName());
                paramMap.put("type", p.getType().getSimpleName());
                paramMap.put("description", p.getDescription());
                paramMap.put("required", p.isRequired());
                if (!p.getDefaultValue().isEmpty()) {
                    paramMap.put("defaultValue", p.getDefaultValue());
                }
                paramList.add(paramMap);
            }
            details.put("parameters", paramList);

            return details;
        }
    }

    /**
     * Immutable descriptor for a single tool parameter.
     */
    public static class ParameterDefinition {
        private final String name;
        private final Class<?> type;
        private final String description;
        private final boolean required;
        private final String defaultValue;

        public ParameterDefinition(String name, Class<?> type, String description,
                                   boolean required, String defaultValue) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
            this.defaultValue = defaultValue;
        }

        public String getName() { return name; }
        public Class<?> getType() { return type; }
        public String getDescription() { return description; }
        public boolean isRequired() { return required; }
        public String getDefaultValue() { return defaultValue; }
    }
}
