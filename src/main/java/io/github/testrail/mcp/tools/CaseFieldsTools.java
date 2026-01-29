package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.CaseField;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tools for TestRail Case Fields API.
 */
@Component
public class CaseFieldsTools {

    private final TestrailApiClient apiClient;

    public CaseFieldsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets all case fields")
    public Object[] getCaseFields() {
        return apiClient.getCaseFields();
    }

    @Tool(description = "Adds a new case field")
    public CaseField addCaseField(Map<String, Object> caseField) {
        return apiClient.addCaseField(caseField);
    }
}
