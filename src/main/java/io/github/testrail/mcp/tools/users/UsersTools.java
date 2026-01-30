package io.github.testrail.mcp.tools.users;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.User;
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
    
    @InternalTool(
            name = "get_user",
            description = """
                    Retrieves a user by ID from TestRail.
                    Returns user details including name, email, role, active status, and admin status.
                    Any user can retrieve their own information; admin access required for other users.
                    
                    **When to use:** Use this tool when you need to check user details,
                    verify user role and permissions, get contact information, or validate user IDs.
                    
                    **Might lead to:** get_users (to see all users), get_current_user (for your own info).
                    
                    **Example prompts:**
                    - "Show me user 5"
                    - "Get details of user 10"
                    - "What's the email for user 3?"
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_user', {userId: 5})",
                    "execute_tool('get_user', {userId: 10})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "user", "details", "email", "role"}
    )
    public User getUser(
        @InternalToolParam(description = "The unique ID of the user to retrieve")
        Integer userId
    ) {
        return apiClient.getUser(userId);
    }
    
    @InternalTool(
            name = "get_current_user",
            description = """
                    Retrieves the currently authenticated user's information.
                    Returns details about the user making the API request.
                    Useful for checking your own permissions and role.
                    
                    **When to use:** Use this tool when you need to verify authentication,
                    check your own role and permissions, or get your user ID.
                    
                    **Might lead to:** get_user (to check other users), get_users (to see team).
                    
                    **Example prompts:**
                    - "Who am I?"
                    - "Get my user information"
                    - "What's my role in TestRail?"
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_current_user', {})"
            },
            keywords = {"get", "current", "me", "myself", "authenticated", "user", "who", "role", "permissions"}
    )
    public User getCurrentUser() {
        return apiClient.getCurrentUser();
    }
    
    @InternalTool(
            name = "get_user_by_email",
            description = """
                    Retrieves a user by their email address.
                    Returns user details for the specified email.
                    Any user can retrieve their own information; admin access required for other users.
                    
                    **When to use:** Use this tool when you know a user's email but need their user ID,
                    look up user details by email, or verify user existence.
                    
                    **Might lead to:** get_user (for more details), get_users (to see all users).
                    
                    **Example prompts:**
                    - "Find user with email john.doe@company.com"
                    - "Get user ID for jane.smith@company.com"
                    - "Look up user by email test@example.com"
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_user_by_email', {email: 'john.doe@company.com'})",
                    "execute_tool('get_user_by_email', {email: 'jane.smith@company.com'})"
            },
            keywords = {"get", "find", "lookup", "search", "user", "email", "address"}
    )
    public User getUserByEmail(
        @InternalToolParam(description = "The email address of the user to retrieve")
        String email
    ) {
        return apiClient.getUserByEmail(email);
    }
    
    @InternalTool(
            name = "get_users",
            description = """
                    Retrieves all users or users for a specific project.
                    For administrators: omit projectId to get all users.
                    For non-administrators: projectId is required and returns only users with explicit project access.
                    Note: When projectId is specified, inactive users and users without project access are excluded.
                    
                    **When to use:** Use this tool when you need to see team members,
                    find users for assignment, list project collaborators, or get user IDs for operations.
                    
                    **Might lead to:** get_user (for detailed info), get_user_by_email (to find by email).
                    
                    **Example prompts:**
                    - "List all users in project 1"
                    - "Show me team members for project 5"
                    - "Get all users" (admin only)
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_users', {projectId: 1})",
                    "execute_tool('get_users', {projectId: 5})",
                    "execute_tool('get_users', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "users", "team", "members", "all"}
    )
    public Object[] getUsers(
        @InternalToolParam(description = "The ID of the project to retrieve users for (required for non-admins, optional for admins)", required = false)
        Integer projectId
    ) {
        return apiClient.getUsers(projectId);
    }
}
