package io.github.testrail.mcp.tools.runs;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail test run operations.
 */
@Component
public class RunsTools {

    private static final Logger log = LoggerFactory.getLogger(RunsTools.class);

    private final TestrailApiClient apiClient;

    public RunsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_run",
            description = """
                    Retrieves detailed information about a specific test run by its ID.
                    Returns run name, description, milestone, assignee, configuration, and completion status
                    including pass/fail statistics.
                    
                    **When to use:** Use this tool when you need to check the status of a test run,
                    see test execution progress, review pass/fail statistics, or verify run configuration.
                    
                    **Might lead to:** get_results_for_run (to see all results), add_result (to add a result),
                    close_run (to finalize), update_run (to modify settings).
                    
                    **Example prompts:**
                    - "Show me run R123"
                    - "Get the status of test run 456"
                    - "How many tests passed in run 100?"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('get_run', {runId: 123})",
                    "execute_tool('get_run', {runId: 456})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "run", "status", "progress", "statistics"}
    )
    public TestRun getRun(
            @InternalToolParam(description = "The unique identifier of the test run.")
            Integer runId
    ) {
        log.info("Tool: get_run called with runId={}", runId);
        return apiClient.getRun(runId);
    }

    @InternalTool(
            name = "get_runs",
            description = """
                    Retrieves all test runs for a project with optional filters and pagination support.
                    
                    **When to use:** Use this tool when you need to list all test runs in a project,
                    find a specific run, review testing history, or identify active/completed runs.
                    Filter by completion status, creation time, creator, milestone, or test suite.
                    
                    **Might lead to:** get_run (for detailed info), add_run (to create new),
                    get_results_for_run (to see results).
                    
