package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Tools for managing Behavior-Driven Development (BDD) scenarios in TestRail.
 * BDD scenarios use Gherkin syntax (Feature, Scenario, Given/When/Then) in .feature files.
 * Enables integration between TestRail and BDD testing frameworks (Cucumber, SpecFlow, Behave).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BDDsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = """
            Exports a BDD scenario from a test case as a .feature file in Gherkin syntax.
            Returns the complete feature file content including Feature, Background, Scenario, Given/When/Then steps, Examples, and Rules.
            Useful for syncing TestRail test cases with BDD testing frameworks (Cucumber, SpecFlow, Behave).
            
            **When to use:** Use this tool when you need to export test cases for BDD test automation,
            sync TestRail scenarios with Cucumber/SpecFlow projects, generate .feature files for CI/CD pipelines,
            or migrate BDD scenarios to external testing frameworks.
            
            **Might lead to:** add_bdd (to re-import modified scenarios), update_case (to modify the test case).
            
            **Example prompts:**
            - "Export BDD scenario from test case 2133"
            - "Get the .feature file for case 100"
            - "Download BDD scenario for test case 'Login validation'"
            """)
    public String getBdd(int caseId) {
        log.info("Getting BDD for case {}", caseId);
        return apiClient.getBdd(caseId);
    }

    @Tool(description = """
            Imports/uploads a BDD scenario to a section from .feature file content in Gherkin syntax.
            Creates a new test case from the BDD scenario with Feature, Scenario, Given/When/Then steps.
            The feature file content should follow Gherkin syntax with Feature, Background, Scenario, Examples, and Rules.
            Returns the created test case with BDD content stored in custom fields.
            
            **When to use:** Use this tool when you need to import BDD scenarios from external tools into TestRail,
            sync Cucumber/SpecFlow .feature files with TestRail, automate test case creation from BDD specifications,
            or migrate BDD scenarios from other systems.
            
            **Might lead to:** get_bdd (to verify import), get_case (to view created test case),
            update_case (to modify the imported scenario).
            
            **Example prompts:**
            - "Import BDD scenario to section 188"
            - "Upload this .feature file content to section 50"
            - "Create test case from BDD scenario in section 'Login Tests'"
            """)
    public TestCase addBdd(int sectionId, String featureFileContent) {
        log.info("Adding BDD to section {}", sectionId);
        return apiClient.addBdd(sectionId, featureFileContent);
    }
}
