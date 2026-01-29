package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Priorities API.
 */
@Component
public class PrioritiesTools {

    private final TestrailApiClient apiClient;

    public PrioritiesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets all priorities")
    public Object[] getPriorities() {
        return apiClient.getPriorities();
    }
}
