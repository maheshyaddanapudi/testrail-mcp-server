package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a TestRail configuration group.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationGroup {
    
    private Long id;
    private String name;
    
    @JsonProperty("project_id")
    private Long projectId;
    
    private List<Configuration> configs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Configuration> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Configuration> configs) {
        this.configs = configs;
    }
}
