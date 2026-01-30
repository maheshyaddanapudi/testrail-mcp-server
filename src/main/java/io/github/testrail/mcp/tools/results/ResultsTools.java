package io.github.testrail.mcp.tools.results;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail test result operations.
 */
@Component
public class ResultsTools {

    private static final Logger log = LoggerFactory.getLogger(ResultsTools.class);

    private final TestrailApiClient apiClient;

    public ResultsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_results",
            description = """
                    Retrieves test results for a specific test in a test run with optional filters.
                    Returns the history of results for the test with status, comments, and defects.
                    
                    **When to use:** Use this tool when you need to see the result history for a specific test,
                    check if a test has been executed, review previous execution comments, or track test status changes.
                    Filter by defect ID or status to narrow down results.
                    
                    **Might lead to:** add_result (to add a new result), get_results_for_run (for all run results).
                    
                    **Example prompts:**
                    - "Show me the results for test T123"
                    - "Get failed results for test 456"
                    - "Show results for test 789 with defect TR-123"
                    - "Get the last 10 results for test T456"
                    """,
            category = "test-results",
            examples = {
                    "execute_tool('get_results', {testId: 123})",
                    "execute_tool('get_results', {testId: 456, statusId: '5', limit: 10})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "results", "history", "status", "test"}
    )
    public List<TestResult> getResults(
            @InternalToolParam(description = "The ID of the test (note: this is the test ID, not the case ID)")
            Integer testId,
            @InternalToolParam(description = "Filter by a single Defect ID (e.g. TR-1, 4291, etc.)", required = false)
            String defectsFilter,
            @InternalToolParam(description = "Comma-separated list of status IDs to filter by (e.g. '1,5' for passed and failed)", required = false)
            String statusId,
            @InternalToolParam(description = "Maximum number of results to return (1-250)", required = false, defaultValue = "250")
            Integer limit,
            @InternalToolParam(description = "Number of results to skip for pagination", required = false, defaultValue = "0")
            Integer offset
    ) {
        log.info("Tool: get_results called with testId={}, filters applied", testId);
        return apiClient.getResults(testId, defectsFilter, statusId, limit, offset);
    }

