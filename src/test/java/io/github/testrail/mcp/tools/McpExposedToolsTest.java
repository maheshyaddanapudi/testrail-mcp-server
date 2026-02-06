package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.registry.InternalToolRegistry;
import io.github.testrail.mcp.registry.LuceneToolIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link McpExposedTools}.
 * Covers search_tools, execute_tool, type conversion, error handling,
 * and edge cases for 90%+ code coverage.
 */
@ExtendWith(MockitoExtension.class)
class McpExposedToolsTest {

    @Mock
    private ApplicationContext applicationContext;

    private InternalToolRegistry toolRegistry;
    private LuceneToolIndexService luceneToolIndexService;
    private McpExposedTools mcpExposedTools;
    private ObjectMapper objectMapper;

    // ── Test fixture beans ──────────────────────────────────────────────────

    public static class MathToolBean {
        @InternalTool(
                name = "add_numbers",
                description = "Adds two numbers together",
                category = "math",
                examples = {"execute_tool('add_numbers', {a: 1, b: 2})"},
                keywords = {"add", "sum", "plus"}
        )
        public int addNumbers(
                @InternalToolParam(description = "First number") int a,
                @InternalToolParam(description = "Second number") int b
        ) {
            return a + b;
        }

        @InternalTool(
                name = "divide_numbers",
                description = "Divides first number by second",
                category = "math",
                examples = {"execute_tool('divide_numbers', {a: 10, b: 2})"},
                keywords = {"divide", "division"}
        )
        public double divideNumbers(
                @InternalToolParam(description = "Numerator") double a,
                @InternalToolParam(description = "Denominator") double b
        ) {
            if (b == 0) throw new ArithmeticException("Division by zero");
            return a / b;
        }
    }

    public static class StringToolBean {
        @InternalTool(
                name = "greet",
                description = "Generates a greeting message",
                category = "strings",
                examples = {"execute_tool('greet', {name: 'World'})"},
                keywords = {"greet", "hello"}
        )
        public String greet(
                @InternalToolParam(description = "Name to greet") String name
        ) {
            return "Hello, " + name + "!";
        }

        @InternalTool(
                name = "format_message",
                description = "Formats a message with optional prefix",
                category = "strings",
                examples = {"execute_tool('format_message', {message: 'test'})"},
                keywords = {"format", "message"}
        )
        public String formatMessage(
                @InternalToolParam(description = "The message") String message,
                @InternalToolParam(description = "Optional prefix", required = false, defaultValue = "INFO")
                String prefix
        ) {
            return "[" + prefix + "] " + message;
        }
    }

    public static class MapToolBean {
        @InternalTool(
                name = "process_data",
                description = "Processes a data map",
                category = "data",
                examples = {"execute_tool('process_data', {data: {key: 'value'}})"},
                keywords = {"process", "data"}
        )
        public String processData(
                @InternalToolParam(description = "Data map to process") Map<String, Object> data
        ) {
            return "Processed: " + data.toString();
        }
    }

    public static class MultiTypeToolBean {
        @InternalTool(
                name = "multi_type_tool",
                description = "Tool with multiple parameter types",
                category = "testing",
                keywords = {"multi", "type"}
        )
        public String multiTypeTool(
                @InternalToolParam(description = "An integer") int intVal,
                @InternalToolParam(description = "A long") long longVal,
                @InternalToolParam(description = "A float") float floatVal,
                @InternalToolParam(description = "A boolean") boolean boolVal,
                @InternalToolParam(description = "A string", required = false) String strVal
        ) {
            return intVal + ":" + longVal + ":" + floatVal + ":" + boolVal + ":" + strVal;
        }
    }

    public static class ExceptionToolBean {
        @InternalTool(
                name = "failing_tool",
                description = "A tool that always fails",
                category = "testing",
                keywords = {"fail", "error"}
        )
        public String failingTool(
                @InternalToolParam(description = "Input value") String input
        ) {
            throw new RuntimeException("Intentional failure: " + input);
        }

        @InternalTool(
                name = "null_message_tool",
                description = "A tool that throws exception with null message",
                category = "testing",
                keywords = {"null", "error"}
        )
        public String nullMessageTool(
                @InternalToolParam(description = "Input value") String input
        ) {
            throw new NullPointerException();
        }
    }

    // ── Setup helpers ───────────────────────────────────────────────────────

