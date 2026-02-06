package io.github.testrail.mcp.registry;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InternalToolRegistry}.
 * Covers scanning, registration, duplicate handling, proxy handling,
 * parameter extraction, public API, and inner class behavior.
 */
@ExtendWith(MockitoExtension.class)
class InternalToolRegistryTest {

    @Mock
    private ApplicationContext applicationContext;

    private InternalToolRegistry registry;

    // ── Test fixtures: fake tool beans ───────────────────────────────────────

    /**
     * A simple bean with one @InternalTool method that has an explicit name.
     */
    public static class SimpleToolBean {
        @InternalTool(
                name = "simple_tool",
                description = "A simple tool for testing",
                category = "testing",
                examples = {"execute_tool('simple_tool', {id: 1})"},
                keywords = {"simple", "test"}
        )
        public String simpleTool(
                @InternalToolParam(description = "The ID parameter")
                int id
        ) {
            return "result-" + id;
        }
    }

    /**
     * A bean with multiple @InternalTool methods.
     */
    public static class MultiToolBean {
        @InternalTool(
                name = "tool_alpha",
                description = "First tool",
                category = "multi",
                examples = {},
                keywords = {"alpha"}
        )
        public String toolAlpha() {
            return "alpha";
        }

        @InternalTool(
                name = "tool_beta",
                description = "Second tool",
                category = "multi",
                examples = {"execute_tool('tool_beta', {x: 1})"},
                keywords = {"beta"}
        )
        public int toolBeta(
                @InternalToolParam(description = "X value")
                int x,
                @InternalToolParam(description = "Optional Y value", required = false, defaultValue = "0")
                Integer y
        ) {
            return x + (y != null ? y : 0);
        }

        // A method WITHOUT @InternalTool — should be ignored
        public void notATool() {
            // no-op
        }
    }

    /**
     * A bean with a method that uses default name derivation (empty name in annotation).
     */
    public static class DefaultNameBean {
        @InternalTool(
                name = "",
                description = "Tool with default snake_case name",
                category = "naming"
        )
        public String myFancyMethod() {
            return "fancy";
        }
    }

    /**
     * A bean that duplicates the tool name "simple_tool" from SimpleToolBean.
     */
    public static class DuplicateToolBean {
        @InternalTool(
                name = "simple_tool",
                description = "Duplicate tool name",
                category = "duplicate"
        )
        public String duplicateTool() {
            return "dup";
        }
    }

    /**
     * A bean with a method that has parameters WITHOUT @InternalToolParam annotation.
     */
    public static class UnannotatedParamBean {
        @InternalTool(
                name = "unannotated_param_tool",
                description = "Tool with unannotated params",
                category = "params"
        )
        public String doWork(String rawParam) {
            return rawParam;
        }
    }

    /**
     * A bean with no @InternalTool methods at all.
     */
    public static class NoToolBean {
        public String regularMethod() {
            return "nothing";
        }
    }

    // ── Helper to set up ApplicationContext mock ─────────────────────────────

    private void setupContext(Object... beans) {
        String[] beanNames = new String[beans.length];
        for (int i = 0; i < beans.length; i++) {
            beanNames[i] = "bean" + i;
            when(applicationContext.getBean("bean" + i)).thenReturn(beans[i]);
        }
        when(applicationContext.getBeanDefinitionNames()).thenReturn(beanNames);
    }

    private void setupContextWithFailingBean(Object goodBean) {
        String[] beanNames = {"goodBean", "failingBean"};
        when(applicationContext.getBeanDefinitionNames()).thenReturn(beanNames);
        when(applicationContext.getBean("goodBean")).thenReturn(goodBean);
        when(applicationContext.getBean("failingBean")).thenThrow(new RuntimeException("Cannot instantiate"));
    }

    // ── Tests: Scanning & Registration ──────────────────────────────────────

    @Nested
    @DisplayName("Scanning and Registration")
    class ScanningTests {

