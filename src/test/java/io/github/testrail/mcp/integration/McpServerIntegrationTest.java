package io.github.testrail.mcp.integration;

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
}
