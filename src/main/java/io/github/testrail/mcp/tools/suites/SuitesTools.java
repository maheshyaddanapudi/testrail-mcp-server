package io.github.testrail.mcp.tools.suites;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Suite;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.tools.annotation.ToolCategory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
    
    @Tool(description = "Retrieves a test suite by ID from TestRail. " + "Returns complete suite details including name, description, project, and suite type (master, baseline, completed). " + "Example: Get suite 3 to check its configuration and status.")
    public Suite getSuite(
        @ToolParam(description = "The unique ID of the test suite to retrieve", required = true)
        Integer suiteId
    ) {
        return apiClient.getSuite(suiteId);
    }
    
    @Tool(description = "Retrieves all test suites for a project. " + "Test suites are used to organize test cases by functional areas or modules. " + "Example: Get all suites for project 1 to see how test cases are organized.")
    public Object[] getSuites(
        @ToolParam(description = "The ID of the project to retrieve test suites for", required = true)
        Integer projectId
    ) {
        return apiClient.getSuites(projectId);
    }
    
    @Tool(description = "Creates a new test suite in a project. " + "Requires a name and optionally accepts a description. " + "After creating a suite, you can add sections and test cases to it. " + "Example: Create suite 'User Authentication' with description 'Login, logout, and password reset tests' for project 1.")
    public Suite addSuite(
        @ToolParam(description = "The ID of the project to add the test suite to", required = true)
        Integer projectId,
        
        @ToolParam(description = "The name of the test suite", required = true)
        String name,
        
        @ToolParam(description = "The description of the test suite (optional)")
        String description
    ) {
        Map<String, Object> suite = new HashMap<>();
        suite.put("name", name);
        if (description != null) suite.put("description", description);
        
        return apiClient.addSuite(projectId, suite);
    }
    
    @Tool(description = "Updates an existing test suite. " + "Supports partial updates - only provided fields will be modified. " + "Can update name and description. " + "Example: Update suite 5 to rename it to 'API Testing v2'.")
    public Suite updateSuite(
        @ToolParam(description = "The ID of the test suite to update", required = true)
        Integer suiteId,
        
        @ToolParam(description = "The new name of the test suite (optional)")
        String name,
        
        @ToolParam(description = "The new description (optional)")
        String description
    ) {
        Map<String, Object> suite = new HashMap<>();
        if (name != null) suite.put("name", name);
        if (description != null) suite.put("description", description);
        
        return apiClient.updateSuite(suiteId, suite);
    }
    
    @Tool(description = "Deletes a test suite. " + "WARNING: This deletes all active test runs and results (non-archived). " + "Use soft=1 to preview what will be deleted without actually deleting. " + "Use soft=0 or omit to perform actual deletion. " + "Example: Delete suite 10, or use soft=1 to preview impact first.")
    public OperationResult deleteSuite(
        @ToolParam(description = "The ID of the test suite to delete", required = true)
        Integer suiteId,
        
        @ToolParam(description = "Soft delete: 1 to preview deletion impact, 0 or null to actually delete")
        Integer soft
    ) {
        apiClient.deleteSuite(suiteId, soft);
        if (soft != null && soft == 1) {
            return OperationResult.success("Suite deletion preview completed (no actual deletion)");
        }
        return OperationResult.success("Suite deleted successfully");
    }
}
