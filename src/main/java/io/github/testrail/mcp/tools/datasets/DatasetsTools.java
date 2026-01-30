package io.github.testrail.mcp.tools.datasets;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @InternalTool(
            name = "get_dataset",
            description = """
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
                    """,
            category = "datasets",
            examples = {
                    "execute_tool('get_dataset', {datasetId: 183})",
                    "execute_tool('get_dataset', {datasetId: 543})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "dataset", "variables", "data", "values"}
    )
    public Dataset getDataset(
            @InternalToolParam(description = "The ID of the dataset to retrieve")
            int datasetId
    ) {
        log.info("Getting dataset {}", datasetId);
        return apiClient.getDataset(datasetId);
    }

    @InternalTool(
            name = "get_datasets",
            description = """
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
                    """,
            category = "datasets",
            examples = {
                    "execute_tool('get_datasets', {projectId: 1})",
                    "execute_tool('get_datasets', {projectId: 5})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "datasets", "data", "all", "project"}
    )
    public List<Dataset> getDatasets(
            @InternalToolParam(description = "The ID of the project to retrieve datasets for")
            int projectId
    ) {
        log.info("Getting datasets for project {}", projectId);
        return apiClient.getDatasets(projectId);
    }

    @InternalTool(
            name = "add_dataset",
            description = """
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
                    """,
            category = "datasets",
            examples = {
                    "execute_tool('add_dataset', {projectId: 1, dataset: {name: 'Login Credentials', variables: [{name: 'username', value: 'admin'}]}})",
                    "execute_tool('add_dataset', {projectId: 5, dataset: {name: 'Browser Combinations', variables: [{name: 'browser', value: 'Chrome'}]}})"
            },
            keywords = {"add", "create", "new", "dataset", "data", "variables", "define"}
    )
    public Dataset addDataset(
            @InternalToolParam(description = "The ID of the project to add the dataset to")
            int projectId,
            @InternalToolParam(description = "Dataset map with keys: name (required), variables (required array)")
            Map<String, Object> dataset
    ) {
        log.info("Adding dataset to project {}", projectId);
        return apiClient.addDataset(projectId, dataset);
    }

    @InternalTool(
            name = "update_dataset",
            description = """
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
                    """,
            category = "datasets",
            examples = {
                    "execute_tool('update_dataset', {datasetId: 183, dataset: {variables: [{name: 'browser', value: 'Firefox'}]}})",
                    "execute_tool('update_dataset', {datasetId: 543, dataset: {name: 'Production Data'}})"
            },
            keywords = {"update", "modify", "change", "edit", "dataset", "data", "variables", "values"}
    )
    public Dataset updateDataset(
            @InternalToolParam(description = "The ID of the dataset to update")
            int datasetId,
            @InternalToolParam(description = "Dataset map with keys: name, variables")
            Map<String, Object> dataset
    ) {
        log.info("Updating dataset {}", datasetId);
        return apiClient.updateDataset(datasetId, dataset);
    }

    @InternalTool(
            name = "delete_dataset",
            description = """
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
                    """,
            category = "datasets",
            examples = {
                    "execute_tool('delete_dataset', {datasetId: 545})",
                    "execute_tool('delete_dataset', {datasetId: 550})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "dataset", "data", "cleanup"}
    )
    public void deleteDataset(
            @InternalToolParam(description = "The ID of the dataset to delete")
            int datasetId
    ) {
        log.info("Deleting dataset {}", datasetId);
        apiClient.deleteDataset(datasetId);
    }
}
