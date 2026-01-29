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

    @Tool(description = "Get all available user roles in TestRail. Includes role ID, name, and whether it's the default role.")
    public String getRoles() throws JsonProcessingException {
        List<Role> roles = apiClient.getRoles();
        return objectMapper.writeValueAsString(roles);
    }
}
