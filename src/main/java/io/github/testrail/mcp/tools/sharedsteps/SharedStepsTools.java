package io.github.testrail.mcp.tools.sharedsteps;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.SharedStep;
import io.github.testrail.mcp.model.SharedStepHistory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail shared steps.
 * Shared steps allow reusing common test step sequences across multiple test cases.
 * Requires TestRail 7.0 or later.
 */
@Component
public class SharedStepsTools {

    private final TestrailApiClient apiClient;

    public SharedStepsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_shared_step",
            description = """
                    Retrieves a single shared step set by its ID.
                    Shared steps are reusable test step sequences (e.g., "Login", "Logout") used across multiple test cases.
                    Returns step details including title, steps array, and list of test cases using this shared step.
                    Requires TestRail 7.0 or later.
                    
                    **When to use:** Use this tool when you need to view details of a specific shared step set,
                    understand which test cases use a shared step, review step content before reusing,
                    or audit shared step definitions.
                    
                    **Might lead to:** update_shared_step (to modify steps), get_shared_step_history (to view changes),
                    add_case (to reference shared steps in new cases).
                    
                    **Example prompts:**
                    - "Show me shared step 1"
                    - "Get details for shared step 'Login'"
                    - "Which test cases use shared step 5?"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('get_shared_step', {sharedStepId: 1})",
                    "execute_tool('get_shared_step', {sharedStepId: 5})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "shared", "step", "steps", "reusable"}
    )
    public SharedStep getSharedStep(
            @InternalToolParam(description = "The ID of the shared step to retrieve")
            int sharedStepId
    ) {
        SharedStep sharedStep = apiClient.getSharedStep(sharedStepId);
        return sharedStep;
    }

    @InternalTool(
            name = "get_shared_step_history",
            description = """
                    Retrieves the complete change history for a shared step set.
                    Returns a timeline of all modifications including who changed what and when.
                    Useful for auditing, rollback decisions, and understanding step evolution.
                    Requires TestRail 7.3 or later.
                    
                    **When to use:** Use this tool when you need to audit changes to shared steps,
                    understand who modified steps and when, investigate step evolution over time,
                    or prepare for rollback to a previous version.
                    
                    **Might lead to:** update_shared_step (to revert or modify based on history).
                    
