package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Variable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VariablesTools {

    private final TestrailApiClient apiClient;

    @Tool(description = "Get all variables for a project. Requires Enterprise license.")
    public List<Variable> getVariables(int projectId) {
        log.info("Getting variables for project {}", projectId);
        return apiClient.getVariables(projectId);
    }

    @Tool(description = "Create a new variable in a project. Requires Enterprise license.")
    public Variable addVariable(int projectId, Map<String, Object> variable) {
        log.info("Adding variable to project {}", projectId);
        return apiClient.addVariable(projectId, variable);
    }

    @Tool(description = "Update an existing variable. Requires Enterprise license.")
    public Variable updateVariable(int variableId, Map<String, Object> variable) {
        log.info("Updating variable {}", variableId);
        return apiClient.updateVariable(variableId, variable);
    }

    @Tool(description = "Delete a variable. Also deletes corresponding values from datasets. Requires Enterprise license.")
    public void deleteVariable(int variableId) {
        log.info("Deleting variable {}", variableId);
        apiClient.deleteVariable(variableId);
    }
}
