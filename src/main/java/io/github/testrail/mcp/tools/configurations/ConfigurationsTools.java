package io.github.testrail.mcp.tools.configurations;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Configuration;
import io.github.testrail.mcp.model.ConfigurationGroup;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tools for TestRail Configurations API.
 * Configurations define test environment variations (browsers, OS, devices) used in test plans.
 * Configuration groups organize related configurations (e.g., "Browsers" group contains Chrome, Firefox, Safari).
 */
@Component
public class ConfigurationsTools {

    private final TestrailApiClient apiClient;

    public ConfigurationsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_configs",
            description = """
                    Retrieves all configuration groups and their configurations for a project.
                    Returns a hierarchical structure where each configuration group contains multiple configurations.
                    For example, a "Browsers" group might contain Chrome, Firefox, and Safari configurations.
                    
                    **When to use:** Use this tool when you need to view all available test configurations in a project,
                    understand what configuration options exist before creating test plans, check configuration IDs for
                    use in test plan creation, or audit the test environment matrix.
                    
                    **Might lead to:** add_config_group (to create new group), add_config (to add configuration),
                    add_plan (to create test plan with configurations).
                    
                    **Example prompts:**
                    - "Show me all configurations in project 1"
                    - "What configuration groups exist for project 5?"
                    - "List all browser configurations available"
                    - "What test environments are configured for this project?"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('get_configs', {projectId: 1})",
                    "execute_tool('get_configs', {projectId: 5})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "configurations", "config", "groups", "environments"}
    )
    public Object[] getConfigs(
            @InternalToolParam(description = "The ID of the project to retrieve configurations for")
            Integer projectId
    ) {
        return apiClient.getConfigs(projectId);
    }

    @InternalTool(
            name = "add_config_group",
            description = """
                    Creates a new configuration group in a project.
                    Configuration groups organize related configurations (e.g., "Operating Systems", "Browsers", "Devices").
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool when you need to add a new category of test environment variations,
                    set up configuration structure for a new project, organize existing configurations into logical groups,
                    or prepare for multi-configuration test plans.
                    
                    **Might lead to:** add_config (to add configurations to the new group), get_configs (to verify creation).
                    
