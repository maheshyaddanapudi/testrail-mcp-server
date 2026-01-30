package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail variables for data-driven testing.
 * Variables define the structure/schema for test data (e.g., "username", "password", "browser").
 * Datasets then provide specific values for these variables (e.g., "admin", "pass123", "Chrome").
 * Variables are project-level definitions, while datasets contain the actual test data.
 * Requires TestRail Enterprise license.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VariablesTools {

    private final TestrailApiClient apiClient;

    @Tool(description = """
            Retrieves all variables defined for a project.
            Variables define the schema/structure for test data in datasets (e.g., "username", "password", "browser").
            Each variable has an ID and name - datasets then provide specific values for these variables.
            Returns array of variables with IDs and names.
            Requires TestRail Enterprise license.
            
            **When to use:** Use this tool when you need to view the test data schema for a project,
            discover what data fields are available for datasets, audit variable definitions,
            or prepare for creating new datasets with appropriate variables.
            
            **Might lead to:** add_variable (to define new data fields), get_datasets (to see variable values),
            update_variable (to rename variables).
            
            **Example prompts:**
            - "List all variables in project 12"
            - "Show me test data fields for project 5"
            - "What variables are defined for data-driven testing?"
            """)
    public List<Variable> getVariables(int projectId) {
        log.info("Getting variables for project {}", projectId);
        return apiClient.getVariables(projectId);
    }

    @Tool(description = """
            Creates a new variable (test data field) in a project.
            Variables define the schema for datasets - they specify what data fields can be used.
            Example: Create "browser", "os", "version" variables, then datasets provide values like "Chrome", "Windows", "10".
            Variable names must be unique within the project.
            Requires TestRail Enterprise license.
            
            Required data fields:
            - **name** (string): Variable name (e.g., "username", "password", "environment", "browser")
            
            **When to use:** Use this tool when you need to define new test data fields,
            extend the data schema for parameterized testing, add data dimensions for cross-platform testing,
            or establish variable structures before creating datasets.
            
            **Might lead to:** get_variables (to verify creation), add_dataset (to create datasets with this variable),
            update_variable (to rename if needed).
            
            **Example prompts:**
            - "Create a variable called 'browser' in project 12"
            - "Add a new variable 'environment' for test data"
            - "Define a 'user_role' variable for project 5"
            """)
    public Variable addVariable(int projectId, Map<String, Object> variable) {
        log.info("Adding variable to project {}", projectId);
        return apiClient.addVariable(projectId, variable);
    }

    @Tool(description = """
            Updates an existing variable's name.
            Changes affect the variable definition across all datasets - the schema is updated everywhere.
            Variable names must be unique within the project.
            Requires TestRail Enterprise license.
            
            Updatable fields:
            - **name** (string): New variable name
            
            **When to use:** Use this tool when you need to rename variables for clarity,
            fix typos in variable names, standardize variable naming conventions,
            or update variable definitions to reflect project changes. Changes propagate to all datasets.
            
            **Might lead to:** get_variables (to verify update), get_datasets (to see updated schema).
            
            **Example prompts:**
            - "Rename variable 611 to 'username'"
            - "Update variable 'd' to 'device_type'"
            - "Change variable 612 name to 'operating_system'"
            """)
    public Variable updateVariable(int variableId, Map<String, Object> variable) {
        log.info("Updating variable {}", variableId);
        return apiClient.updateVariable(variableId, variable);
    }

    @Tool(description = """
            Permanently deletes a variable and all its values from all datasets.
            **WARNING: This removes the variable definition AND all corresponding data values.**
            All datasets will lose this data field and its values.
            This action cannot be undone.
            Requires TestRail Enterprise license.
            
            **When to use:** Use this tool ONLY when a variable is obsolete and no longer needed,
            after verifying no active test automation depends on it, or when consolidating duplicate variables.
            Always check variable usage across datasets before deletion.
            
            **Might lead to:** get_variables (to verify deletion), get_datasets (to check impact on datasets).
            
            **Example prompts:**
            - "Delete variable 613"
            - "Remove the obsolete 'old_browser' variable"
            - "Permanently delete variable 'f'"
            """)
    public void deleteVariable(int variableId) {
        log.info("Deleting variable {}", variableId);
        apiClient.deleteVariable(variableId);
    }
}
