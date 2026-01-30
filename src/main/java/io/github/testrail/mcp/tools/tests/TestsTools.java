package io.github.testrail.mcp.tools.tests;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tools for TestRail test operations.
 * Tests are individual instances of test cases added to specific test runs.
 */
@Component
public class TestsTools {

    private static final Logger log = LoggerFactory.getLogger(TestsTools.class);

    private final TestrailApiClient apiClient;

    public TestsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_test",
            description = """
                    Retrieves detailed information about a specific test by its ID.
                    A test is an individual instance of a test case within a test run.
                    Returns test title, status, assigned user, priority, estimate, and custom fields.
                    
                    **When to use:** Use this tool when you need to check the current status of a specific test,
                    see who it's assigned to, review test steps, or get detailed test information.
                    
                    **Might lead to:** add_result (to add a result), get_results (to see result history),
                    get_run (to see the parent run).
                    
                    **Example prompts:**
                    - "Show me test T123"
                    - "Get details of test 456"
                    - "What's the status of test 789?"
                    - "Who is test 100 assigned to?"
                    """,
            category = "tests",
            examples = {
                    "execute_tool('get_test', {testId: 123})",
                    "execute_tool('get_test', {testId: 456})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "test", "status", "details", "assigned"}
    )
    public Test getTest(
            @InternalToolParam(description = "The unique identifier of the test.")
            Integer testId,
            @InternalToolParam(description = "Optional parameter to get additional data", required = false)
            String withData
    ) {
        log.info("Tool: get_test called with testId={}", testId);
        return apiClient.getTest(testId, withData);
    }

    @InternalTool(
            name = "get_tests",
            description = """
                    Retrieves all tests for a specific test run with optional filters and pagination support.
                    Tests are the individual test case instances within a run that need to be executed.
                    
                    **When to use:** Use this tool when you need to see all tests in a run,
                    filter tests by status (e.g., failed, passed, untested), find tests by label,
                    or get a paginated list of tests for large runs.
                    
                    **Filters available:**
                    - **status_id**: Filter by status (e.g., "1,4,5" for passed, retest, failed)
                    - **label_id**: Filter by labels attached to tests
                    - **limit/offset**: Paginate through large result sets
                    
                    **Might lead to:** get_test (for detailed info), add_result (to add results),
                    get_results (to see result history).
                    
                    **Example prompts:**
                    - "List all tests in run 123"
                    - "Show me failed tests in run 456"
                    - "Get untested tests in run 789"
                    - "Find tests with status 4 or 5 in run 100"
                    - "Show first 50 tests in run 200"
                    """,
            category = "tests",
            examples = {
                    "execute_tool('get_tests', {runId: 123})",
                    "execute_tool('get_tests', {runId: 456, statusId: '5'})",
                    "execute_tool('get_tests', {runId: 789, statusId: '3', limit: 50})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "tests", "filter", "status", "failed", "passed"}
    )
    public List<Test> getTests(
            @InternalToolParam(description = "The ID of the test run to retrieve tests from.")
            Integer runId,
            @InternalToolParam(description = "Comma-separated list of status IDs to filter by (e.g., '1,4,5')", required = false)
            String statusId,
            @InternalToolParam(description = "Comma-separated list of label IDs to filter by", required = false)
            String labelId,
            @InternalToolParam(description = "Maximum number of results to return (1-250, default: 250)", required = false, defaultValue = "250")
            Integer limit,
            @InternalToolParam(description = "Number of results to skip for pagination", required = false, defaultValue = "0")
            Integer offset
    ) {
        log.info("Tool: get_tests called with runId={}, filters applied", runId);
        return apiClient.getTests(runId, statusId, labelId, limit, offset);
    }
}
