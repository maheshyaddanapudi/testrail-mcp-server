package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents a shared step in TestRail.
 * Shared steps allow reusing test steps across multiple test cases.
 * Requires TestRail 7.0 or later.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharedStep {
    private Integer id;
    private String title;
    
    @JsonProperty("project_id")
    private Integer projectId;
    
    @JsonProperty("created_by")
    private Integer createdBy;
    
    @JsonProperty("created_on")
    private Long createdOn;
    
    @JsonProperty("updated_by")
    private Integer updatedBy;
    
    @JsonProperty("updated_on")
    private Long updatedOn;
    
    @JsonProperty("custom_steps_separated")
    private List<Map<String, Object>> customStepsSeparated;
    
    @JsonProperty("case_ids")
    private List<Integer> caseIds;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<Map<String, Object>> getCustomStepsSeparated() {
        return customStepsSeparated;
    }

    public void setCustomStepsSeparated(List<Map<String, Object>> customStepsSeparated) {
        this.customStepsSeparated = customStepsSeparated;
    }

    public List<Integer> getCaseIds() {
        return caseIds;
    }

    public void setCaseIds(List<Integer> caseIds) {
        this.caseIds = caseIds;
    }
}