        @Test
        @DisplayName("Should discover and register a single tool from a simple bean")
        void shouldRegisterSingleTool() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(1);
            assertThat(registry.hasTool("simple_tool")).isTrue();

            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            assertThat(def).isNotNull();
            assertThat(def.getName()).isEqualTo("simple_tool");
            assertThat(def.getDescription()).isEqualTo("A simple tool for testing");
            assertThat(def.getCategory()).isEqualTo("testing");
            assertThat(def.getExamples()).containsExactly("execute_tool('simple_tool', {id: 1})");
            assertThat(def.getKeywords()).containsExactly("simple", "test");
            assertThat(def.getBean()).isSameAs(bean);
            assertThat(def.getMethod().getName()).isEqualTo("simpleTool");
        }

        @Test
        @DisplayName("Should discover multiple tools from a multi-tool bean")
        void shouldRegisterMultipleTools() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(2);
            assertThat(registry.hasTool("tool_alpha")).isTrue();
            assertThat(registry.hasTool("tool_beta")).isTrue();
        }

        @Test
        @DisplayName("Should discover tools from multiple beans")
        void shouldRegisterToolsFromMultipleBeans() {
            SimpleToolBean bean1 = new SimpleToolBean();
            MultiToolBean bean2 = new MultiToolBean();
            setupContext(bean1, bean2);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(3);
            assertThat(registry.getToolNames()).containsExactlyInAnyOrder(
                    "simple_tool", "tool_alpha", "tool_beta");
        }

        @Test
        @DisplayName("Should ignore methods without @InternalTool annotation")
        void shouldIgnoreNonAnnotatedMethods() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            // MultiToolBean has 3 methods but only 2 are annotated
            assertThat(registry.getToolCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should ignore beans with no @InternalTool methods")
        void shouldIgnoreBeansWithNoTools() {
            NoToolBean bean = new NoToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty application context")
        void shouldHandleEmptyContext() {
            when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[0]);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(0);
            assertThat(registry.getAllTools()).isEmpty();
        }

        @Test
        @DisplayName("Should skip beans that throw on getBean()")
        void shouldSkipFailingBeans() {
            SimpleToolBean goodBean = new SimpleToolBean();
            setupContextWithFailingBean(goodBean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(1);
            assertThat(registry.hasTool("simple_tool")).isTrue();
        }
    }

    // ── Tests: Name Derivation ──────────────────────────────────────────────

    @Nested
    @DisplayName("Tool Name Derivation")
    class NameDerivationTests {

        @Test
        @DisplayName("Should use explicit name from annotation when provided")
        void shouldUseExplicitName() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.hasTool("simple_tool")).isTrue();
        }

        @Test
        @DisplayName("Should derive snake_case name from method name when name is empty")
        void shouldDeriveSnakeCaseName() {
            DefaultNameBean bean = new DefaultNameBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            assertThat(registry.getToolCount()).isEqualTo(1);
            assertThat(registry.hasTool("my_fancy_method")).isTrue();

            InternalToolRegistry.ToolDefinition def = registry.getTool("my_fancy_method");
            assertThat(def.getDescription()).isEqualTo("Tool with default snake_case name");
            assertThat(def.getCategory()).isEqualTo("naming");
        }
    }

    // ── Tests: Duplicate Handling ───────────────────────────────────────────

    @Nested
    @DisplayName("Duplicate Tool Name Handling")
    class DuplicateTests {

        @Test
        @DisplayName("Should keep first registration when duplicate tool name is found")
        void shouldKeepFirstOnDuplicate() {
            SimpleToolBean first = new SimpleToolBean();
            DuplicateToolBean second = new DuplicateToolBean();
            setupContext(first, second);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            // Only 1 tool registered (first wins)
            assertThat(registry.getToolCount()).isEqualTo(1);
            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            assertThat(def.getDescription()).isEqualTo("A simple tool for testing");
            assertThat(def.getBean()).isSameAs(first);
        }
    }

    // ── Tests: Parameter Extraction ─────────────────────────────────────────

    @Nested
    @DisplayName("Parameter Extraction")
    class ParameterExtractionTests {

        @Test
        @DisplayName("Should extract annotated parameter metadata correctly")
        void shouldExtractAnnotatedParams() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            List<InternalToolRegistry.ParameterDefinition> params = def.getParameters();

            assertThat(params).hasSize(1);
            InternalToolRegistry.ParameterDefinition param = params.get(0);
            assertThat(param.getDescription()).isEqualTo("The ID parameter");
            assertThat(param.getType()).isEqualTo(int.class);
            assertThat(param.isRequired()).isTrue();
            assertThat(param.getDefaultValue()).isEmpty();
        }

        @Test
        @DisplayName("Should extract multiple parameters with required/optional flags")
        void shouldExtractMultipleParams() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("tool_beta");
            List<InternalToolRegistry.ParameterDefinition> params = def.getParameters();

            assertThat(params).hasSize(2);

            // First param: required
            assertThat(params.get(0).getDescription()).isEqualTo("X value");
            assertThat(params.get(0).getType()).isEqualTo(int.class);
            assertThat(params.get(0).isRequired()).isTrue();

            // Second param: optional with default
            assertThat(params.get(1).getDescription()).isEqualTo("Optional Y value");
            assertThat(params.get(1).getType()).isEqualTo(Integer.class);
            assertThat(params.get(1).isRequired()).isFalse();
            assertThat(params.get(1).getDefaultValue()).isEqualTo("0");
        }

        @Test
        @DisplayName("Should extract zero parameters for no-arg tool methods")
        void shouldExtractZeroParams() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("tool_alpha");
            assertThat(def.getParameters()).isEmpty();
        }

        @Test
        @DisplayName("Should handle parameters without @InternalToolParam annotation")
        void shouldHandleUnannotatedParams() {
            UnannotatedParamBean bean = new UnannotatedParamBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("unannotated_param_tool");
            List<InternalToolRegistry.ParameterDefinition> params = def.getParameters();

            assertThat(params).hasSize(1);
            // No annotation: description is empty, required defaults to true, defaultValue is empty
            assertThat(params.get(0).getDescription()).isEmpty();
            assertThat(params.get(0).isRequired()).isTrue();
            assertThat(params.get(0).getDefaultValue()).isEmpty();
            assertThat(params.get(0).getType()).isEqualTo(String.class);
        }
    }

    // ── Tests: Public API ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Public API Methods")
    class PublicApiTests {

        @BeforeEach
        void setUp() {
            SimpleToolBean bean1 = new SimpleToolBean();
            MultiToolBean bean2 = new MultiToolBean();
            setupContext(bean1, bean2);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();
        }

        @Test
        @DisplayName("getTool should return definition for existing tool")
        void getToolShouldReturnDefinition() {
            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            assertThat(def).isNotNull();
            assertThat(def.getName()).isEqualTo("simple_tool");
        }

        @Test
        @DisplayName("getTool should return null for non-existent tool")
        void getToolShouldReturnNullForMissing() {
            assertThat(registry.getTool("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getAllTools should return unmodifiable collection of all tools")
        void getAllToolsShouldReturnAll() {
            Collection<InternalToolRegistry.ToolDefinition> all = registry.getAllTools();
            assertThat(all).hasSize(3);
            assertThatThrownBy(() -> all.clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getToolCount should return correct count")
        void getToolCountShouldReturnCorrect() {
            assertThat(registry.getToolCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("hasTool should return true for existing and false for missing")
        void hasToolShouldWork() {
            assertThat(registry.hasTool("simple_tool")).isTrue();
            assertThat(registry.hasTool("tool_alpha")).isTrue();
            assertThat(registry.hasTool("missing_tool")).isFalse();
        }

        @Test
        @DisplayName("getToolNames should return unmodifiable set of all names")
        void getToolNamesShouldReturnAll() {
            Set<String> names = registry.getToolNames();
            assertThat(names).containsExactlyInAnyOrder("simple_tool", "tool_alpha", "tool_beta");
            assertThatThrownBy(() -> names.add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ── Tests: ToolDefinition inner class ───────────────────────────────────

    @Nested
    @DisplayName("ToolDefinition")
    class ToolDefinitionTests {

        @Test
        @DisplayName("toFullDetails should include all annotation metadata")
        void toFullDetailsShouldBeComplete() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            Map<String, Object> details = def.toFullDetails();

            assertThat(details.get("name")).isEqualTo("simple_tool");
            assertThat(details.get("description")).isEqualTo("A simple tool for testing");
            assertThat(details.get("category")).isEqualTo("testing");
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) details.get("keywords");
            assertThat(keywords).containsExactlyInAnyOrder("test", "simple");
            @SuppressWarnings("unchecked")
            List<String> examples = (List<String>) details.get("examples");
            assertThat(examples).containsExactly("execute_tool('simple_tool', {id: 1})");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) details.get("parameters");
            assertThat(params).hasSize(1);
            assertThat(params.get(0).get("name")).isEqualTo("id");
            assertThat(params.get(0).get("type")).isEqualTo("int");
            assertThat(params.get(0).get("required")).isEqualTo(true);
        }

        @Test
        @DisplayName("toFullDetails should include full description without truncation")
        void toFullDetailsShouldNotTruncateDescription() throws Exception {
            String longDesc = "A".repeat(300);
            Method method = SimpleToolBean.class.getDeclaredMethod("simpleTool", int.class);

            InternalToolRegistry.ToolDefinition def = new InternalToolRegistry.ToolDefinition(
                    "long_desc_tool",
                    longDesc,
                    "testing",
                    List.of(),
                    List.of(),
                    new SimpleToolBean(),
                    method,
                    List.of()
            );

            Map<String, Object> details = def.toFullDetails();
            assertThat(details.get("description")).isEqualTo(longDesc);
        }

        @Test
        @DisplayName("toFullDetails should show optional params with default values")
        void toFullDetailsShouldShowOptionalParamsWithDefaults() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("tool_beta");
            Map<String, Object> details = def.toFullDetails();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) details.get("parameters");
            assertThat(params).hasSize(2);

            // First param: required
            assertThat(params.get(0).get("required")).isEqualTo(true);

            // Second param: optional
            assertThat(params.get(1).get("required")).isEqualTo(false);
        }

        @Test
        @DisplayName("toFullDetails should omit defaultValue when empty")
        void toFullDetailsShouldOmitEmptyDefaultValue() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");
            Map<String, Object> details = def.toFullDetails();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) details.get("parameters");
            assertThat(params.get(0)).doesNotContainKey("defaultValue");
        }

        @Test
        @DisplayName("toFullDetails should include empty parameters list for no-arg tools")
        void toFullDetailsShouldIncludeEmptyParamsForNoArgTools() {
            MultiToolBean bean = new MultiToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("tool_alpha");
            Map<String, Object> details = def.toFullDetails();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) details.get("parameters");
            assertThat(params).isEmpty();
        }

        @Test
        @DisplayName("getExamples and getKeywords should return unmodifiable lists")
        void listsShoudBeUnmodifiable() {
            SimpleToolBean bean = new SimpleToolBean();
            setupContext(bean);

            registry = new InternalToolRegistry(applicationContext);
            registry.init();

            InternalToolRegistry.ToolDefinition def = registry.getTool("simple_tool");

            assertThatThrownBy(() -> def.getExamples().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> def.getKeywords().add("hack"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> def.getParameters().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ── Tests: ParameterDefinition inner class ──────────────────────────────

    @Nested
    @DisplayName("ParameterDefinition")
    class ParameterDefinitionTests {

        @Test
        @DisplayName("Should correctly store and return all fields")
        void shouldStoreAllFields() {
            InternalToolRegistry.ParameterDefinition param = new InternalToolRegistry.ParameterDefinition(
                    "testParam", Integer.class, "A test parameter", true, "42"
            );

            assertThat(param.getName()).isEqualTo("testParam");
            assertThat(param.getType()).isEqualTo(Integer.class);
            assertThat(param.getDescription()).isEqualTo("A test parameter");
            assertThat(param.isRequired()).isTrue();
            assertThat(param.getDefaultValue()).isEqualTo("42");
        }

        @Test
        @DisplayName("Should handle optional parameter with empty default")
        void shouldHandleOptionalWithEmptyDefault() {
            InternalToolRegistry.ParameterDefinition param = new InternalToolRegistry.ParameterDefinition(
                    "optParam", String.class, "Optional param", false, ""
            );

            assertThat(param.isRequired()).isFalse();
            assertThat(param.getDefaultValue()).isEmpty();
        }
    }
}
