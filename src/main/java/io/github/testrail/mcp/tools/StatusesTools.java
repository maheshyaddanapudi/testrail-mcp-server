package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Statuses API.
 */
@Component
public class StatusesTools {

    private final TestrailApiClient apiClient;

    public StatusesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets all statuses")
    public Object[] getStatuses() {
        return apiClient.getStatuses();
    }
}