                    **Example prompts:**
                    - "Create a configuration group called 'Browsers' in project 1"
                    - "Add a new 'Mobile Devices' configuration group to project 5"
                    - "Set up an 'Operating Systems' configuration group"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('add_config_group', {projectId: 1, configGroup: {name: 'Browsers'}})",
                    "execute_tool('add_config_group', {projectId: 5, configGroup: {name: 'Mobile Devices'}})"
            },
            keywords = {"add", "create", "new", "config", "configuration", "group", "category", "organize"}
    )
    public ConfigurationGroup addConfigGroup(
            @InternalToolParam(description = "The ID of the project to add the configuration group to")
            Integer projectId,
            @InternalToolParam(description = "Configuration group map with keys: name (required)")
            Map<String, Object> configGroup
    ) {
        return apiClient.addConfigGroup(projectId, configGroup);
    }

    @InternalTool(
            name = "add_config",
            description = """
                    Creates a new configuration within a configuration group.
                    Configurations represent specific test environment variations (e.g., "Chrome 120", "Windows 11", "iPhone 15").
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool when you need to add a new test environment option to an existing group,
                    expand the test matrix with new browser/OS/device versions, prepare configurations for upcoming test cycles,
                    or maintain up-to-date environment options.
                    
                    **Might lead to:** get_configs (to verify creation), add_plan (to use in test plans).
                    
                    **Example prompts:**
                    - "Add 'Chrome 120' configuration to the Browsers group (ID 3)"
                    - "Create a new 'Windows 11' configuration in group 5"
                    - "Add 'iPhone 15 Pro' to the Mobile Devices configuration group"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('add_config', {configGroupId: 3, config: {name: 'Chrome 120'}})",
                    "execute_tool('add_config', {configGroupId: 5, config: {name: 'Windows 11'}})"
            },
            keywords = {"add", "create", "new", "config", "configuration", "environment", "browser", "os", "device"}
    )
    public Configuration addConfig(
            @InternalToolParam(description = "The ID of the configuration group to add the configuration to")
            Integer configGroupId,
            @InternalToolParam(description = "Configuration map with keys: name (required)")
            Map<String, Object> config
    ) {
        return apiClient.addConfig(configGroupId, config);
    }

    @InternalTool(
            name = "update_config_group",
            description = """
                    Updates an existing configuration group's properties (primarily its name).
                    Does not affect closed test plans/runs, or active test plans/runs unless they are updated.
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool when you need to rename a configuration group for clarity,
                    correct naming mistakes, reorganize configuration structure, or improve configuration group descriptions.
                    
                    **Might lead to:** get_configs (to verify update).
                    
                    **Example prompts:**
                    - "Rename configuration group 3 to 'Web Browsers'"
                    - "Update the name of configuration group 5 to 'Desktop Operating Systems'"
                    - "Change configuration group 2's name to 'Mobile Platforms'"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('update_config_group', {configGroupId: 3, configGroup: {name: 'Web Browsers'}})",
                    "execute_tool('update_config_group', {configGroupId: 5, configGroup: {name: 'Desktop Operating Systems'}})"
            },
            keywords = {"update", "modify", "change", "edit", "rename", "config", "configuration", "group"}
    )
    public ConfigurationGroup updateConfigGroup(
            @InternalToolParam(description = "The ID of the configuration group to update")
            Integer configGroupId,
            @InternalToolParam(description = "Configuration group map with keys: name")
            Map<String, Object> configGroup
    ) {
        return apiClient.updateConfigGroup(configGroupId, configGroup);
    }

    @InternalTool(
            name = "update_config",
            description = """
                    Updates an existing configuration's properties (primarily its name).
                    Does not affect closed test plans/runs, or active test plans/runs unless they are updated.
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool when you need to rename a configuration to reflect version updates,
                    correct naming mistakes, update configuration details, or maintain accurate environment labels.
                    
                    **Might lead to:** get_configs (to verify update).
                    
                    **Example prompts:**
                    - "Rename configuration 10 to 'Chrome 121'"
                    - "Update configuration 25 to 'Windows 11 23H2'"
                    - "Change configuration 15's name to 'iPhone 15 Pro Max'"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('update_config', {configId: 10, config: {name: 'Chrome 121'}})",
                    "execute_tool('update_config', {configId: 25, config: {name: 'Windows 11 23H2'}})"
            },
            keywords = {"update", "modify", "change", "edit", "rename", "config", "configuration"}
    )
    public Configuration updateConfig(
            @InternalToolParam(description = "The ID of the configuration to update")
            Integer configId,
            @InternalToolParam(description = "Configuration map with keys: name")
            Map<String, Object> config
    ) {
        return apiClient.updateConfig(configId, config);
    }

    @InternalTool(
            name = "delete_config_group",
            description = """
                    Permanently deletes a configuration group and all its configurations.
                    **WARNING: This action cannot be undone.** However, it does not affect closed test plans/runs,
                    or active test plans/runs unless they are updated.
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool ONLY when you need to remove obsolete configuration groups,
                    clean up unused test environment categories, or restructure the configuration hierarchy.
                    Always verify the group is not in use before deletion.
                    
                    **Might lead to:** get_configs (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete configuration group 8"
                    - "Remove the 'Legacy Browsers' configuration group (ID 12)"
                    - "Permanently delete configuration group 3 and all its configurations"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('delete_config_group', {configGroupId: 8})",
                    "execute_tool('delete_config_group', {configGroupId: 12})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "config", "configuration", "group", "cleanup"}
    )
    public void deleteConfigGroup(
            @InternalToolParam(description = "The ID of the configuration group to delete")
            Integer configGroupId
    ) {
        apiClient.deleteConfigGroup(configGroupId);
    }

    @InternalTool(
            name = "delete_config",
            description = """
                    Permanently deletes a single configuration from its group.
                    **WARNING: This action cannot be undone.** However, it does not affect closed test plans/runs,
                    or active test plans/runs unless they are updated.
                    Requires TestRail 5.2 or later.
                    
                    **When to use:** Use this tool ONLY when you need to remove obsolete configurations,
                    clean up deprecated environment versions, retire unsupported platforms, or maintain a current test matrix.
                    Always verify the configuration is not in active use before deletion.
                    
                    **Might lead to:** get_configs (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete configuration 15"
                    - "Remove 'Internet Explorer 11' configuration (ID 22)"
                    - "Permanently delete the 'Windows 7' configuration"
                    """,
            category = "configurations",
            examples = {
                    "execute_tool('delete_config', {configId: 15})",
                    "execute_tool('delete_config', {configId: 22})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "config", "configuration", "cleanup"}
    )
    public void deleteConfig(
            @InternalToolParam(description = "The ID of the configuration to delete")
            Integer configId
    ) {
        apiClient.deleteConfig(configId);
    }
}
