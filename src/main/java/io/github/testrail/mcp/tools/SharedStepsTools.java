package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.SharedStep;
import io.github.testrail.mcp.model.SharedStepHistory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail shared steps.
 * Requires TestRail 7.0 or later.
 */
@Component
public class SharedStepsTools {

    private final TestrailApiClient apiClient;
    private final ObjectMapper objectMapper;

    public SharedStepsTools(TestrailApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Get a shared step by ID. Shared steps allow reusing test steps across multiple test cases.")
    public String getSharedStep(int sharedStepId) throws JsonProcessingException {
        SharedStep sharedStep = apiClient.getSharedStep(sharedStepId);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = "Get the history of changes for a shared step.")
    public String getSharedStepHistory(int sharedStepId) throws JsonProcessingException {
        List<SharedStepHistory> history = apiClient.getSharedStepHistory(sharedStepId);
        return objectMapper.writeValueAsString(history);
    }

    @Tool(description = "Get all shared steps for a project.")
    public String getSharedSteps(int projectId) throws JsonProcessingException {
        List<SharedStep> sharedSteps = apiClient.getSharedSteps(projectId);
        return objectMapper.writeValueAsString(sharedSteps);
    }

    @Tool(description = "Add a new shared step to a project. Provide title and custom_steps_separated in the data.")
    public String addSharedStep(int projectId, String data) throws JsonProcessingException {
        Map<String, Object> dataMap = objectMapper.readValue(data, Map.class);
        SharedStep sharedStep = apiClient.addSharedStep(projectId, dataMap);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = "Update an existing shared step. Provide fields to update in the data.")
    public String updateSharedStep(int sharedStepId, String data) throws JsonProcessingException {
        Map<String, Object> dataMap = objectMapper.readValue(data, Map.class);
        SharedStep sharedStep = apiClient.updateSharedStep(sharedStepId, dataMap);
        return objectMapper.writeValueAsString(sharedStep);
    }

    @Tool(description = "Delete a shared step by ID.")
    public String deleteSharedStep(int sharedStepId) {
        apiClient.deleteSharedStep(sharedStepId);
        return "{\"success\": true, \"message\": \"Shared step deleted successfully\"}";
    }
}
