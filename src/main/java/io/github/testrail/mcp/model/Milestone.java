package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail milestone.
 */
public class Milestone {
    
    private Integer id;
    private String name;
    private String description;
    
    @JsonProperty("project_id")
    private Integer projectId;
    
    @JsonProperty("parent_id")
    private Integer parentId;
    
    @JsonProperty("due_on")
    private Long dueOn;
    
    @JsonProperty("start_on")
    private Long startOn;
    
    @JsonProperty("started_on")
    private Long startedOn;
    
    @JsonProperty("completed_on")
    private Long completedOn;
    
    @JsonProperty("is_completed")
    private Boolean isCompleted;
    
    @JsonProperty("is_started")
    private Boolean isStarted;
    
    private String refs;
    private String url;
    private Object[] milestones; // Sub-milestones array
    
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
    
    public Integer getParentId() {
        return parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    public Long getDueOn() {
        return dueOn;
    }
    
    public void setDueOn(Long dueOn) {
        this.dueOn = dueOn;
    }
    
    public Long getStartOn() {
        return startOn;
    }
    
    public void setStartOn(Long startOn) {
        this.startOn = startOn;
    }
    
    public Long getStartedOn() {
        return startedOn;
    }
    
    public void setStartedOn(Long startedOn) {
        this.startedOn = startedOn;
    }
    
    public Long getCompletedOn() {
        return completedOn;
    }
    
    public void setCompletedOn(Long completedOn) {
        this.completedOn = completedOn;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Boolean getIsStarted() {
        return isStarted;
    }
    
    public void setIsStarted(Boolean isStarted) {
        this.isStarted = isStarted;
    }
    
    public String getRefs() {
        return refs;
    }
    
    public void setRefs(String refs) {
        this.refs = refs;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Object[] getMilestones() {
        return milestones;
    }
    
    public void setMilestones(Object[] milestones) {
        this.milestones = milestones;
    }
}