                    **Example prompts:**
                    - "List all test runs in project 1"
                    - "Show me active runs for project 5"
                    - "Get completed test runs in project 3"
                    - "Find runs created after timestamp 1640000000"
                    - "Show runs for milestone 5 in project 2"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('get_runs', {projectId: 1})",
                    "execute_tool('get_runs', {projectId: 5, isCompleted: false})",
                    "execute_tool('get_runs', {projectId: 3, isCompleted: true, limit: 50})",
                    "execute_tool('get_runs', {projectId: 2, milestoneId: '5'})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "runs", "all", "history", "active", "completed"}
    )
    public List<TestRun> getRuns(
            @InternalToolParam(description = "The ID of the project to retrieve test runs from.")
            Integer projectId,
            @InternalToolParam(description = "Filter by completion status: true for completed runs, false for active runs, null for all", required = false)
            Boolean isCompleted,
            @InternalToolParam(description = "Only return runs created after this UNIX timestamp", required = false)
            Long createdAfter,
            @InternalToolParam(description = "Only return runs created before this UNIX timestamp", required = false)
            Long createdBefore,
            @InternalToolParam(description = "Comma-separated list of creator user IDs to filter by", required = false)
            String createdBy,
            @InternalToolParam(description = "Comma-separated list of milestone IDs to filter by", required = false)
            String milestoneId,
            @InternalToolParam(description = "Comma-separated list of test suite IDs to filter by", required = false)
            String suiteId,
            @InternalToolParam(description = "Maximum number of results to return (1-250, default: 250)", required = false, defaultValue = "250")
            Integer limit,
            @InternalToolParam(description = "Number of results to skip for pagination", required = false, defaultValue = "0")
            Integer offset
    ) {
        log.info("Tool: get_runs called with projectId={}, filters applied", projectId);
        return apiClient.getRuns(projectId, isCompleted, createdAfter, createdBefore, createdBy, milestoneId, suiteId, limit, offset);
    }

    @InternalTool(
            name = "add_run",
            description = """
                    Creates a new test run in TestRail for a project.
                    A test run is an instance of test execution against specific test cases.
                    Can include all cases from a suite or a specific selection.
                    
                    **When to use:** Use this tool when you need to start a new testing cycle,
                    execute tests for a specific build/release, create a regression test run,
                    or set up a test run for a specific milestone.
                    
                    **Might lead to:** add_result (to record results), get_run (to verify creation),
                    get_results_for_run (to monitor progress).
                    
                    **Example prompts:**
                    - "Create a test run for regression testing"
                    - "Start a new run in project 1 with suite 5"
                    - "Add a test run called 'Sprint 10 Testing'"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('add_run', {projectId: 1, name: 'Regression Testing', suiteId: 5})",
                    "execute_tool('add_run', {projectId: 2, name: 'Sprint 10 Testing', milestoneId: 3, includeAll: true})"
            },
            keywords = {"add", "create", "new", "start", "run", "test", "execution", "cycle"}
    )
    public TestRun addRun(
            @InternalToolParam(description = "The ID of the project where the run will be created.")
            Integer projectId,
            @InternalToolParam(description = "Name/title of the test run.")
            String name,
            @InternalToolParam(description = "Description of the test run purpose.", required = false)
            String description,
            @InternalToolParam(description = "Test suite ID (for projects with multiple suites).", required = false)
            Integer suiteId,
            @InternalToolParam(description = "Milestone ID to associate with this run.", required = false)
            Integer milestoneId,
            @InternalToolParam(description = "User ID to assign the run to.", required = false)
            Integer assignedtoId,
            @InternalToolParam(description = "Set to true to include all test cases from the suite. Default is true.", required = false, defaultValue = "true")
            Boolean includeAll,
            @InternalToolParam(description = "Comma-separated list of test case IDs to include (when includeAll is false).", required = false)
            String caseIds
    ) {
        log.info("Tool: add_run called for project={}, name={}", projectId, name);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        if (description != null) data.put("description", description);
        if (suiteId != null) data.put("suite_id", suiteId);
        if (milestoneId != null) data.put("milestone_id", milestoneId);
        if (assignedtoId != null) data.put("assignedto_id", assignedtoId);
        if (includeAll != null) data.put("include_all", includeAll);

        if (caseIds != null && !caseIds.isEmpty()) {
            List<Integer> ids = java.util.Arrays.stream(caseIds.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();
            data.put("case_ids", ids);
            data.put("include_all", false);
        }

        return apiClient.addRun(projectId, data);
    }

    @InternalTool(
            name = "update_run",
            description = """
                    Updates an existing test run's settings.
                    
                    **When to use:** Use this tool when you need to change a run's name or description,
                    update the milestone association, or change the assigned user.
                    
                    **Might lead to:** get_run (to verify changes).
                    
                    **Example prompts:**
                    - "Rename run 123 to 'Final Regression'"
                    - "Update the description of run 456"
                    - "Assign run 789 to user 5"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('update_run', {runId: 123, name: 'Final Regression'})",
                    "execute_tool('update_run', {runId: 456, assignedtoId: 5})"
            },
            keywords = {"update", "modify", "change", "edit", "revise", "run", "settings"}
    )
    public TestRun updateRun(
            @InternalToolParam(description = "The ID of the test run to update.")
            Integer runId,
            @InternalToolParam(description = "New name for the test run.", required = false)
            String name,
            @InternalToolParam(description = "New description.", required = false)
            String description,
            @InternalToolParam(description = "New milestone ID.", required = false)
            Integer milestoneId,
            @InternalToolParam(description = "New assigned user ID.", required = false)
            Integer assignedtoId
    ) {
        log.info("Tool: update_run called for runId={}", runId);

        Map<String, Object> data = new HashMap<>();
        if (name != null) data.put("name", name);
        if (description != null) data.put("description", description);
        if (milestoneId != null) data.put("milestone_id", milestoneId);
        if (assignedtoId != null) data.put("assignedto_id", assignedtoId);

        return apiClient.updateRun(runId, data);
    }

    @InternalTool(
            name = "close_run",
            description = """
                    Closes an open test run in TestRail.
                    Once closed, no more results can be added. This action marks the test run as complete
                    and locks it for archival purposes.
                    
                    **When to use:** Use this tool when you need to finalize a completed test run,
                    lock a run after all testing is done, or archive test results for a release.
                    
                    **Might lead to:** get_run (to verify closure), add_run (to create a new run for continued testing).
                    
                    **Example prompts:**
                    - "Close test run R123"
                    - "Finalize run 456"
                    - "Mark run R789 as complete"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('close_run', {runId: 123})",
                    "execute_tool('close_run', {runId: 456})"
            },
            keywords = {"close", "finalize", "complete", "lock", "archive", "run", "finish"}
    )
    public TestRun closeRun(
            @InternalToolParam(description = "The ID of the test run to close.")
            Integer runId
    ) {
        log.info("Tool: close_run called for runId={}", runId);
        return apiClient.closeRun(runId);
    }

    @InternalTool(
            name = "delete_run",
            description = """
                    Permanently deletes a test run from TestRail.
                    
                    **WARNING: This action cannot be undone. All test results in the run will be deleted.**
                    
                    **When to use:** Use this tool ONLY when you need to remove a test run created by mistake,
                    or clean up old/obsolete runs. Be careful as this removes all results.
                    
                    **Might lead to:** get_runs (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete test run R123"
                    - "Remove run 456"
                    """,
            category = "test-runs",
            examples = {
                    "execute_tool('delete_run', {runId: 123})",
                    "execute_tool('delete_run', {runId: 456})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "run", "cleanup"}
    )
    public OperationResult deleteRun(
            @InternalToolParam(description = "The ID of the test run to delete. WARNING: This permanently removes the run and all its results.")
            Integer runId
    ) {
        log.warn("Tool: delete_run called for runId={}", runId);
        apiClient.deleteRun(runId);
        return OperationResult.success("Test run R" + runId + " and all its results have been permanently deleted.");
    }
}
