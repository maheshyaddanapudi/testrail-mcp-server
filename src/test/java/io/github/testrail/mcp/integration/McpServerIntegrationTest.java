package io.github.testrail.mcp.integration;

import io.github.testrail.mcp.registry.InternalToolRegistry;
import io.github.testrail.mcp.registry.LuceneToolIndexService;
import io.github.testrail.mcp.tools.McpExposedTools;
import io.github.testrail.mcp.tools.cases.CasesTools;
import io.github.testrail.mcp.tools.projects.ProjectsTools;
import io.github.testrail.mcp.tools.results.ResultsTools;
import io.github.testrail.mcp.tools.runs.RunsTools;
import io.github.testrail.mcp.tools.sections.SectionsTools;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for MCP Server configuration.
 * Verifies that the Spring context loads correctly and all components
 * (tool beans, registry, Lucene index, MCP-exposed tools) are properly wired.
 */
@SpringBootTest
class McpServerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasesTools casesTools;

    @Autowired
    private ProjectsTools projectsTools;

    @Autowired
    private RunsTools runsTools;

    @Autowired
    private ResultsTools resultsTools;

    @Autowired
    private SectionsTools sectionsTools;

    @Autowired
    private InternalToolRegistry internalToolRegistry;

    @Autowired
    private LuceneToolIndexService luceneToolIndexService;

    @Autowired
    private McpExposedTools mcpExposedTools;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void casesToolsShouldBeRegistered() {
        assertThat(casesTools).isNotNull();
    }

    @Test
    void projectsToolsShouldBeRegistered() {
        assertThat(projectsTools).isNotNull();
    }

    @Test
    void runsToolsShouldBeRegistered() {
        assertThat(runsTools).isNotNull();
    }

    @Test
    void resultsToolsShouldBeRegistered() {
        assertThat(resultsTools).isNotNull();
    }

    @Test
    void sectionsToolsShouldBeRegistered() {
        assertThat(sectionsTools).isNotNull();
    }

    @Test
    void allToolComponentsShouldBeAvailable() {
        // Verify all tool components are properly configured
        assertThat(applicationContext.getBean(CasesTools.class)).isNotNull();
        assertThat(applicationContext.getBean(ProjectsTools.class)).isNotNull();
        assertThat(applicationContext.getBean(RunsTools.class)).isNotNull();
        assertThat(applicationContext.getBean(ResultsTools.class)).isNotNull();
        assertThat(applicationContext.getBean(SectionsTools.class)).isNotNull();
    }

    // ── New: Verify Lucene-based tool discovery infrastructure ──────────────

    @Test
    void internalToolRegistryShouldBeWired() {
        assertThat(internalToolRegistry).isNotNull();
    }

    @Test
    void internalToolRegistryShouldDiscoverAllTools() {
        // All 26 @Component Tools classes with @InternalTool methods should be discovered
        assertThat(internalToolRegistry.getToolNames()).isNotEmpty();
        // Verify some known tools are registered
        assertThat(internalToolRegistry.hasTool("get_case")).isTrue();
        assertThat(internalToolRegistry.hasTool("get_projects")).isTrue();
        assertThat(internalToolRegistry.hasTool("get_runs")).isTrue();
        assertThat(internalToolRegistry.hasTool("add_result")).isTrue();
        assertThat(internalToolRegistry.hasTool("get_sections")).isTrue();
    }

    @Test
    void luceneToolIndexServiceShouldBeWired() {
        assertThat(luceneToolIndexService).isNotNull();
    }

    @Test
    void luceneToolIndexServiceShouldHaveIndexedTools() {
        // The Lucene index should have indexed all tools from the registry
        assertThat(luceneToolIndexService.getIndexedToolCount()).isGreaterThan(0);
        assertThat(luceneToolIndexService.getIndexedToolCount())
                .isEqualTo(internalToolRegistry.getToolNames().size());
    }

    @Test
    void luceneSearchShouldReturnResults() {
        // A search for "test case" should find relevant tools
        var results = luceneToolIndexService.search("test case");
        assertThat(results).isNotEmpty();
    }

    @Test
    void mcpExposedToolsShouldBeWired() {
        assertThat(mcpExposedTools).isNotNull();
    }

    @Test
    void searchToolsShouldReturnResults() {
        // The MCP-exposed search_tools should work end-to-end
        String result = mcpExposedTools.searchTools("get test case");
        assertThat(result).isNotNull();
        assertThat(result).contains("get_case");
    }
}