    @InternalTool(
            name = "get_results_for_run",
            description = """
                    Retrieves all test results for a test run with optional filters.
                    Returns results for all tests in the run with their status, comments, and defects.
                    
                    **When to use:** Use this tool when you need to see all results for a test run,
                    generate a summary report, analyze pass/fail patterns, or export run results.
                    Filter by creation time, creator, defect ID, or status to narrow down results.
                    
                    **Might lead to:** get_run (for run details), add_result (to add missing results).
                    
                    **Example prompts:**
                    - "Show me all results for run R123"
                    - "Get failed results for run 456"
                    - "Show results created after timestamp 1640000000 in run 789"
                    - "Get results with defect TR-123 in run R456"
                    - "Show results with status 1 or 5 in run 789"
                    """,
            category = "test-results",
            examples = {
                    "execute_tool('get_results_for_run', {runId: 123})",
                    "execute_tool('get_results_for_run', {runId: 456, statusId: '5'})",
                    "execute_tool('get_results_for_run', {runId: 789, createdAfter: 1640000000})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "results", "run", "all", "report", "summary"}
    )
    public List<TestResult> getResultsForRun(
            @InternalToolParam(description = "The ID of the test run")
            Integer runId,
            @InternalToolParam(description = "Only return results created after this UNIX timestamp", required = false)
            Long createdAfter,
            @InternalToolParam(description = "Only return results created before this UNIX timestamp", required = false)
            Long createdBefore,
            @InternalToolParam(description = "Comma-separated list of creator user IDs to filter by", required = false)
            String createdBy,
            @InternalToolParam(description = "Filter by a single Defect ID (e.g. TR-1, 4291, etc.)", required = false)
            String defectsFilter,
            @InternalToolParam(description = "Comma-separated list of status IDs to filter by (e.g. '1,5' for passed and failed)", required = false)
            String statusId,
            @InternalToolParam(description = "Maximum number of results to return (1-250)", required = false, defaultValue = "250")
            Integer limit,
            @InternalToolParam(description = "Number of results to skip for pagination", required = false, defaultValue = "0")
            Integer offset
    ) {
        log.info("Tool: get_results_for_run called with runId={}, filters applied", runId);
        return apiClient.getResultsForRun(runId, createdAfter, createdBefore, createdBy, defectsFilter, statusId, limit, offset);
    }

    @InternalTool(
            name = "add_result",
            description = """
                    Adds a test result to a specific test in a test run.
                    Records the test status (passed, failed, blocked, etc.) along with optional comment, defects, and elapsed time.
                    
                    Status IDs:
                    - 1 = Passed
                    - 2 = Blocked
                    - 3 = Untested
                    - 4 = Retest
                    - 5 = Failed
                    
                    **When to use:** Use this tool when you need to record a test execution result,
                    mark a test as passed or failed, log a defect found during testing, or document test execution notes.
                    
                    **Might lead to:** get_results (to verify), add_result (for more results), get_run (to check overall progress).
                    
                    **Example prompts:**
                    - "Mark test T123 as passed"
                    - "Record a failure for test 456 with comment 'Login button not responding'"
                    - "Set test T100 as blocked due to environment issues"
                    """,
            category = "test-results",
            examples = {
                    "execute_tool('add_result', {testId: 123, statusId: 1})",
                    "execute_tool('add_result', {testId: 456, statusId: 5, comment: 'Login button not responding', defects: 'BUG-789'})"
            },
            keywords = {"add", "create", "record", "log", "mark", "result", "status", "pass", "fail", "execute"}
    )
    public TestResult addResult(
            @InternalToolParam(description = "The ID of the test (not case ID) in the test run.")
            Integer testId,
            @InternalToolParam(description = "Status ID: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @InternalToolParam(description = "Comment or notes about the test execution.", required = false)
            String comment,
            @InternalToolParam(description = "Defect references (e.g., 'BUG-123, BUG-456').", required = false)
            String defects,
            @InternalToolParam(description = "Time spent on test execution (e.g., '30s', '1m 45s', '2h').", required = false)
            String elapsed,
            @InternalToolParam(description = "Version or build tested.", required = false)
            String version
    ) {
        log.info("Tool: add_result called for testId={}, statusId={}", testId, statusId);

        Map<String, Object> data = new HashMap<>();
        data.put("status_id", statusId);
        if (comment != null) data.put("comment", comment);
        if (defects != null) data.put("defects", defects);
        if (elapsed != null) data.put("elapsed", elapsed);
        if (version != null) data.put("version", version);

        return apiClient.addResult(testId, data);
    }

    @InternalTool(
            name = "add_results",
            description = """
                    Adds multiple test results to a test run at once.
                    More efficient than adding results one by one when you have multiple results to record.
                    
                    **When to use:** Use this tool when you need to add results for multiple tests at once,
                    bulk update test statuses, or import results from automated testing.
                    
                    **Might lead to:** get_results_for_run (to verify), get_run (to check progress).
                    
                    **Example prompts:**
                    - "Mark tests T1, T2, T3 as passed"
                    - "Add results for multiple tests in run 123"
                    """,
            category = "test-results",
            examples = {
                    "execute_tool('add_results', {runId: 123, testIds: '1,2,3', statusId: 1})",
                    "execute_tool('add_results', {runId: 456, testIds: '10,20,30', statusId: 5, comment: 'Automated test failed'})"
            },
            keywords = {"add", "create", "bulk", "multiple", "batch", "results", "import", "automation"}
    )
    public List<TestResult> addResults(
            @InternalToolParam(description = "The ID of the test run.")
            Integer runId,
            @InternalToolParam(description = "Comma-separated list of test IDs.")
            String testIds,
            @InternalToolParam(description = "Status ID to apply to all tests: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @InternalToolParam(description = "Optional comment to apply to all results.", required = false)
            String comment
    ) {
        log.info("Tool: add_results called for runId={}", runId);

        List<Map<String, Object>> results = new ArrayList<>();
        for (String testIdStr : testIds.split(",")) {
            Integer testId = Integer.parseInt(testIdStr.trim());
            Map<String, Object> result = new HashMap<>();
            result.put("test_id", testId);
            result.put("status_id", statusId);
            if (comment != null) result.put("comment", comment);
            results.add(result);
        }

        return apiClient.addResults(runId, results);
    }

    @InternalTool(
            name = "get_results_for_case",
            description = """
                    Adds test results for specific cases in a test run.
                    Uses case IDs instead of test IDs, which is useful when you know the case IDs but not the test IDs.
                    
                    **When to use:** Use this tool when you want to add results by case ID rather than test ID,
                    which is common when importing results from external systems or automation frameworks.
                    
                    **Might lead to:** get_results_for_run (to verify), get_run (to check progress).
                    
                    **Example prompts:**
                    - "Mark case C123 as passed in run 456"
                    - "Add results for cases C1, C2, C3 in run 789"
                    """,
            category = "test-results",
            examples = {
                    "execute_tool('get_results_for_case', {runId: 456, caseIds: '123', statusId: 1})",
                    "execute_tool('get_results_for_case', {runId: 789, caseIds: '1,2,3', statusId: 1})"
            },
            keywords = {"add", "create", "results", "case", "cases", "import", "automation", "by-case"}
    )
    public List<TestResult> addResultsForCases(
            @InternalToolParam(description = "The ID of the test run.")
            Integer runId,
            @InternalToolParam(description = "Comma-separated list of case IDs.")
            String caseIds,
            @InternalToolParam(description = "Status ID to apply: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @InternalToolParam(description = "Optional comment to apply to all results.", required = false)
            String comment
    ) {
        log.info("Tool: add_results_for_cases called for runId={}", runId);

        List<Map<String, Object>> results = new ArrayList<>();
        for (String caseIdStr : caseIds.split(",")) {
            Integer caseId = Integer.parseInt(caseIdStr.trim());
            Map<String, Object> result = new HashMap<>();
            result.put("case_id", caseId);
            result.put("status_id", statusId);
            if (comment != null) result.put("comment", comment);
            results.add(result);
        }

        return apiClient.addResultsForCases(runId, results);
    }
}
