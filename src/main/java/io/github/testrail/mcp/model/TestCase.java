package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a TestRail test case.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {

    private Integer id;
    private String title;

    @JsonProperty("section_id")
    private Integer sectionId;

    @JsonProperty("template_id")
    private Integer templateId;

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("priority_id")
    private Integer priorityId;

    @JsonProperty("milestone_id")
    private Integer milestoneId;

    private String refs;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_on")
    private Long createdOn;

    @JsonProperty("updated_by")
    private Integer updatedBy;

    @JsonProperty("updated_on")
    private Long updatedOn;

    private String estimate;

    @JsonProperty("estimate_forecast")
    private String estimateForecast;

    @JsonProperty("suite_id")
    private Integer suiteId;

    @JsonProperty("custom_preconds")
    private String preconditions;

    @JsonProperty("custom_steps")
    private String steps;

    @JsonProperty("custom_expected")
    private String expectedResult;

    @JsonProperty("custom_steps_separated")
    private List<TestStep> stepsSeparated;

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

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Integer priorityId) {
        this.priorityId = priorityId;
    }

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getRefs() {
        return refs;
    }

    public void setRefs(String refs) {
        this.refs = refs;
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

    public String getEstimate() {
        return estimate;
    }

    public void setEstimate(String estimate) {
        this.estimate = estimate;
    }

    public String getEstimateForecast() {
        return estimateForecast;
    }

    public void setEstimateForecast(String estimateForecast) {
        this.estimateForecast = estimateForecast;
    }

    public Integer getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(Integer suiteId) {
        this.suiteId = suiteId;
    }

    public String getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(String preconditions) {
        this.preconditions = preconditions;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public List<TestStep> getStepsSeparated() {
        return stepsSeparated;
    }

    public void setStepsSeparated(List<TestStep> stepsSeparated) {
        this.stepsSeparated = stepsSeparated;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", sectionId=" + sectionId +
                ", priorityId=" + priorityId +
                '}';
    }
}