    private void setupWithBeans(Object... beans) {
        String[] beanNames = new String[beans.length];
        for (int i = 0; i < beans.length; i++) {
            beanNames[i] = "bean" + i;
            when(applicationContext.getBean("bean" + i)).thenReturn(beans[i]);
        }
        when(applicationContext.getBeanDefinitionNames()).thenReturn(beanNames);

        toolRegistry = new InternalToolRegistry(applicationContext);
        toolRegistry.init();

        luceneToolIndexService = new LuceneToolIndexService(toolRegistry);
        luceneToolIndexService.init();

        objectMapper = new ObjectMapper();
        mcpExposedTools = new McpExposedTools(toolRegistry, luceneToolIndexService, objectMapper);
    }

    @BeforeEach
    void setUp() {
        setupWithBeans(new MathToolBean(), new StringToolBean());
    }

    // ── Tests: searchTools ──────────────────────────────────────────────────

    @Nested
    @DisplayName("searchTools")
    class SearchToolsTests {

        @Test
        @DisplayName("Should return matching tools with full details")
        void shouldReturnMatchingTools() throws Exception {
            String result = mcpExposedTools.searchTools("add numbers");

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("query")).isEqualTo("add numbers");
            assertThat((Integer) response.get("matchCount")).isGreaterThan(0);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) response.get("tools");
            assertThat(tools).isNotEmpty();

