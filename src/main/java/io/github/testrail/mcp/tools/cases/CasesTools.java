package io.github.testrail.mcp.tools.cases;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail test case operations.
 */
@Component
public class CasesTools {

    private static final Logger log = LoggerFactory.getLogger(CasesTools.class);

    private final TestrailApiClient apiClient;

    public CasesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_case",
            description = """
                    Retrieves a specific test case from TestRail by its unique ID.
                    Returns complete details including title, steps, expected results, preconditions, priority, type, and custom fields.
                    
                    **When to use:** Use this tool when you need to view the details of a specific test case,
                    check the current state before making modifications, understand the test steps and expected results,
                    or verify a test case exists before referencing it in a test run.
                    
                    **Might lead to:** update_test_case (to modify), clone_test_case (to copy with changes),
                    add_run (to include in a test run), get_test_cases (to see related cases).
                    
                    **Example prompts:**
                    - "Show me test case C123"
                    - "Get the details of case 456"
                    - "What are the steps in test case C789?"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('get_case', {caseId: 123})",
                    "execute_tool('get_case', {caseId: 456})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "case", "details", "read"}
    )
    public TestCase getTestCase(
            @InternalToolParam(description = "The unique identifier of the test case. Can be provided with or without the 'C' prefix (e.g., 123 or C123).")
            Integer caseId
    ) {
        log.info("Tool: get_test_case called with caseId={}", caseId);
        return apiClient.getCase(caseId);
    }

    @InternalTool(
            name = "get_cases",
            description = """
                    Retrieves all test cases for a project, optionally filtered by test suite and section.
                    Returns a list of test cases with their IDs, titles, and key attributes.
                    Supports pagination with offset and limit parameters.
                    
                    **When to use:** Use this tool when you need to list all test cases in a project or suite,
                    search for test cases by browsing, get an overview of test coverage, or find test cases to include in a new test run.
                    
                    **Might lead to:** get_test_case (for full details), add_test_case (to create new),
                    add_run (to create a test run with selected cases).
                    
