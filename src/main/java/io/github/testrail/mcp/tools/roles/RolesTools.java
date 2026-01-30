package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Role;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools for managing TestRail user roles.
 * Roles define user permissions and access levels within TestRail.
 * Common roles include: Admin, Lead, Tester, Read-Only.
 * Requires TestRail 7.3 or later.
 */
@Component
public class RolesTools {

    private final TestrailApiClient apiClient;
    private final ObjectMapper objectMapper;

    public RolesTools(TestrailApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = """
            Retrieves all available user roles in TestRail.
            Roles define user permissions and access levels for test management activities.
            Common roles include: Admin, Lead, Tester, Read-Only (custom roles may also exist).
            Requires TestRail 7.3 or later.
            
            Each role has:
            - **id**: Unique identifier used when adding/updating users
            - **name**: Display name of the role (e.g., "Lead", "Tester")
            - **is_default**: Boolean indicating if this is the default role for new users
            - **is_project_admin**: Boolean indicating project-level admin permissions (Enterprise only)
            
            **When to use:** Use this tool when you need to understand available user roles,
            get role IDs for user management, check which role is the default for new users,
            audit role configurations, or prepare user provisioning workflows.
            
            **Might lead to:** add_user or update_user (to assign roles to users),
            get_users (to see which users have specific roles).
            
            **Example prompts:**
            - "Show me all user roles"
            - "What roles are available in TestRail?"
            - "What's the ID for the 'Tester' role?"
            - "Which role is the default for new users?"
            - "List all available user permission levels"
            """)
    public String getRoles() throws JsonProcessingException {
        List<Role> roles = apiClient.getRoles();
        return objectMapper.writeValueAsString(roles);
    }
}
