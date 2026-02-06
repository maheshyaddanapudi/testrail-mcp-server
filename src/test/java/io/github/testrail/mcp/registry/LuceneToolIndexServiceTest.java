package io.github.testrail.mcp.registry;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LuceneToolIndexService}.
 * Covers index building, search across multiple fields, edge cases,
 * query sanitization, result ranking, and the ToolSearchResult DTO.
 */
@ExtendWith(MockitoExtension.class)
class LuceneToolIndexServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    private InternalToolRegistry toolRegistry;
    private LuceneToolIndexService indexService;

    // ── Test fixture beans ──────────────────────────────────────────────────

    public static class CaseToolBean {
        @InternalTool(
                name = "get_case",
                description = "Retrieves a specific test case from TestRail by its unique ID. Returns complete details including title, steps, expected results.",
                category = "test-cases",
                examples = {"execute_tool('get_case', {caseId: 123})"},
                keywords = {"get", "retrieve", "fetch", "case", "details"}
        )
        public String getCase(
                @InternalToolParam(description = "The unique identifier of the test case")
                int caseId
        ) {
            return "case-" + caseId;
        }

        @InternalTool(
                name = "add_case",
                description = "Creates a new test case in a specified section of a TestRail project. Requires a title and section ID.",
                category = "test-cases",
                examples = {"execute_tool('add_case', {sectionId: 1, title: 'Login test'})"},
                keywords = {"add", "create", "new", "case"}
        )
        public String addCase(
                @InternalToolParam(description = "The section ID") int sectionId,
                @InternalToolParam(description = "The title") String title
        ) {
            return "added";
        }
    }

    public static class RunToolBean {
        @InternalTool(
                name = "get_run",
                description = "Retrieves a specific test run from TestRail. Returns run details including status, assignee, and configuration.",
                category = "test-runs",
                examples = {"execute_tool('get_run', {runId: 42})"},
                keywords = {"get", "retrieve", "run", "execution"}
        )
        public String getRun(
                @InternalToolParam(description = "The run ID") int runId
        ) {
            return "run-" + runId;
        }
    }

    public static class MilestoneToolBean {
        @InternalTool(
                name = "get_milestone",
                description = "Retrieves a specific milestone from TestRail. Returns milestone details including due date and progress.",
                category = "milestones",
                examples = {"execute_tool('get_milestone', {milestoneId: 10})"},
                keywords = {"get", "milestone", "deadline", "progress"}
        )
        public String getMilestone(
                @InternalToolParam(description = "The milestone ID") int milestoneId
        ) {
            return "milestone-" + milestoneId;
        }
    }

    public static class ResultToolBean {
        @InternalTool(
                name = "add_result",
                description = "Adds a test result for a specific test in a test run. Supports status, comment, elapsed time, and custom fields.",
                category = "test-results",
                examples = {"execute_tool('add_result', {testId: 5, statusId: 1})"},
                keywords = {"add", "result", "status", "pass", "fail"}
        )
        public String addResult(
                @InternalToolParam(description = "The test ID") int testId,
                @InternalToolParam(description = "The status ID") int statusId
        ) {
            return "result-added";
        }
    }

    // ── Setup helpers ───────────────────────────────────────────────────────

    private void setupRegistryWithBeans(Object... beans) {
        String[] beanNames = new String[beans.length];
        for (int i = 0; i < beans.length; i++) {
            beanNames[i] = "bean" + i;
            when(applicationContext.getBean("bean" + i)).thenReturn(beans[i]);
        }
        when(applicationContext.getBeanDefinitionNames()).thenReturn(beanNames);

        toolRegistry = new InternalToolRegistry(applicationContext);
        toolRegistry.init();

        indexService = new LuceneToolIndexService(toolRegistry);
        indexService.init();
    }

    // ── Tests: Index Building ───────────────────────────────────────────────

    @Nested
    @DisplayName("Index Building")
    class IndexBuildingTests {

        @Test
        @DisplayName("Should index all tools from registry")
        void shouldIndexAllTools() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean());

            assertThat(indexService.getIndexedToolCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle empty registry gracefully")
        void shouldHandleEmptyRegistry() {
            when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[0]);
            toolRegistry = new InternalToolRegistry(applicationContext);
            toolRegistry.init();

            indexService = new LuceneToolIndexService(toolRegistry);
            indexService.init();

            assertThat(indexService.getIndexedToolCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create document with correct fields")
        void shouldCreateDocumentWithCorrectFields() {
            setupRegistryWithBeans(new CaseToolBean());

            InternalToolRegistry.ToolDefinition tool = toolRegistry.getTool("get_case");
            Document doc = indexService.createDocument(tool);

            assertThat(doc.get(LuceneToolIndexService.FIELD_NAME)).isEqualTo("get_case");
            assertThat(doc.get(LuceneToolIndexService.FIELD_CATEGORY)).isEqualTo("test-cases");
        }
    }

    // ── Tests: Search by Name ───────────────────────────────────────────────

    @Nested
    @DisplayName("Search by Name")
    class SearchByNameTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Should find tool by exact name")
        void shouldFindByExactName() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("get_case");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getToolName()).isEqualTo("get_case");
        }

        @Test
        @DisplayName("Should find tool by partial name words")
        void shouldFindByPartialName() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("case");

            assertThat(results).isNotEmpty();
            // Both get_case and add_case should match
            List<String> names = results.stream().map(LuceneToolIndexService.ToolSearchResult::getToolName).toList();
            assertThat(names).contains("get_case", "add_case");
        }
    }

    // ── Tests: Search by Description ────────────────────────────────────────

    @Nested
    @DisplayName("Search by Description")
    class SearchByDescriptionTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Should find tool by description keywords")
        void shouldFindByDescriptionKeywords() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("milestone due date progress");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getToolName()).isEqualTo("get_milestone");
        }

        @Test
        @DisplayName("Should find tool by description phrase")
        void shouldFindByDescriptionPhrase() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("test run status assignee configuration");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getToolName()).isEqualTo("get_run");
        }
    }

    // ── Tests: Search by Keywords ───────────────────────────────────────────

    @Nested
    @DisplayName("Search by Keywords")
    class SearchByKeywordsTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Should find tool by annotated keywords")
        void shouldFindByKeywords() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("deadline");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getToolName()).isEqualTo("get_milestone");
        }

        @Test
        @DisplayName("Should find tool by status-related keywords")
        void shouldFindByStatusKeywords() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("pass fail");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getToolName()).isEqualTo("add_result");
        }
    }

    // ── Tests: Search by Category ───────────────────────────────────────────

    @Nested
    @DisplayName("Search by Category")
    class SearchByCategoryTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Should find tools by category name")
        void shouldFindByCategory() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("test-results");

            assertThat(results).isNotEmpty();
            assertThat(results.get(0).getCategory()).isEqualTo("test-results");
        }
    }

    // ── Tests: Search Edge Cases ────────────────────────────────────────────

    @Nested
    @DisplayName("Search Edge Cases")
    class SearchEdgeCaseTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean());
        }

        @Test
        @DisplayName("Should return empty list for null query")
        void shouldReturnEmptyForNullQuery() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search(null);
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty query")
        void shouldReturnEmptyForEmptyQuery() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for blank query")
        void shouldReturnEmptyForBlankQuery() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("   ");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for query with only special characters")
        void shouldReturnEmptyForSpecialCharsOnly() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("+++---***");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for non-matching query")
        void shouldReturnEmptyForNonMatchingQuery() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("xyznonexistent");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should respect maxResults limit")
        void shouldRespectMaxResults() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("get", 1);
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should use default max results with single-arg search")
        void shouldUseDefaultMaxResults() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("get");
            // Should return results (up to default 10), not fail
            assertThat(results).isNotEmpty();
            assertThat(results.size()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("Should handle query with Lucene special characters gracefully")
        void shouldHandleSpecialCharacters() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("get+case:test [run]");
            // Should not throw, may or may not return results depending on sanitization
            assertThat(results).isNotNull();
        }
    }

    // ── Tests: Query Sanitization ───────────────────────────────────────────

    @Nested
    @DisplayName("Query Sanitization")
    class SanitizationTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean());
        }

        @Test
        @DisplayName("Should remove Lucene special characters")
        void shouldRemoveSpecialChars() {
            String sanitized = indexService.sanitizeQuery("get+case:test[run]");
            assertThat(sanitized).doesNotContain("+", ":", "[", "]");
            assertThat(sanitized).contains("get", "case", "test", "run");
        }

        @Test
        @DisplayName("Should collapse multiple spaces")
        void shouldCollapseSpaces() {
            String sanitized = indexService.sanitizeQuery("get   case    test");
            assertThat(sanitized).isEqualTo("get case test");
        }

        @Test
        @DisplayName("Should return empty string for null input")
        void shouldReturnEmptyForNull() {
            String sanitized = indexService.sanitizeQuery(null);
            assertThat(sanitized).isEmpty();
        }

        @Test
        @DisplayName("Should preserve alphanumeric and basic characters")
        void shouldPreserveAlphanumeric() {
            String sanitized = indexService.sanitizeQuery("get_case test-run 123");
            assertThat(sanitized).contains("get_case", "test", "run", "123");
        }
    }

    // ── Tests: Search Result Ranking ────────────────────────────────────────

    @Nested
    @DisplayName("Search Result Ranking")
    class RankingTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Results should have positive scores")
        void resultsShouldHavePositiveScores() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("get case");

            assertThat(results).isNotEmpty();
            for (LuceneToolIndexService.ToolSearchResult result : results) {
                assertThat(result.getScore()).isGreaterThan(0f);
            }
        }

        @Test
        @DisplayName("Results should be ordered by descending score")
        void resultsShouldBeOrderedByScore() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("retrieve test");

            if (results.size() > 1) {
                for (int i = 0; i < results.size() - 1; i++) {
                    assertThat(results.get(i).getScore())
                            .isGreaterThanOrEqualTo(results.get(i + 1).getScore());
                }
            }
        }
    }

    // ── Tests: ToolSearchResult DTO ─────────────────────────────────────────

    @Nested
    @DisplayName("ToolSearchResult DTO")
    class ToolSearchResultTests {

        @Test
        @DisplayName("Should store and return all fields correctly")
        void shouldStoreAllFields() {
            // Create a minimal ToolDefinition for testing
            InternalToolRegistry.ToolDefinition toolDef = new InternalToolRegistry.ToolDefinition(
                    "my_tool", "My tool description", "my-category",
                    java.util.List.of(), java.util.List.of(),
                    new Object(), null, java.util.List.of()
            );
            LuceneToolIndexService.ToolSearchResult result =
                    new LuceneToolIndexService.ToolSearchResult("my_tool", "my-category", toolDef, 1.5f);

            assertThat(result.getToolName()).isEqualTo("my_tool");
            assertThat(result.getCategory()).isEqualTo("my-category");
            assertThat(result.getToolDefinition()).isNotNull();
            assertThat(result.getToolDefinition().getName()).isEqualTo("my_tool");
            assertThat(result.getScore()).isEqualTo(1.5f);
        }
    }

    // ── Tests: getIndexedToolCount ──────────────────────────────────────────

    @Nested
    @DisplayName("Indexed Tool Count")
    class IndexedToolCountTests {

        @Test
        @DisplayName("Should return correct count after indexing")
        void shouldReturnCorrectCount() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean());
            assertThat(indexService.getIndexedToolCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return zero for empty index")
        void shouldReturnZeroForEmpty() {
            when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[0]);
            toolRegistry = new InternalToolRegistry(applicationContext);
            toolRegistry.init();

            indexService = new LuceneToolIndexService(toolRegistry);
            indexService.init();

            assertThat(indexService.getIndexedToolCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero when indexReader is null (before init)")
        void shouldReturnZeroWhenIndexReaderNull() {
            when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[0]);
            toolRegistry = new InternalToolRegistry(applicationContext);
            toolRegistry.init();

            // Create service but do NOT call init()
            indexService = new LuceneToolIndexService(toolRegistry);

            assertThat(indexService.getIndexedToolCount()).isEqualTo(0);
        }
    }

    // ── Tests: Search before init (indexSearcher null) ──────────────────────

    @Nested
    @DisplayName("Search Before Init")
    class SearchBeforeInitTests {

        @Test
        @DisplayName("Should return empty list when searching before init")
        void shouldReturnEmptyWhenSearcherNull() {
            when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[0]);
            toolRegistry = new InternalToolRegistry(applicationContext);
            toolRegistry.init();

            // Create service but do NOT call init() — indexSearcher remains null
            indexService = new LuceneToolIndexService(toolRegistry);

            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("anything");
            assertThat(results).isEmpty();
        }
    }

    // ── Tests: Rebuild index (re-open reader path) ──────────────────────────

    @Nested
    @DisplayName("Index Rebuild")
    class IndexRebuildTests {

        @Test
        @DisplayName("Should re-open reader when buildIndex is called again")
        void shouldReopenReaderOnRebuild() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean());

            // First build already done in setup. Verify initial state.
            assertThat(indexService.getIndexedToolCount()).isEqualTo(3);

            // Call buildIndex again — this exercises the indexReader != null branch in openReader()
            indexService.buildIndex();

            // Should still work correctly after rebuild
            assertThat(indexService.getIndexedToolCount()).isEqualTo(3);
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("case");
            assertThat(results).isNotEmpty();
        }
    }

    // ── Tests: Search with null indexSearcher via reflection ─────────────────

    @Nested
    @DisplayName("Null IndexSearcher via Reflection")
    class NullSearcherReflectionTests {

        @Test
        @DisplayName("Should return empty when indexSearcher is set to null after init")
        void shouldReturnEmptyWhenSearcherNulledAfterInit() throws Exception {
            setupRegistryWithBeans(new CaseToolBean());

            // Verify search works before nulling
            assertThat(indexService.search("case")).isNotEmpty();

            // Use reflection to null out indexSearcher
            Field searcherField = LuceneToolIndexService.class.getDeclaredField("indexSearcher");
            searcherField.setAccessible(true);
            searcherField.set(indexService, null);

            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("case");
            assertThat(results).isEmpty();
        }
    }

    // ── Tests: Search with examples field ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Search by Examples")
    class SearchByExamplesTests {

        @BeforeEach
        void setUp() {
            setupRegistryWithBeans(new CaseToolBean(), new RunToolBean(), new MilestoneToolBean(), new ResultToolBean());
        }

        @Test
        @DisplayName("Should find tool by example content")
        void shouldFindByExampleContent() {
            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("execute_tool caseId");
            assertThat(results).isNotEmpty();
            // get_case has caseId in its example
            assertThat(results.get(0).getToolName()).isEqualTo("get_case");
        }
    }

    // ── Tests: Search result filtering (toolDef null) ────────────────────────────────────────────────

    @Nested
    @DisplayName("Search Result Filtering")
    class SearchResultFilteringTests {

        @Test
        @DisplayName("Should include full tool definition in search results")
        void shouldIncludeToolDefinitionInResults() {
            setupRegistryWithBeans(new CaseToolBean());

            List<LuceneToolIndexService.ToolSearchResult> results = indexService.search("case");
            assertThat(results).isNotEmpty();

            LuceneToolIndexService.ToolSearchResult first = results.get(0);
            assertThat(first.getToolDefinition()).isNotNull();
            assertThat(first.getToolDefinition().getName()).isEqualTo(first.getToolName());
            assertThat(first.getToolDefinition().getDescription()).isNotBlank();
            assertThat(first.getToolDefinition().getCategory()).isEqualTo(first.getCategory());
        }
    }
}