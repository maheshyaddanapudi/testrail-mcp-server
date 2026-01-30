package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.SharedStep;
import io.github.testrail.mcp.model.SharedStepHistory;
import org.springframework.ai.tool.annotation.Tool;
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
    private final ObjectMapper objectMapper;

    public SharedStepsTools(TestrailApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
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
            """)
    public String getSharedStep(int sharedStepId) throws JsonProcessingException {
        SharedStep sharedStep = apiClient.getSharedStep(sharedStepId);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = """
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
            """)
    public String getSharedStepHistory(int sharedStepId) throws JsonProcessingException {
        List<SharedStepHistory> history = apiClient.getSharedStepHistory(sharedStepId);
        return objectMapper.writeValueAsString(history);
    }

    @Tool(description = """
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
            """)
    public String getSharedSteps(int projectId) throws JsonProcessingException {
        List<SharedStep> sharedSteps = apiClient.getSharedSteps(projectId);
        return objectMapper.writeValueAsString(sharedSteps);
    }

    @Tool(description = """
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
            """)
    public String addSharedStep(int projectId, String data) throws JsonProcessingException {
        Map<String, Object> dataMap = objectMapper.readValue(data, Map.class);
        SharedStep sharedStep = apiClient.addSharedStep(projectId, dataMap);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = """
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
            """)
    public String updateSharedStep(int sharedStepId, String data) throws JsonProcessingException {
        Map<String, Object> dataMap = objectMapper.readValue(data, Map.class);
        SharedStep sharedStep = apiClient.updateSharedStep(sharedStepId, dataMap);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = """
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
            """)
    public String deleteSharedStep(int sharedStepId) {
        apiClient.deleteSharedStep(sharedStepId);
        return "{\"success\": true, \"message\": \"Shared step deleted successfully\"}";
    }
}
