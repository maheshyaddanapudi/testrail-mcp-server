package io.github.testrail.mcp.tools.results;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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

    @Tool(description = """
            Retrieves test results for a specific test in a test run.
            Returns the history of results for the test with status, comments, and defects.

            **When to use:** Use this tool when you need to see the result history for a specific test,
            check if a test has been executed, review previous execution comments, or track test status changes.

            **Might lead to:** add_result (to add a new result), get_results_for_run (for all run results).

            **Example prompts:**
            - "Show me the results for test T123"
            - "Get the result history for test 456"
            - "What was the last result for test T789?"
            """)
    public List<TestResult> getResults(
            @ToolParam(description = "The ID of the test (note: this is the test ID, not the case ID).")
            Integer testId,
            @ToolParam(description = "Maximum number of results to return.", required = false)
            Integer limit,
            @ToolParam(description = "Number of results to skip for pagination.", required = false)
            Integer offset
    ) {
        log.info("Tool: get_results called with testId={}", testId);
        return apiClient.getResults(testId, limit, offset);
    }

    @Tool(description = """
            Retrieves all test results for a test run.
            Returns results for all tests in the run with their status, comments, and defects.

            **When to use:** Use this tool when you need to see all results for a test run,
            generate a summary report, analyze pass/fail patterns, or export run results.

            **Might lead to:** get_run (for run details), add_result (to add missing results).

            **Example prompts:**
            - "Show me all results for run R123"
            - "Get the results summary for run 456"
            - "What tests failed in run R789?"
            """)
    public List<TestResult> getResultsForRun(
            @ToolParam(description = "The ID of the test run.")
            Integer runId,
            @ToolParam(description = "Maximum number of results to return.", required = false)
            Integer limit,
            @ToolParam(description = "Number of results to skip for pagination.", required = false)
            Integer offset
    ) {
        log.info("Tool: get_results_for_run called with runId={}", runId);
        return apiClient.getResultsForRun(runId, limit, offset);
    }

    @Tool(description = """
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
            """)
    public TestResult addResult(
            @ToolParam(description = "The ID of the test (not case ID) in the test run.")
            Integer testId,
            @ToolParam(description = "Status ID: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @ToolParam(description = "Comment or notes about the test execution.", required = false)
            String comment,
            @ToolParam(description = "Defect references (e.g., 'BUG-123, BUG-456').", required = false)
            String defects,
            @ToolParam(description = "Time spent on test execution (e.g., '30s', '1m 45s', '2h').", required = false)
            String elapsed,
            @ToolParam(description = "Version or build tested.", required = false)
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

    @Tool(description = """
            Adds multiple test results to a test run at once.
            More efficient than adding results one by one when you have multiple results to record.

            **When to use:** Use this tool when you need to add results for multiple tests at once,
            bulk update test statuses, or import results from automated testing.

            **Might lead to:** get_results_for_run (to verify), get_run (to check progress).

            **Example prompts:**
            - "Mark tests T1, T2, T3 as passed"
            - "Add results for multiple tests in run 123"
            """)
    public List<TestResult> addResults(
            @ToolParam(description = "The ID of the test run.")
            Integer runId,
            @ToolParam(description = "Comma-separated list of test IDs.")
            String testIds,
            @ToolParam(description = "Status ID to apply to all tests: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @ToolParam(description = "Optional comment to apply to all results.", required = false)
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

    @Tool(description = """
            Adds test results for specific cases in a test run.
            Uses case IDs instead of test IDs, which is useful when you know the case IDs but not the test IDs.

            **When to use:** Use this tool when you want to add results by case ID rather than test ID,
            which is common when importing results from external systems or automation frameworks.

            **Might lead to:** get_results_for_run (to verify), get_run (to check progress).

            **Example prompts:**
            - "Mark case C123 as passed in run 456"
            - "Add results for cases C1, C2, C3 in run 789"
            """)
    public List<TestResult> addResultsForCases(
            @ToolParam(description = "The ID of the test run.")
            Integer runId,
            @ToolParam(description = "Comma-separated list of case IDs.")
            String caseIds,
            @ToolParam(description = "Status ID to apply: 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed")
            Integer statusId,
            @ToolParam(description = "Optional comment to apply to all results.", required = false)
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
