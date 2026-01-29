package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Templates API.
 */
@Component
public class TemplatesTools {

    private final TestrailApiClient apiClient;

    public TemplatesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets templates for a project")
    public Object[] getTemplates(Integer projectId) {
        return apiClient.getTemplates(projectId);
    }
}
