package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Dataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasetsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = "Get a single dataset by ID with its variables")
    public Dataset getDataset(int datasetId) {
        log.info("Getting dataset {}", datasetId);
        return apiClient.getDataset(datasetId);
    }

    @Tool(description = "Get all datasets for a project")
    public List<Dataset> getDatasets(int projectId) {
        log.info("Getting datasets for project {}", projectId);
        return apiClient.getDatasets(projectId);
    }

    @Tool(description = "Create a new dataset in a project. Requires Enterprise license.")
    public Dataset addDataset(int projectId, Map<String, Object> dataset) {
        log.info("Adding dataset to project {}", projectId);
        return apiClient.addDataset(projectId, dataset);
    }

    @Tool(description = "Update an existing dataset. Requires Enterprise license.")
    public Dataset updateDataset(int datasetId, Map<String, Object> dataset) {
        log.info("Updating dataset {}", datasetId);
        return apiClient.updateDataset(datasetId, dataset);
    }

    @Tool(description = "Delete a dataset. Requires Enterprise license.")
    public void deleteDataset(int datasetId) {
        log.info("Deleting dataset {}", datasetId);
        apiClient.deleteDataset(datasetId);
    }
}