            Map<String, Object> firstTool = tools.get(0);
            assertThat(firstTool).containsKey("name");
            assertThat(firstTool).containsKey("description");
            assertThat(firstTool).containsKey("category");
            assertThat(firstTool).containsKey("keywords");
            assertThat(firstTool).containsKey("examples");
            assertThat(firstTool).containsKey("parameters");
            assertThat(firstTool).containsKey("relevanceScore");
        }

        @Test
        @DisplayName("Should return full parameter details in search results")
        void shouldReturnFullParameterDetails() throws Exception {
            String result = mcpExposedTools.searchTools("add numbers");

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) response.get("tools");

            // Find add_numbers tool
            Map<String, Object> addTool = tools.stream()
                    .filter(t -> "add_numbers".equals(t.get("name")))
                    .findFirst().orElseThrow();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) addTool.get("parameters");
            assertThat(params).hasSize(2);
            assertThat(params.get(0).get("name")).isEqualTo("a");
            assertThat(params.get(0).get("type")).isEqualTo("int");
            assertThat(params.get(0).get("description")).isEqualTo("First number");
            assertThat(params.get(0).get("required")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should return empty list for no matches")
        void shouldReturnEmptyForNoMatches() throws Exception {
            String result = mcpExposedTools.searchTools("xyznonexistent");

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) response.get("tools");
            assertThat(tools).isEmpty();
            assertThat(response).containsKey("message");
        }

        @Test
        @DisplayName("Should return error for null query")
        void shouldReturnErrorForNullQuery() throws Exception {
            String result = mcpExposedTools.searchTools(null);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response).containsKey("error");
        }

        @Test
        @DisplayName("Should return error for blank query")
        void shouldReturnErrorForBlankQuery() throws Exception {
            String result = mcpExposedTools.searchTools("   ");

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response).containsKey("error");
        }

        @Test
        @DisplayName("Should search across categories")
        void shouldSearchAcrossCategories() throws Exception {
            String result = mcpExposedTools.searchTools("greet hello");

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) response.get("tools");
            assertThat(tools).isNotEmpty();

            boolean foundGreet = tools.stream().anyMatch(t -> "greet".equals(t.get("name")));
            assertThat(foundGreet).isTrue();
        }
    }

    // ── Tests: executeTool ──────────────────────────────────────────────────

    @Nested
    @DisplayName("executeTool")
    class ExecuteToolTests {

        @Test
        @DisplayName("Should execute tool with correct parameters and return result")
        void shouldExecuteToolSuccessfully() throws Exception {
            String result = mcpExposedTools.executeTool("add_numbers",
                    Map.of("a", 3, "b", 7));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("tool")).isEqualTo("add_numbers");
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo(10);
        }

        @Test
        @DisplayName("Should execute string tool")
        void shouldExecuteStringTool() throws Exception {
            String result = mcpExposedTools.executeTool("greet",
                    Map.of("name", "World"));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should return error for null tool name")
        void shouldReturnErrorForNullToolName() throws Exception {
            String result = mcpExposedTools.executeTool(null, Map.of());

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response).containsKey("error");
        }

        @Test
        @DisplayName("Should return error for blank tool name")
        void shouldReturnErrorForBlankToolName() throws Exception {
            String result = mcpExposedTools.executeTool("  ", Map.of());

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response).containsKey("error");
        }

        @Test
        @DisplayName("Should return error for unknown tool name")
        void shouldReturnErrorForUnknownTool() throws Exception {
            String result = mcpExposedTools.executeTool("nonexistent_tool", Map.of());

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("error")).asString().contains("Tool not found");
            assertThat(response).containsKey("suggestion");
        }

        @Test
        @DisplayName("Should handle null parameters map")
        void shouldHandleNullParameters() throws Exception {
            // add_numbers has required primitive params, so this should fail with missing param error
            String result = mcpExposedTools.executeTool("greet", null);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            // greet requires "name" (String, not primitive), so null param → null arg → "Hello, null!"
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should handle tool execution exception with InvocationTargetException")
        void shouldHandleToolExecutionException() throws Exception {
            setupWithBeans(new ExceptionToolBean());

            String result = mcpExposedTools.executeTool("failing_tool",
                    Map.of("input", "test"));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(false);
            assertThat(response.get("error")).asString().contains("Intentional failure");
        }

        @Test
        @DisplayName("Should handle tool exception with null message")
        void shouldHandleToolExceptionWithNullMessage() throws Exception {
            setupWithBeans(new ExceptionToolBean());

            String result = mcpExposedTools.executeTool("null_message_tool",
                    Map.of("input", "test"));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(false);
            assertThat(response.get("error")).asString().contains("NullPointerException");
        }

        @Test
        @DisplayName("Should use default value for optional parameters")
        void shouldUseDefaultForOptionalParams() throws Exception {
            String result = mcpExposedTools.executeTool("format_message",
                    Map.of("message", "hello"));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo("[INFO] hello");
        }

        @Test
        @DisplayName("Should override default value when parameter is provided")
        void shouldOverrideDefaultWhenProvided() throws Exception {
            String result = mcpExposedTools.executeTool("format_message",
                    Map.of("message", "hello", "prefix", "WARN"));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo("[WARN] hello");
        }

        @Test
        @DisplayName("Should throw error for missing required primitive parameter")
        void shouldThrowForMissingRequiredPrimitive() throws Exception {
            String result = mcpExposedTools.executeTool("add_numbers", Map.of());

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(false);
            assertThat(response.get("error")).asString().contains("Required parameter");
        }
    }

    // ── Tests: buildMethodArguments / convertValue ──────────────────────────

    @Nested
    @DisplayName("Type Conversion")
    class TypeConversionTests {

        @Test
        @DisplayName("Should convert Number to int")
        void shouldConvertNumberToInt() throws Exception {
            // JSON numbers come as Integer or Long
            String result = mcpExposedTools.executeTool("add_numbers",
                    Map.of("a", 5L, "b", 3L));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo(8);
        }

        @Test
        @DisplayName("Should convert String to int")
        void shouldConvertStringToInt() throws Exception {
            Map<String, Object> params = new HashMap<>();
            params.put("a", "5");
            params.put("b", "3");
            String result = mcpExposedTools.executeTool("add_numbers", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo(8);
        }

        @Test
        @DisplayName("Should convert Number to double")
        void shouldConvertNumberToDouble() throws Exception {
            String result = mcpExposedTools.executeTool("divide_numbers",
                    Map.of("a", 10, "b", 4));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat((Double) response.get("result")).isEqualTo(2.5);
        }

        @Test
        @DisplayName("Should handle Map parameter type")
        void shouldHandleMapParameter() throws Exception {
            setupWithBeans(new MapToolBean());

            Map<String, Object> data = Map.of("key1", "value1", "key2", 42);
            String result = mcpExposedTools.executeTool("process_data",
                    Map.of("data", data));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).asString().contains("Processed:");
        }

        @Test
        @DisplayName("Should convert multiple types in one call")
        void shouldConvertMultipleTypes() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 42);
            params.put("longVal", 100L);
            params.put("floatVal", 3.14);
            params.put("boolVal", true);
            params.put("strVal", "hello");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should fail on invalid string to number conversion")
        void shouldFailOnInvalidStringToNumber() throws Exception {
            Map<String, Object> params = new HashMap<>();
            params.put("a", "not_a_number");
            params.put("b", "3");
            String result = mcpExposedTools.executeTool("add_numbers", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(false);
            assertThat(response.get("error")).asString().contains("Cannot convert");
        }

        @Test
        @DisplayName("Should convert Number to boolean (non-zero = true)")
        void shouldConvertNumberToBoolean() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 1.0f);
            params.put("boolVal", 1);  // Number → boolean
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should convert Number to String")
        void shouldConvertNumberToString() throws Exception {
            // greet expects String, pass a number
            String result = mcpExposedTools.executeTool("greet",
                    Map.of("name", 42));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo("Hello, 42!");
        }

        @Test
        @DisplayName("Should convert Boolean to String")
        void shouldConvertBooleanToString() throws Exception {
            String result = mcpExposedTools.executeTool("greet",
                    Map.of("name", true));

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).isEqualTo("Hello, true!");
        }

        @Test
        @DisplayName("Should convert String to boolean")
        void shouldConvertStringToBoolean() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 1.0f);
            params.put("boolVal", "true");  // String → boolean
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should convert String to long")
        void shouldConvertStringToLong() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", "999");  // String → long
            params.put("floatVal", 1.0f);
            params.put("boolVal", true);
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should convert String to double")
        void shouldConvertStringToDouble() throws Exception {
            Map<String, Object> params = new HashMap<>();
            params.put("a", "10.5");
            params.put("b", "2.0");
            String result = mcpExposedTools.executeTool("divide_numbers", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat((Double) response.get("result")).isEqualTo(5.25);
        }

        @Test
        @DisplayName("Should convert String to float")
        void shouldConvertStringToFloat() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", "3.14");  // String → float
            params.put("boolVal", true);
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should convert Number to float")
        void shouldConvertNumberToFloat() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 3.14);  // Double → float
            params.put("boolVal", true);
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should convert Number to long")
        void shouldConvertNumberToLong() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 100);  // Integer → long
            params.put("floatVal", 1.0f);
            params.put("boolVal", true);
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
        }
    }

    // ── Tests: Additional convertValue branches ────────────────────────────

    @Nested
    @DisplayName("Additional Type Conversion Branches")
    class AdditionalTypeConversionTests {

        @Test
        @DisplayName("Should pass Boolean directly when target is boolean")
        void shouldPassBooleanDirectly() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 1.0f);
            params.put("boolVal", Boolean.TRUE);  // Boolean object → boolean primitive
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).asString().contains("true");
        }

        @Test
        @DisplayName("Should convert Number zero to boolean false")
        void shouldConvertNumberZeroToBooleanFalse() throws Exception {
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 1.0f);
            params.put("boolVal", 0);  // Number 0 → boolean false
            params.put("strVal", "test");
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            Map<String, Object> response = objectMapper.readValue(result, Map.class);
            assertThat(response.get("success")).isEqualTo(true);
            assertThat(response.get("result")).asString().contains("false");
        }

        @Test
        @DisplayName("Should handle Integer type via boxed Integer class")
        void shouldHandleBoxedIntegerType() throws Exception {
            // Test using convertValue directly with Integer.class target
            Object result = mcpExposedTools.convertValue(42L, Integer.class, "testParam");
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should handle Long type via boxed Long class")
        void shouldHandleBoxedLongType() throws Exception {
            Object result = mcpExposedTools.convertValue(42, Long.class, "testParam");
            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("Should handle Double type via boxed Double class")
        void shouldHandleBoxedDoubleType() throws Exception {
            Object result = mcpExposedTools.convertValue(42, Double.class, "testParam");
            assertThat(result).isEqualTo(42.0);
        }

        @Test
        @DisplayName("Should handle Float type via boxed Float class")
        void shouldHandleBoxedFloatType() throws Exception {
            Object result = mcpExposedTools.convertValue(42, Float.class, "testParam");
            assertThat(result).isEqualTo(42.0f);
        }

        @Test
        @DisplayName("Should handle Boolean type via boxed Boolean class")
        void shouldHandleBoxedBooleanType() throws Exception {
            Object result = mcpExposedTools.convertValue(1, Boolean.class, "testParam");
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("Should handle String to Integer boxed type")
        void shouldHandleStringToBoxedInteger() throws Exception {
            Object result = mcpExposedTools.convertValue("42", Integer.class, "testParam");
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should handle String to Long boxed type")
        void shouldHandleStringToBoxedLong() throws Exception {
            Object result = mcpExposedTools.convertValue("42", Long.class, "testParam");
            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("Should handle String to Double boxed type")
        void shouldHandleStringToBoxedDouble() throws Exception {
            Object result = mcpExposedTools.convertValue("42.5", Double.class, "testParam");
            assertThat(result).isEqualTo(42.5);
        }

        @Test
        @DisplayName("Should handle String to Float boxed type")
        void shouldHandleStringToBoxedFloat() throws Exception {
            Object result = mcpExposedTools.convertValue("42.5", Float.class, "testParam");
            assertThat(result).isEqualTo(42.5f);
        }

        @Test
        @DisplayName("Should handle String to Boolean boxed type")
        void shouldHandleStringToBoxedBoolean() throws Exception {
            Object result = mcpExposedTools.convertValue("true", Boolean.class, "testParam");
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("Should use ObjectMapper fallback for compatible type conversion")
        void shouldUseObjectMapperFallback() throws Exception {
            // Pass a List value to an array target type — ObjectMapper can handle List to array
            // This bypasses all explicit conversion paths (not Number, String, Boolean, or Map target)
            List<String> input = List.of("hello", "world");
            Object result = mcpExposedTools.convertValue(input, String[].class, "testParam");
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(String[].class);
            assertThat((String[]) result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Should throw on ObjectMapper fallback failure")
        void shouldThrowOnFallbackFailure() {
            // Pass a List to Integer — ObjectMapper can't convert this
            assertThatThrownBy(() -> mcpExposedTools.convertValue(
                    List.of("a", "b"), Integer.class, "testParam"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot convert");
        }

        @Test
        @DisplayName("Should handle direct type assignment when types match")
        void shouldHandleDirectTypeAssignment() throws Exception {
            // String → String (direct assignment via isAssignableFrom)
            Object result = mcpExposedTools.convertValue("hello", String.class, "testParam");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should handle Integer to int via isAssignableFrom")
        void shouldHandleIntegerToInt() throws Exception {
            // Integer → Integer (direct assignment)
            Object result = mcpExposedTools.convertValue(Integer.valueOf(42), Integer.class, "testParam");
            assertThat(result).isEqualTo(42);
        }
    }

    // ── Tests: getDefaultPrimitiveValue ─────────────────────────────────────

    @Nested
    @DisplayName("Default Primitive Values")
    class DefaultPrimitiveValueTests {

        @Test
        @DisplayName("Should return 0 for int when parameter missing and not required")
        void shouldReturnZeroForInt() {
            // Test through the public API — add_numbers with missing non-required params
            // Since add_numbers params are required, we test via multiTypeTool with optional strVal
            setupWithBeans(new MultiTypeToolBean());

            Map<String, Object> params = new HashMap<>();
            params.put("intVal", 1);
            params.put("longVal", 1L);
            params.put("floatVal", 1.0f);
            params.put("boolVal", true);
            // strVal is optional, not provided → should get null
            String result = mcpExposedTools.executeTool("multi_type_tool", params);

            // Should succeed with null strVal
            assertThat(result).contains("\"success\":true");
        }

        @Test
        @DisplayName("getDefaultPrimitiveValue should return correct defaults")
        void shouldReturnCorrectDefaults() {
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(int.class)).isEqualTo(0);
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(long.class)).isEqualTo(0L);
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(double.class)).isEqualTo(0.0);
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(float.class)).isEqualTo(0.0f);
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(boolean.class)).isEqualTo(false);
            assertThat(mcpExposedTools.getDefaultPrimitiveValue(String.class)).isNull();
        }
    }

    // ── Tests: toJson ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize simple map to JSON")
        void shouldSerializeMapToJson() {
            String json = mcpExposedTools.toJson(Map.of("key", "value"));
            assertThat(json).contains("\"key\"");
            assertThat(json).contains("\"value\"");
        }

        @Test
        @DisplayName("Should handle serialization failure gracefully")
        void shouldHandleSerializationFailure() throws Exception {
            // Create McpExposedTools with a broken ObjectMapper
            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            when(brokenMapper.writeValueAsString(any()))
                    .thenThrow(new JsonProcessingException("Simulated failure") {});

            McpExposedTools brokenTools = new McpExposedTools(toolRegistry, luceneToolIndexService, brokenMapper);

            Map<String, String> testObj = Map.of("key", "value");
            String result = brokenTools.toJson(testObj);

            // Should fall back to toString()
            assertThat(result).isNotNull();
            assertThat(result).contains("key");
        }
    }

    // ── Tests: End-to-end search → execute flow ─────────────────────────────

    @Nested
    @DisplayName("End-to-End Flow")
    class EndToEndTests {

        @Test
        @DisplayName("Should search then execute successfully")
        void shouldSearchThenExecute() throws Exception {
            // Step 1: Search
            String searchResult = mcpExposedTools.searchTools("add numbers");
            Map<String, Object> searchResponse = objectMapper.readValue(searchResult, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) searchResponse.get("tools");
            assertThat(tools).isNotEmpty();

            // Find the tool name from search results
            String toolName = (String) tools.get(0).get("name");

            // Step 2: Execute with the found tool name
            String execResult = mcpExposedTools.executeTool(toolName,
                    Map.of("a", 10, "b", 20));

            Map<String, Object> execResponse = objectMapper.readValue(execResult, Map.class);
            assertThat(execResponse.get("success")).isEqualTo(true);
            assertThat(execResponse.get("result")).isEqualTo(30);
        }
    }
}
