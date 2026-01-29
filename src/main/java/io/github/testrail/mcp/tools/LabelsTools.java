package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Label;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LabelsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = "Get a single label by ID")
    public Label getLabel(int labelId) {
        log.info("Getting label {}", labelId);
        return apiClient.getLabel(labelId);
    }

    @Tool(description = "Get all labels for a project with optional pagination")
    public List<Label> getLabels(int projectId, Integer limit, Integer offset) {
        log.info("Getting labels for project {}", projectId);
        return apiClient.getLabels(projectId, limit, offset);
    }

    @Tool(description = "Update an existing label. Maximum 20 characters for title.")
    public Label updateLabel(int labelId, Map<String, Object> label) {
        log.info("Updating label {}", labelId);
        return apiClient.updateLabel(labelId, label);
    }
}
