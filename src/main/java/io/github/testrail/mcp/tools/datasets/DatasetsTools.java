package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail datasets for data-driven testing.
 * Datasets store collections of test data variables (name-value pairs) for parameterized testing.
 * Example: A dataset "Browser Testing" might contain variables: browser=Chrome, os=Windows, version=10.
 * Requires TestRail Enterprise license for create/update/delete operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = """
            Retrieves a single dataset by ID including all its variables.
            Datasets store test data variables (name-value pairs) for data-driven testing.
            Returns dataset name and array of variables with IDs, names, and values.
            
            **When to use:** Use this tool when you need to view a specific dataset's variables,
            retrieve test data for automation scripts, verify dataset values before test execution,
            or audit dataset configurations.
            
            **Might lead to:** update_dataset (to modify variables), get_variables (to see variable definitions).
            
            **Example prompts:**
            - "Show me dataset 183"
            - "Get variables from dataset 'Default'"
            - "What values are in dataset 543?"
            """)
    public Dataset getDataset(int datasetId) {
        log.info("Getting dataset {}", datasetId);
        return apiClient.getDataset(datasetId);
    }

    @Tool(description = """
            Retrieves all datasets for a project including their variables.
            Datasets organize test data for data-driven/parameterized testing across multiple test scenarios.
            Each dataset contains multiple variables (e.g., username, password, browser, environment).
            Returns array of datasets with their variables.
            
            **When to use:** Use this tool when you need to browse available test data sets in a project,
            discover existing data combinations for test execution, audit test data inventory,
            or prepare for data-driven test automation.
            
            **Might lead to:** get_dataset (to view specific dataset details), add_dataset (to create new data sets),
            get_variables (to see variable definitions).
            
            **Example prompts:**
            - "List all datasets in project 1"
            - "Show me test data sets for project 5"
            - "What datasets are available for data-driven testing?"
            """)
    public List<Dataset> getDatasets(int projectId) {
        log.info("Getting datasets for project {}", projectId);
        return apiClient.getDatasets(projectId);
    }

    @Tool(description = """
            Creates a new dataset in a project with specified variables.
            Datasets enable data-driven testing by storing multiple combinations of test data.
            Requires TestRail Enterprise license.
            
            Required data fields:
            - **name** (string): Dataset name (e.g., "Browser Combinations", "User Profiles")
            - **variables** (array): Array of variable objects with name and value
            
            **When to use:** Use this tool when you need to create new test data sets for parameterized testing,
            define data combinations for cross-browser/cross-platform testing, establish test data libraries,
            or support data-driven test automation.
            
            **Might lead to:** get_dataset (to verify creation), add_variable (to add more variables),
            update_dataset (to modify values).
            
            **Example prompts:**
            - "Create a dataset called 'Login Credentials' in project 1"
            - "Add a new dataset for browser testing with Chrome and Firefox"
            - "Define a dataset with user profiles for testing"
            """)
    public Dataset addDataset(int projectId, Map<String, Object> dataset) {
        log.info("Adding dataset to project {}", projectId);
        return apiClient.addDataset(projectId, dataset);
    }

    @Tool(description = """
            Updates an existing dataset's name or variables.
            Changes affect all test cases and test runs using this dataset.
            Requires TestRail Enterprise license.
            
            Updatable fields:
            - **name** (string): Change dataset name
            - **variables** (array): Modify variable values
            
            **When to use:** Use this tool when you need to update test data values,
            fix incorrect dataset configurations, add/remove variables from datasets,
            or maintain test data accuracy. Verify impact on dependent tests before updating.
            
            **Might lead to:** get_dataset (to verify update), get_variables (to check variable definitions).
            
            **Example prompts:**
            - "Update dataset 183 to change browser to Firefox"
            - "Modify the 'Default' dataset variables"
            - "Change dataset 543 name to 'Production Data'"
            """)
    public Dataset updateDataset(int datasetId, Map<String, Object> dataset) {
        log.info("Updating dataset {}", datasetId);
        return apiClient.updateDataset(datasetId, dataset);
    }

    @Tool(description = """
            Permanently deletes a dataset and all its variables.
            **WARNING: This removes the dataset from all test cases and runs using it.**
            Test cases referencing this dataset will lose their data associations.
            This action cannot be undone.
            Requires TestRail Enterprise license.
            
            **When to use:** Use this tool ONLY when a dataset is obsolete and no longer needed,
            after verifying no active test cases depend on it, or when consolidating duplicate datasets.
            Always check dataset usage before deletion.
            
            **Might lead to:** get_datasets (to verify deletion), update_case (to fix affected test cases).
            
            **Example prompts:**
            - "Delete dataset 545"
            - "Remove the obsolete 'Old Browser Data' dataset"
            - "Permanently delete dataset 'BOO'"
            """)
    public void deleteDataset(int datasetId) {
        log.info("Deleting dataset {}", datasetId);
        apiClient.deleteDataset(datasetId);
    }
}
