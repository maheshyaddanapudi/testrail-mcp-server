package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents a TestRail test (individual instance of a test case in a run).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Test {

    private Integer id;

    @JsonProperty("case_id")
    private Integer caseId;

    @JsonProperty("run_id")
    private Integer runId;

    @JsonProperty("status_id")
    private Integer statusId;

    @JsonProperty("assignedto_id")
    private Integer assignedtoId;

    private String title;

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("priority_id")
    private Integer priorityId;

    @JsonProperty("milestone_id")
    private Integer milestoneId;

    private String refs;

    private String estimate;

    @JsonProperty("estimate_forecast")
    private String estimateForecast;

    // Custom fields (examples - actual fields depend on TestRail configuration)
    @JsonProperty("custom_expected")
    private String customExpected;

    @JsonProperty("custom_preconds")
    private String customPreconds;

    @JsonProperty("custom_steps_separated")
    private List<TestStep> customStepsSeparated;

    // Labels
    private List<Label> labels;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getAssignedtoId() {
        return assignedtoId;
    }

    public void setAssignedtoId(Integer assignedtoId) {
        this.assignedtoId = assignedtoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getCustomExpected() {
        return customExpected;
    }

    public void setCustomExpected(String customExpected) {
        this.customExpected = customExpected;
    }

    public String getCustomPreconds() {
        return customPreconds;
    }

    public void setCustomPreconds(String customPreconds) {
        this.customPreconds = customPreconds;
    }

    public List<TestStep> getCustomStepsSeparated() {
        return customStepsSeparated;
    }

    public void setCustomStepsSeparated(List<TestStep> customStepsSeparated) {
        this.customStepsSeparated = customStepsSeparated;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", caseId=" + caseId +
                ", runId=" + runId +
                ", title='" + title + '\'' +
                ", statusId=" + statusId +
                '}';
    }

    /**
     * Represents a label in TestRail.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        private Integer id;
        private String title;

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

        @Override
        public String toString() {
            return "Label{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