                    **Example prompts:**
                    - "List all test cases in project 1"
                    - "Show me the test cases in suite 5"
                    - "Get test cases from section 10 in project 3"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('get_cases', {projectId: 1})",
                    "execute_tool('get_cases', {projectId: 1, suiteId: 5})",
                    "execute_tool('get_cases', {projectId: 3, sectionId: 10, limit: 50})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "cases", "all", "search"}
    )
    public List<TestCase> getTestCases(
            @InternalToolParam(description = "The ID of the project to retrieve test cases from.")
            Integer projectId,
            @InternalToolParam(description = "The ID of the test suite (required for projects using multiple suites mode).", required = false)
            Integer suiteId,
            @InternalToolParam(description = "Filter by section ID to get cases only from a specific section.", required = false)
            Integer sectionId,
            @InternalToolParam(description = "Maximum number of results to return (1-250, default: 250).", required = false, defaultValue = "250")
            Integer limit,
            @InternalToolParam(description = "Number of results to skip for pagination. Use with limit for paging through large result sets.", required = false, defaultValue = "0")
            Integer offset
    ) {
        log.info("Tool: get_test_cases called with projectId={}, suiteId={}, sectionId={}", projectId, suiteId, sectionId);
        return apiClient.getCases(projectId, suiteId, sectionId, limit, offset);
    }

    @InternalTool(
            name = "add_case",
            description = """
                    Creates a new test case in TestRail within a specified section.
                    Allows setting title, steps, expected results, preconditions, priority, type, and references.
                    
                    **When to use:** Use this tool when you need to create a new test case from scratch,
                    document a new test scenario, add test coverage for new features,
                    or create a test case based on user requirements or bug reports.
                    
                    **Might lead to:** get_test_case (to verify creation), update_test_case (to modify),
                    add_run (to test the new case).
                    
                    **Example prompts:**
                    - "Create a new test case for login validation"
                    - "Add a test case to section 5 for password reset"
                    - "Create test case: verify user can logout successfully"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('add_case', {sectionId: 5, title: 'Verify login with valid credentials'})",
                    "execute_tool('add_case', {sectionId: 10, title: 'Password reset flow', steps: '1. Click forgot password\\n2. Enter email', expectedResult: 'Reset email sent'})"
            },
            keywords = {"add", "create", "new", "case", "test", "document", "write"}
    )
    public TestCase addTestCase(
            @InternalToolParam(description = "The ID of the section where the test case will be created. Use get_sections to find available sections.")
            Integer sectionId,
            @InternalToolParam(description = "The title/name of the test case. Should be descriptive and follow your team's naming conventions.")
            String title,
            @InternalToolParam(description = "The test steps in plain text or TestRail's step format.", required = false)
            String steps,
            @InternalToolParam(description = "The expected result(s) of the test. Describe what should happen when the test passes.", required = false)
            String expectedResult,
            @InternalToolParam(description = "Any preconditions that must be met before executing this test.", required = false)
            String preconditions,
            @InternalToolParam(description = "Priority ID: 1=Low, 2=Medium, 3=High, 4=Critical", required = false)
            Integer priorityId,
            @InternalToolParam(description = "Test type ID (e.g., 1=Acceptance, 2=Accessibility, 3=Automated, etc.).", required = false)
            Integer typeId,
            @InternalToolParam(description = "Comma-separated list of references (e.g., JIRA tickets: 'PROJ-123, PROJ-456').", required = false)
            String refs
    ) {
        log.info("Tool: add_test_case called for section={}, title={}", sectionId, title);

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        if (steps != null) data.put("custom_steps", steps);
        if (expectedResult != null) data.put("custom_expected", expectedResult);
        if (preconditions != null) data.put("custom_preconds", preconditions);
        if (priorityId != null) data.put("priority_id", priorityId);
        if (typeId != null) data.put("type_id", typeId);
        if (refs != null) data.put("refs", refs);

        return apiClient.addCase(sectionId, data);
    }

    @InternalTool(
            name = "update_case",
            description = """
                    Updates an existing test case in TestRail.
                    Can modify title, steps, expected results, preconditions, priority, type, and custom fields.
                    Only specified fields are updated; others remain unchanged.
                    
                    **When to use:** Use this tool when you need to modify test case steps or expected results,
                    update test case title or description, change priority or type, or add references to tickets.
                    
                    **Might lead to:** get_test_case (to verify updates), add_result (to record test execution after update).
                    
                    **Example prompts:**
                    - "Update test case C123 to change the expected result"
                    - "Modify the steps in case 456"
                    - "Change the priority of test case C789 to High"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('update_case', {caseId: 123, expectedResult: 'User is redirected to dashboard'})",
                    "execute_tool('update_case', {caseId: 456, priorityId: 3})"
            },
            keywords = {"update", "modify", "change", "edit", "revise", "case", "test"}
    )
    public TestCase updateTestCase(
            @InternalToolParam(description = "The ID of the test case to update.")
            Integer caseId,
            @InternalToolParam(description = "New title for the test case (leave null to keep existing).", required = false)
            String title,
            @InternalToolParam(description = "Updated test steps.", required = false)
            String steps,
            @InternalToolParam(description = "Updated expected results.", required = false)
            String expectedResult,
            @InternalToolParam(description = "Updated preconditions.", required = false)
            String preconditions,
            @InternalToolParam(description = "New priority ID: 1=Low, 2=Medium, 3=High, 4=Critical", required = false)
            Integer priorityId,
            @InternalToolParam(description = "New type ID.", required = false)
            Integer typeId,
            @InternalToolParam(description = "Updated references.", required = false)
            String refs
    ) {
        log.info("Tool: update_test_case called for caseId={}", caseId);

        Map<String, Object> data = new HashMap<>();
        if (title != null) data.put("title", title);
        if (steps != null) data.put("custom_steps", steps);
        if (expectedResult != null) data.put("custom_expected", expectedResult);
        if (preconditions != null) data.put("custom_preconds", preconditions);
        if (priorityId != null) data.put("priority_id", priorityId);
        if (typeId != null) data.put("type_id", typeId);
        if (refs != null) data.put("refs", refs);

        return apiClient.updateCase(caseId, data);
    }

    @InternalTool(
            name = "delete_case",
            description = """
                    Permanently deletes a test case from TestRail.
                    
                    **WARNING: This action cannot be undone. The test case and all its history will be removed.**
                    
                    **When to use:** Use this tool ONLY when you need to remove obsolete or duplicate test cases,
                    clean up test cases created by mistake, or remove test cases that are no longer relevant.
                    CAUTION: Prefer archiving over deletion when possible.
                    
                    **Might lead to:** get_test_cases (to verify deletion), add_test_case (to create replacement if needed).
                    
                    **Example prompts:**
                    - "Delete test case C123"
                    - "Remove case 456 from TestRail"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('delete_case', {caseId: 123})",
                    "execute_tool('delete_case', {caseId: 456})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "case", "cleanup"}
    )
    public OperationResult deleteTestCase(
            @InternalToolParam(description = "The ID of the test case to delete. WARNING: This permanently removes the test case and cannot be undone.")
            Integer caseId
    ) {
        log.warn("Tool: delete_test_case called for caseId={}", caseId);
        apiClient.deleteCase(caseId);
        return OperationResult.success("Test case C" + caseId + " has been permanently deleted.");
    }

    @InternalTool(
            name = "copy_cases_to_section",
            description = """
                    Creates a copy of an existing test case, optionally in a different section.
                    The cloned case can have modifications applied during cloning (new title, updated steps, etc.).
                    
                    **When to use:** Use this tool when you need to create a similar test case with minor variations,
                    copy a test case to a different section, create a template-based test case,
                    or duplicate a test case for different test data scenarios.
                    
                    **Might lead to:** get_test_case (to view cloned case), update_test_case (for additional modifications),
                    add_run (to include cloned case in a test run).
                    
                    **Example prompts:**
                    - "Clone test case C123"
                    - "Copy case 456 to section 10"
                    - "Duplicate test case C789 and change the title"
                    """,
            category = "test-cases",
            examples = {
                    "execute_tool('copy_cases_to_section', {sourceCaseId: 123})",
                    "execute_tool('copy_cases_to_section', {sourceCaseId: 456, targetSectionId: 10, newTitle: 'Modified test case'})"
            },
            keywords = {"clone", "copy", "duplicate", "replicate", "case", "template"}
    )
    public TestCase cloneTestCase(
            @InternalToolParam(description = "The ID of the test case to clone.")
            Integer sourceCaseId,
            @InternalToolParam(description = "Target section ID. Leave null to clone in the same section as the original.", required = false)
            Integer targetSectionId,
            @InternalToolParam(description = "New title for the cloned case. Leave null to use 'Copy of [original title]'.", required = false)
            String newTitle,
            @InternalToolParam(description = "Updated steps for the cloned case. Leave null to keep original steps.", required = false)
            String newSteps,
            @InternalToolParam(description = "Updated expected result. Leave null to keep original.", required = false)
            String newExpectedResult
    ) {
        log.info("Tool: clone_test_case called for sourceCaseId={}, targetSectionId={}", sourceCaseId, targetSectionId);

        // Get the original test case
        TestCase original = apiClient.getCase(sourceCaseId);

        // Determine target section
        Integer sectionId = targetSectionId != null ? targetSectionId : original.getSectionId();

        // Build the clone data
        Map<String, Object> data = new HashMap<>();
        data.put("title", newTitle != null ? newTitle : "Copy of " + original.getTitle());

        // Use new values if provided, otherwise use original
        data.put("custom_steps", newSteps != null ? newSteps : original.getSteps());
        data.put("custom_expected", newExpectedResult != null ? newExpectedResult : original.getExpectedResult());

        // Copy other fields from original
        if (original.getPreconditions() != null) {
            data.put("custom_preconds", original.getPreconditions());
        }
        if (original.getPriorityId() != null) {
            data.put("priority_id", original.getPriorityId());
        }
        if (original.getTypeId() != null) {
            data.put("type_id", original.getTypeId());
        }
        if (original.getRefs() != null) {
            data.put("refs", original.getRefs());
        }

        return apiClient.addCase(sectionId, data);
    }
}
