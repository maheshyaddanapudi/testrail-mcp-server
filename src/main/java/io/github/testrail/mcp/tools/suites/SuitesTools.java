package io.github.testrail.mcp.tools.suites;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Suite;
import io.github.testrail.mcp.model.OperationResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP tools for managing TestRail test suites.
 */
@Component
public class SuitesTools {
    
    private final TestrailApiClient apiClient;
    
    public SuitesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @InternalTool(
            name = "get_suite",
            description = """
                    Retrieves a test suite by ID from TestRail.
                    Returns complete suite details including name, description, project, and suite type (master, baseline, completed).
                    
                    **When to use:** Use this tool when you need to check suite configuration and status,
                    verify suite details before creating test runs, or understand suite organization.
                    
                    **Might lead to:** get_sections (to see sections in suite), get_test_cases (to list cases),
                    update_suite (to modify), add_run (to create run from suite).
                    
                    **Example prompts:**
                    - "Show me suite 3"
                    - "Get details of the API Testing suite"
                    - "What's in suite 10?"
                    """,
            category = "test-suites",
            examples = {
                    "execute_tool('get_suite', {suiteId: 3})",
                    "execute_tool('get_suite', {suiteId: 10})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "suite", "details", "configuration"}
    )
    public Suite getSuite(
        @InternalToolParam(description = "The unique ID of the test suite to retrieve")
        Integer suiteId
    ) {
        return apiClient.getSuite(suiteId);
    }
    
    @InternalTool(
            name = "get_suites",
            description = """
                    Retrieves all test suites for a project.
                    Test suites are used to organize test cases by functional areas or modules.
                    
                    **When to use:** Use this tool when you need to see how test cases are organized,
                    find available suites for creating test runs, or explore project test structure.
                    
                    **Might lead to:** get_suite (for details), add_suite (to create new),
                    get_sections (to see sections in suite), add_run (to create run).
                    
                    **Example prompts:**
                    - "List all suites in project 1"
                    - "Show me test suites for project 5"
                    - "What suites are available in project 3?"
                    """,
            category = "test-suites",
            examples = {
                    "execute_tool('get_suites', {projectId: 1})",
                    "execute_tool('get_suites', {projectId: 5})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "suites", "all", "organize"}
    )
    public Object[] getSuites(
        @InternalToolParam(description = "The ID of the project to retrieve test suites for")
        Integer projectId
    ) {
        return apiClient.getSuites(projectId);
    }
    
    @InternalTool(
            name = "add_suite",
            description = """
                    Creates a new test suite in a project.
                    Requires a name and optionally accepts a description.
                    After creating a suite, you can add sections and test cases to it.
                    
                    **When to use:** Use this tool when you need to organize test cases by functional area,
                    create a new module for testing, or set up test structure for a new feature.
                    
                    **Required fields:**
                    - **name**: The name of the test suite
                    
                    **Optional fields:**
                    - **description**: Detailed description of the suite
                    
                    **Might lead to:** add_section (to create sections), add_test_case (to add cases),
                    get_suite (to verify creation).
                    
                    **Example prompts:**
                    - "Create a test suite called 'User Authentication' in project 1"
                    - "Add a suite for API testing in project 3"
                    - "Create suite 'Payment Processing' with description 'Checkout and payment tests'"
                    """,
            category = "test-suites",
            examples = {
                    "execute_tool('add_suite', {projectId: 1, name: 'User Authentication'})",
                    "execute_tool('add_suite', {projectId: 3, name: 'Payment Processing', description: 'Checkout and payment tests'})"
            },
            keywords = {"add", "create", "new", "suite", "organize", "module", "feature"}
    )
    public Suite addSuite(
        @InternalToolParam(description = "The ID of the project to add the test suite to")
        Integer projectId,
        
        @InternalToolParam(description = "The name of the test suite")
        String name,
        
        @InternalToolParam(description = "The description of the test suite (optional)", required = false)
        String description
    ) {
        Map<String, Object> suite = new HashMap<>();
        suite.put("name", name);
        if (description != null) suite.put("description", description);
        
        return apiClient.addSuite(projectId, suite);
    }
    
    @InternalTool(
            name = "update_suite",
            description = """
                    Updates an existing test suite.
                    Supports partial updates - only provided fields will be modified.
                    Can update name and description.
                    
                    **When to use:** Use this tool when you need to rename a suite,
                    update the description, or correct suite information.
                    
                    **Updatable fields:**
                    - **name**: Suite name
                    - **description**: Suite description
                    
                    **Might lead to:** get_suite (to verify changes), get_suites (to see updated list).
                    
                    **Example prompts:**
                    - "Rename suite 5 to 'API Testing v2'"
                    - "Update the description of suite 10"
                    """,
            category = "test-suites",
            examples = {
                    "execute_tool('update_suite', {suiteId: 5, name: 'API Testing v2'})",
                    "execute_tool('update_suite', {suiteId: 10, description: 'Updated description for mobile tests'})"
            },
            keywords = {"update", "modify", "change", "edit", "rename", "suite"}
    )
    public Suite updateSuite(
        @InternalToolParam(description = "The ID of the test suite to update")
        Integer suiteId,
        
        @InternalToolParam(description = "The new name of the test suite (optional)", required = false)
        String name,
        
        @InternalToolParam(description = "The new description (optional)", required = false)
        String description
    ) {
        Map<String, Object> suite = new HashMap<>();
        if (name != null) suite.put("name", name);
        if (description != null) suite.put("description", description);
        
        return apiClient.updateSuite(suiteId, suite);
    }
    
    @InternalTool(
            name = "delete_suite",
            description = """
                    Deletes a test suite.
                    
                    **WARNING: This deletes all active test runs and results (non-archived).**
                    
                    Use soft=1 to preview what will be deleted without actually deleting.
                    Use soft=0 or omit to perform actual deletion.
                    
                    **When to use:** Use this tool when you need to remove an obsolete suite,
                    clean up test structure, or reorganize test organization. Be careful as this removes data.
                    
                    **Might lead to:** get_suites (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete suite 10"
                    - "Preview deletion of suite 5"
                    - "Remove the old API tests suite"
                    """,
            category = "test-suites",
            examples = {
                    "execute_tool('delete_suite', {suiteId: 10})",
                    "execute_tool('delete_suite', {suiteId: 5, soft: 1})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "suite", "cleanup", "preview"}
    )
    public OperationResult deleteSuite(
        @InternalToolParam(description = "The ID of the test suite to delete")
        Integer suiteId,
        
        @InternalToolParam(description = "Soft delete: 1 to preview deletion impact, 0 or null to actually delete", required = false, defaultValue = "0")
        Integer soft
    ) {
        apiClient.deleteSuite(suiteId, soft);
        if (soft != null && soft == 1) {
            return OperationResult.success("Suite deletion preview completed (no actual deletion)");
        }
        return OperationResult.success("Suite deleted successfully");
    }
}
