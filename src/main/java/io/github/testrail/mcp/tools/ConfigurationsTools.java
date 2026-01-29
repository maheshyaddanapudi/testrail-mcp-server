package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Configuration;
import io.github.testrail.mcp.model.ConfigurationGroup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tools for TestRail Configurations API.
 */
@Component
public class ConfigurationsTools {

    private final TestrailApiClient apiClient;

    public ConfigurationsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = "Gets configuration groups for a project")
    public Object[] getConfigs(Integer projectId) {
        return apiClient.getConfigs(projectId);
    }

    @Tool(description = "Adds a new configuration group to a project")
    public ConfigurationGroup addConfigGroup(Integer projectId, Map<String, Object> configGroup) {
        return apiClient.addConfigGroup(projectId, configGroup);
    }

    @Tool(description = "Adds a new configuration to a configuration group")
    public Configuration addConfig(Integer configGroupId, Map<String, Object> config) {
        return apiClient.addConfig(configGroupId, config);
    }

    @Tool(description = "Updates a configuration group")
    public ConfigurationGroup updateConfigGroup(Integer configGroupId, Map<String, Object> configGroup) {
        return apiClient.updateConfigGroup(configGroupId, configGroup);
    }

    @Tool(description = "Updates a configuration")
    public Configuration updateConfig(Integer configId, Map<String, Object> config) {
        return apiClient.updateConfig(configId, config);
    }

    @Tool(description = "Deletes a configuration group")
    public void deleteConfigGroup(Integer configGroupId) {
        apiClient.deleteConfigGroup(configGroupId);
    }

    @Tool(description = "Deletes a configuration")
    public void deleteConfig(Integer configId) {
        apiClient.deleteConfig(configId);
    }
}
