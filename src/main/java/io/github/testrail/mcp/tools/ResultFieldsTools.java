package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Result Fields API.
 */
@Component
public class ResultFieldsTools {

    private final TestrailApiClient apiClient;

    public ResultFieldsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets all result fields")
    public Object[] getResultFields() {
        return apiClient.getResultFields();
    }
}
