package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a report template in TestRail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {
    private Integer id;
    private String name;
    private String description;
    
    @JsonProperty("report_template_id")
    private Integer reportTemplateId;
    
    @JsonProperty("project_id")
    private Integer projectId;
    
    @JsonProperty("is_cross_project")
    private Boolean isCrossProject;

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

    public Integer getReportTemplateId() {
        return reportTemplateId;
    }

    public void setReportTemplateId(Integer reportTemplateId) {
        this.reportTemplateId = reportTemplateId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Boolean getIsCrossProject() {
        return isCrossProject;
    }

    public void setIsCrossProject(Boolean isCrossProject) {
        this.isCrossProject = isCrossProject;
    }
}
