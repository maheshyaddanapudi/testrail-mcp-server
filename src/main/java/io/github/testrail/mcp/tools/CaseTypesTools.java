package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Case Types API.
 */
@Component
public class CaseTypesTools {

    private final TestrailApiClient apiClient;

    public CaseTypesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets all case types")
    public Object[] getCaseTypes() {
        return apiClient.getCaseTypes();
    }
}