                    **Example prompts:**
                    - "Show me the change history for shared step 1"
                    - "Who modified shared step 5 and when?"
                    - "What changes were made to the 'Login' shared step?"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('get_shared_step_history', {sharedStepId: 1})",
                    "execute_tool('get_shared_step_history', {sharedStepId: 5})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "shared", "step", "history", "changes", "audit"}
    )
    public List<SharedStepHistory> getSharedStepHistory(
            @InternalToolParam(description = "The ID of the shared step to retrieve history for")
            int sharedStepId
    ) {
        List<SharedStepHistory> history = apiClient.getSharedStepHistory(sharedStepId);
        return history;
    }

    @InternalTool(
            name = "get_shared_steps",
            description = """
                    Retrieves all shared step sets for a project.
                    Returns a list of reusable step sequences available in the project.
                    Supports filtering by creator, creation/update dates, and references.
                    Supports pagination (limit/offset) - returns up to 250 entries per request.
                    Requires TestRail 7.0 or later.
                    
                    **When to use:** Use this tool when you need to browse available shared steps in a project,
                    discover reusable step sequences before creating test cases, audit shared step inventory,
                    or find shared steps by creator or date.
                    
                    **Might lead to:** get_shared_step (to view details), add_case (to use shared steps),
                    add_shared_step (to create new shared steps).
                    
                    **Example prompts:**
                    - "List all shared steps in project 1"
                    - "Show me shared steps created by user 5"
                    - "What shared steps are available for reuse?"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('get_shared_steps', {projectId: 1})",
                    "execute_tool('get_shared_steps', {projectId: 5})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "shared", "steps", "reusable", "all"}
    )
    public List<SharedStep> getSharedSteps(
            @InternalToolParam(description = "The ID of the project to retrieve shared steps for")
            int projectId
    ) {
        List<SharedStep> sharedSteps = apiClient.getSharedSteps(projectId);
        return sharedSteps;
    }

    @InternalTool(
            name = "add_shared_step",
            description = """
                    Creates a new shared step set in a project.
                    Shared steps allow defining common test sequences once and reusing across multiple test cases.
                    Requires permission to add test cases in the project.
                    Requires TestRail 7.0 or later.
                    
                    Required data fields:
                    - **title** (string): Name for the shared step set (e.g., "Login Flow")
                    - **custom_steps_separated** (array): Array of step objects with fields:
                      - content: Step description
                      - expected: Expected result
                      - additional_info: Extra details (optional)
                      - refs: Reference IDs (optional)
                    
                    **When to use:** Use this tool when you need to create reusable test step sequences,
                    standardize common workflows (login, logout, setup), reduce test case maintenance effort,
                    or establish step libraries for consistent testing.
                    
                    **Might lead to:** add_case or update_case (to reference the new shared step),
                    get_shared_step (to verify creation).
                    
                    **Example prompts:**
                    - "Create a shared step for login flow in project 1"
                    - "Add a new shared step called 'Database Setup' with 3 steps"
                    - "Define a reusable 'Logout' shared step"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('add_shared_step', {projectId: 1, data: '{\"title\": \"Login Flow\", \"custom_steps_separated\": [{\"content\": \"Enter username\", \"expected\": \"Username field populated\"}]}'})"
            },
            keywords = {"add", "create", "new", "shared", "step", "steps", "reusable", "define"}
    )
    public SharedStep addSharedStep(
            @InternalToolParam(description = "The ID of the project to add the shared step to")
            int projectId,
            @InternalToolParam(description = "Map containing shared step data with title and custom_steps_separated")
            @SuppressWarnings("unchecked") Map<String, Object> data
    ) {
        SharedStep sharedStep = apiClient.addSharedStep(projectId, data);
        return sharedStep;
    }

    @InternalTool(
            name = "update_shared_step",
            description = """
                    Updates an existing shared step set.
                    Changes are propagated to all test cases using this shared step.
                    Use with caution as modifications affect multiple test cases simultaneously.
                    Requires TestRail 7.0 or later.
                    
                    Updatable fields:
                    - **title** (string): Change the shared step name
                    - **custom_steps_separated** (array): Modify step sequence
                    
                    **When to use:** Use this tool when you need to fix errors in shared steps,
                    update step sequences to reflect process changes, improve step clarity,
                    or maintain shared step accuracy. Always verify impact on dependent test cases first.
                    
                    **Might lead to:** get_shared_step (to verify update), get_shared_step_history (to track changes).
                    
                    **Example prompts:**
                    - "Update shared step 1 to fix typo in step 2"
                    - "Modify the 'Login' shared step to add password validation"
                    - "Change the title of shared step 5 to 'Enhanced Login Flow'"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('update_shared_step', {sharedStepId: 1, data: '{\"title\": \"Enhanced Login Flow\"}'})",
                    "execute_tool('update_shared_step', {sharedStepId: 5, data: '{\"custom_steps_separated\": [{\"content\": \"Updated step\", \"expected\": \"Updated result\"}]}'})"
            },
            keywords = {"update", "modify", "change", "edit", "revise", "shared", "step", "steps"}
    )
    public SharedStep updateSharedStep(
            @InternalToolParam(description = "The ID of the shared step to update")
            int sharedStepId,
            @InternalToolParam(description = "JSON string containing updated shared step data")
            @SuppressWarnings("unchecked") Map<String, Object> data
    ) {
        SharedStep sharedStep = apiClient.updateSharedStep(sharedStepId, data);
        return sharedStep;
    }

    @InternalTool(
            name = "delete_shared_step",
            description = """
                    Permanently deletes a shared step set.
                    **WARNING: This removes the shared step from all test cases using it.**
                    Test cases referencing this shared step will have the reference removed (not the cases themselves).
                    This action cannot be undone.
                    Requires TestRail 7.0 or later.
                    
                    **When to use:** Use this tool ONLY when a shared step is obsolete and no longer needed,
                    after verifying no active test cases depend on it, or when consolidating duplicate shared steps.
                    Always check which test cases use the shared step before deletion.
                    
                    **Might lead to:** get_shared_steps (to verify deletion), update_case (to fix affected test cases).
                    
                    **Example prompts:**
                    - "Delete shared step 10"
                    - "Remove the obsolete 'Old Login' shared step"
                    - "Permanently delete shared step 25"
                    """,
            category = "shared-steps",
            examples = {
                    "execute_tool('delete_shared_step', {sharedStepId: 10})",
                    "execute_tool('delete_shared_step', {sharedStepId: 25})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "shared", "step", "steps", "cleanup"}
    )
    public String deleteSharedStep(
            @InternalToolParam(description = "The ID of the shared step to delete")
            int sharedStepId
    ) {
        apiClient.deleteSharedStep(sharedStepId);
        return "{\"success\": true, \"message\": \"Shared step deleted successfully\"}";
    }
}
