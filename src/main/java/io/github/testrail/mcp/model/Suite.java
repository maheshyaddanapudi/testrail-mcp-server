package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail test suite.
 */
public class Suite {
    
    private Integer id;
    private String name;
    private String description;
    
    @JsonProperty("project_id")
    private Integer projectId;
    
    @JsonProperty("is_master")
    private Boolean isMaster;
    
    @JsonProperty("is_baseline")
    private Boolean isBaseline;
    
    @JsonProperty("is_completed")
    private Boolean isCompleted;
    
    @JsonProperty("completed_on")
    private Long completedOn;
    
    private String url;
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
    
    public Boolean getIsMaster() {
        return isMaster;
    }
    
    public void setIsMaster(Boolean isMaster) {
        this.isMaster = isMaster;
    }
    
    public Boolean getIsBaseline() {
        return isBaseline;
    }
    
    public void setIsBaseline(Boolean isBaseline) {
        this.isBaseline = isBaseline;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Long getCompletedOn() {
        return completedOn;
    }
    
    public void setCompletedOn(Long completedOn) {
        this.completedOn = completedOn;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}
