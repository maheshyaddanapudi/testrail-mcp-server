package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail test result.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResult {

    private Integer id;

    @JsonProperty("test_id")
    private Integer testId;

    @JsonProperty("status_id")
    private Integer statusId;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("created_on")
    private Long createdOn;

    @JsonProperty("assignedto_id")
    private Integer assignedtoId;

    private String comment;
    private String version;
    private String elapsed;
    private String defects;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
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

    public Integer getAssignedtoId() {
        return assignedtoId;
    }

    public void setAssignedtoId(Integer assignedtoId) {
        this.assignedtoId = assignedtoId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getElapsed() {
        return elapsed;
    }

    public void setElapsed(String elapsed) {
        this.elapsed = elapsed;
    }

    public String getDefects() {
        return defects;
    }

    public void setDefects(String defects) {
        this.defects = defects;
    }

    /**
     * Returns the status as a human-readable string.
     *
     * @return status description
     */
    public String getStatusDescription() {
        if (statusId == null) {
            return "Unknown";
        }
        return switch (statusId) {
            case 1 -> "Passed";
            case 2 -> "Blocked";
            case 3 -> "Untested";
            case 4 -> "Retest";
            case 5 -> "Failed";
            default -> "Custom Status (" + statusId + ")";
        };
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "id=" + id +
                ", testId=" + testId +
                ", statusId=" + statusId +
                ", comment='" + comment + '\'' +
                '}';
    }
}
