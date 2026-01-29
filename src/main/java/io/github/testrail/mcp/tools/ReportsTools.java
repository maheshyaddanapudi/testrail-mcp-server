package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Report;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools for running and managing TestRail reports.
 * Reports must be marked as "On demand via the API" to be accessible.
 */
@Component
public class ReportsTools {

    private final TestrailApiClient apiClient;
    private final ObjectMapper objectMapper;

    public ReportsTools(TestrailApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Get all API-accessible reports for a project. Reports must be marked as 'On demand via the API' to appear here.")
    public String getReports(int projectId) throws JsonProcessingException {
        List<Report> reports = apiClient.getReports(projectId);
        return objectMapper.writeValueAsString(reports);
    }

    @Tool(description = "Run a single-project report and get the results. Use getReports to find available report template IDs.")
    public String runReport(int reportTemplateId) throws JsonProcessingException {
        JsonNode reportData = apiClient.runReport(reportTemplateId);
        return objectMapper.writeValueAsString(reportData);
    }

    @Tool(description = "Get all cross-project reports. Requires TestRail Enterprise license.")
    public String getCrossProjectReports() throws JsonProcessingException {
        List<Report> reports = apiClient.getCrossProjectReports();
        return objectMapper.writeValueAsString(reports);
    }

    @Tool(description = "Run a cross-project report and get the results. Requires TestRail Enterprise license.")
    public String runCrossProjectReport(int reportTemplateId) throws JsonProcessingException {
        JsonNode reportData = apiClient.runCrossProjectReport(reportTemplateId);
        return objectMapper.writeValueAsString(reportData);
    }
}
