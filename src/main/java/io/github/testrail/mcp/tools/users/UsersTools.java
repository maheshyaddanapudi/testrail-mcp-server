package io.github.testrail.mcp.tools.users;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.User;
import io.github.testrail.mcp.tools.annotation.ToolCategory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools for retrieving TestRail user information.
 */
@Component
public class UsersTools {
    
    private final TestrailApiClient apiClient;
    
    public UsersTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @Tool(description = "Retrieves a user by ID from TestRail. " + "Returns user details including name, email, role, active status, and admin status. " + "Any user can retrieve their own information; admin access required for other users. " + "Example: Get user 5 to check their role and email address.")
    public User getUser(
        @ToolParam(description = "The unique ID of the user to retrieve", required = true)
        Integer userId
    ) {
        return apiClient.getUser(userId);
    }
    
    @Tool(description = "Retrieves the currently authenticated user's information. " + "Returns details about the user making the API request. " + "Useful for checking your own permissions and role. " + "Example: Get current user to verify authentication and check your role.")
    public User getCurrentUser() {
        return apiClient.getCurrentUser();
    }
    
    @Tool(description = "Retrieves a user by their email address. " + "Returns user details for the specified email. " + "Any user can retrieve their own information; admin access required for other users. " + "Example: Get user with email 'john.doe@company.com' to find their user ID.")
    public User getUserByEmail(
        @ToolParam(description = "The email address of the user to retrieve", required = true)
        String email
    ) {
        return apiClient.getUserByEmail(email);
    }
    
    @Tool(description = "Retrieves all users or users for a specific project. " + "For administrators: omit projectId to get all users. " + "For non-administrators: projectId is required and returns only users with explicit project access. " + "Note: When projectId is specified, inactive users and users without project access are excluded. " + "Example: Get all users for project 1 to see team members, or omit projectId (admin only) for all users.")
    public Object[] getUsers(
        @ToolParam(description = "The ID of the project to retrieve users for (required for non-admins, optional for admins)")
        Integer projectId
    ) {
        return apiClient.getUsers(projectId);
    }
}
