package io.github.testrail.mcp.tools.plans;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail test plan operations.
 * Test plans allow grouping multiple test runs together for complex testing scenarios.
 */
@Component
public class PlansTools {

    private static final Logger log = LoggerFactory.getLogger(PlansTools.class);

    private final TestrailApiClient apiClient;

    public PlansTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = """
            Retrieves detailed information about a specific test plan by its ID.
            A test plan groups multiple test runs together and can automatically generate runs
            for various browser, OS, or other configurations.
            Returns plan name, description, milestone, entries (groups of runs), and completion status
            including pass/fail statistics across all runs.

            **When to use:** Use this tool when you need to check the status of a test plan,
            see all test runs within it, review overall pass/fail statistics, or verify plan configuration.

            **Might lead to:** get_runs (to see individual runs), get_tests (to see tests in runs),
            close_plan (to finalize), update_plan (to modify settings).

            **Example prompts:**
            - "Show me plan P123"
            - "Get the status of test plan 456"
            - "How many tests passed in plan 100?"
            - "Show all runs in plan 789"
            """)
    public TestPlan getPlan(
            @ToolParam(description = "The unique identifier of the test plan.")
            Integer planId
    ) {
        log.info("Tool: get_plan called with planId={}", planId);
        return apiClient.getPlan(planId);
    }

    @Tool(description = """
            Retrieves all test plans for a project with optional filters and pagination support.
            Test plans organize multiple test runs for complex testing scenarios like sprint testing
            or testing across multiple configurations.

            **When to use:** Use this tool when you need to list all test plans in a project,
            find a specific plan, review testing history, or identify active/completed plans.
            Filter by completion status, creation time, creator, or milestone.

            **Filters available:**
            - **is_completed**: 1 for completed plans only, 0 for active plans only
            - **created_after/before**: Filter by creation timestamp
            - **created_by**: Filter by creator user IDs (comma-separated)
            - **milestone_id**: Filter by milestone IDs (comma-separated)
            - **limit/offset**: Paginate through large result sets

            **Might lead to:** get_plan (for detailed info), add_plan (to create new),
            get_runs (to see runs in plans).

            **Example prompts:**
            - "List all test plans in project 1"
            - "Show me active plans for project 5"
            - "Get completed test plans in project 3"
            - "Find plans created after timestamp 1640000000"
            - "Show plans for milestone 5 in project 2"
            """)
    public List<TestPlan> getPlans(
            @ToolParam(description = "The ID of the project to retrieve test plans from.")
            Integer projectId,
            @ToolParam(description = "Only return plans created after this UNIX timestamp", required = false)
            Long createdAfter,
            @ToolParam(description = "Only return plans created before this UNIX timestamp", required = false)
            Long createdBefore,
            @ToolParam(description = "Comma-separated list of creator user IDs to filter by", required = false)
            String createdBy,
            @ToolParam(description = "Filter by completion status: 1 for completed, 0 for active, null for all", required = false)
            Integer isCompleted,
            @ToolParam(description = "Comma-separated list of milestone IDs to filter by", required = false)
            String milestoneId,
            @ToolParam(description = "Maximum number of results to return (1-250, default: 250)", required = false)
            Integer limit,
            @ToolParam(description = "Number of results to skip for pagination", required = false)
            Integer offset
    ) {
        log.info("Tool: get_plans called with projectId={}, filters applied", projectId);
        return apiClient.getPlans(projectId, createdAfter, createdBefore, createdBy, isCompleted, milestoneId, limit, offset);
    }

    @Tool(description = """
            Creates a new test plan in TestRail for a project.
            A test plan groups multiple test runs together and can automatically generate runs
            for various configurations (browsers, OS, etc.).

            **When to use:** Use this tool when you need to start a complex testing cycle,
            organize multiple test runs together, or set up testing across different configurations.

            **Required fields:**
            - **name**: The name of the test plan

            **Optional fields:**
            - **description**: Detailed description of the plan
            - **milestone_id**: Link to a milestone
            - **start_on/due_on**: Start and due dates (UNIX timestamps)
            - **entries**: Array of test run configurations (see examples)

            **Might lead to:** get_plan (to verify creation), add_plan_entry (to add more runs),
            get_tests (to see tests in runs).

            **Example prompts:**
            - "Create a test plan named 'Sprint 23 Testing' in project 1"
            - "Add a new plan for milestone 5 in project 2"
            - "Create plan 'Browser Testing' with runs for Chrome and Firefox"

            **Example with entries:**
            ```json
            {
              "name": "System test",
              "entries": [
                {
                  "suite_id": 1,
                  "name": "Custom run name",
                  "assignedto_id": 1
                }
              ]
            }
            ```
            """)
    public TestPlan addPlan(
            @ToolParam(description = "The ID of the project to add the test plan to.")
            Integer projectId,
            @ToolParam(description = "The name of the test plan.")
            String name,
            @ToolParam(description = "Optional description of the test plan", required = false)
            String description,
            @ToolParam(description = "Optional milestone ID to link to the plan", required = false)
            Integer milestoneId,
            @ToolParam(description = "Optional start date as UNIX timestamp", required = false)
            Long startOn,
            @ToolParam(description = "Optional due date as UNIX timestamp", required = false)
            Long dueOn
    ) {
        log.info("Tool: add_plan called with projectId={}, name={}", projectId, name);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        if (description != null) data.put("description", description);
        if (milestoneId != null) data.put("milestone_id", milestoneId);
        if (startOn != null) data.put("start_on", startOn);
        if (dueOn != null) data.put("due_on", dueOn);

        return apiClient.addPlan(projectId, data);
    }

    @Tool(description = """
            Updates an existing test plan in TestRail.
            Allows modifying plan properties like name, description, milestone, or dates.
            Supports partial updates - only provide fields you want to change.

            **When to use:** Use this tool when you need to modify plan details,
            change the milestone, update dates, or revise the description.

            **Updatable fields:**
            - **name**: The plan name
            - **description**: Plan description
            - **milestone_id**: Associated milestone
            - **start_on/due_on**: Start and due dates (UNIX timestamps)

            **Might lead to:** get_plan (to verify update), close_plan (to finalize),
            add_plan_entry (to add runs).

            **Example prompts:**
            - "Update plan 123 name to 'Sprint 24 Testing'"
            - "Change plan 456 milestone to 5"
            - "Set plan 789 due date to timestamp 1640000000"
            """)
    public TestPlan updatePlan(
            @ToolParam(description = "The ID of the test plan to update.")
            Integer planId,
            @ToolParam(description = "Optional new name for the plan", required = false)
            String name,
            @ToolParam(description = "Optional new description", required = false)
            String description,
            @ToolParam(description = "Optional new milestone ID", required = false)
            Integer milestoneId,
            @ToolParam(description = "Optional new start date as UNIX timestamp", required = false)
            Long startOn,
            @ToolParam(description = "Optional new due date as UNIX timestamp", required = false)
            Long dueOn
    ) {
        log.info("Tool: update_plan called with planId={}", planId);

        Map<String, Object> data = new HashMap<>();
        if (name != null) data.put("name", name);
        if (description != null) data.put("description", description);
        if (milestoneId != null) data.put("milestone_id", milestoneId);
        if (startOn != null) data.put("start_on", startOn);
        if (dueOn != null) data.put("due_on", dueOn);

        return apiClient.updatePlan(planId, data);
    }

    @Tool(description = """
            Closes and archives a test plan and all its test runs.
            This action cannot be undone. Closed plans are marked as completed
            and their runs are archived.

            **When to use:** Use this tool when testing is complete and you want to
            finalize the plan, archive all runs, and mark everything as done.

            **Warning:** This action is permanent and cannot be undone.

            **Might lead to:** get_plan (to verify closure), get_plans (to see other plans).

            **Example prompts:**
            - "Close test plan 123"
            - "Finalize plan 456"
            - "Archive plan 789 and all its runs"
            """)
    public TestPlan closePlan(
            @ToolParam(description = "The ID of the test plan to close.")
            Integer planId
    ) {
        log.info("Tool: close_plan called with planId={}", planId);
        return apiClient.closePlan(planId);
    }

    @Tool(description = """
            Deletes a test plan and all its test runs and results permanently.
            This action cannot be undone. All data associated with the plan will be lost.

            **When to use:** Use this tool only when you need to permanently remove a plan
            and all its data. This is typically used for test plans created by mistake
            or plans that are no longer needed.

            **Warning:** This action is permanent and cannot be undone. All runs and results
            in the plan will be deleted.

            **Might lead to:** get_plans (to verify deletion).

            **Example prompts:**
            - "Delete test plan 123"
            - "Remove plan 456 permanently"
            - "Delete plan 789 and all its data"
            """)
    public OperationResult deletePlan(
            @ToolParam(description = "The ID of the test plan to delete.")
            Integer planId
    ) {
        log.info("Tool: delete_plan called with planId={}", planId);
        try {
            apiClient.deletePlan(planId);
            return OperationResult.success("Test plan " + planId + " deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete plan: {}", planId, e);
            return OperationResult.failure("Failed to delete plan: " + e.getMessage());
        }
    }
}
