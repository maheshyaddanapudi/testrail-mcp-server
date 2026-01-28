package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a TestRail test run.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRun {

    private Integer id;

    @JsonProperty("suite_id")
    private Integer suiteId;

    private String name;
    private String description;

    @JsonProperty("milestone_id")
    private Integer milestoneId;

    @JsonProperty("assignedto_id")
    private Integer assignedtoId;

    @JsonProperty("include_all")
    private Boolean includeAll;

    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @JsonProperty("completed_on")
    private Long completedOn;

    private String config;

    @JsonProperty("config_ids")
    private List<Integer> configIds;

    @JsonProperty("passed_count")
    private Integer passedCount;

    @JsonProperty("blocked_count")
    private Integer blockedCount;

    @JsonProperty("untested_count")
    private Integer untestedCount;

    @JsonProperty("retest_count")
    private Integer retestCount;

    @JsonProperty("failed_count")
    private Integer failedCount;

    @JsonProperty("custom_status1_count")
    private Integer customStatus1Count;

    @JsonProperty("custom_status2_count")
    private Integer customStatus2Count;

    @JsonProperty("custom_status3_count")
    private Integer customStatus3Count;

    @JsonProperty("custom_status4_count")
    private Integer customStatus4Count;

    @JsonProperty("custom_status5_count")
    private Integer customStatus5Count;

    @JsonProperty("custom_status6_count")
    private Integer customStatus6Count;

    @JsonProperty("custom_status7_count")
    private Integer customStatus7Count;

    @JsonProperty("project_id")
    private Integer projectId;

    @JsonProperty("plan_id")
    private Integer planId;

    @JsonProperty("created_on")
    private Long createdOn;

    @JsonProperty("created_by")
    private Integer createdBy;

    private String url;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(Integer suiteId) {
        this.suiteId = suiteId;
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

    public Integer getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Integer milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Integer getAssignedtoId() {
        return assignedtoId;
    }

    public void setAssignedtoId(Integer assignedtoId) {
        this.assignedtoId = assignedtoId;
    }

    public Boolean getIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(Boolean includeAll) {
        this.includeAll = includeAll;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public List<Integer> getConfigIds() {
        return configIds;
    }

    public void setConfigIds(List<Integer> configIds) {
        this.configIds = configIds;
    }

    public Integer getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(Integer passedCount) {
        this.passedCount = passedCount;
    }

    public Integer getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(Integer blockedCount) {
        this.blockedCount = blockedCount;
    }

    public Integer getUntestedCount() {
        return untestedCount;
    }

    public void setUntestedCount(Integer untestedCount) {
        this.untestedCount = untestedCount;
    }

    public Integer getRetestCount() {
        return retestCount;
    }

    public void setRetestCount(Integer retestCount) {
        this.retestCount = retestCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getCustomStatus1Count() {
        return customStatus1Count;
    }

    public void setCustomStatus1Count(Integer customStatus1Count) {
        this.customStatus1Count = customStatus1Count;
    }

    public Integer getCustomStatus2Count() {
        return customStatus2Count;
    }

    public void setCustomStatus2Count(Integer customStatus2Count) {
        this.customStatus2Count = customStatus2Count;
    }

    public Integer getCustomStatus3Count() {
        return customStatus3Count;
    }

    public void setCustomStatus3Count(Integer customStatus3Count) {
        this.customStatus3Count = customStatus3Count;
    }

    public Integer getCustomStatus4Count() {
        return customStatus4Count;
    }

    public void setCustomStatus4Count(Integer customStatus4Count) {
        this.customStatus4Count = customStatus4Count;
    }

    public Integer getCustomStatus5Count() {
        return customStatus5Count;
    }

    public void setCustomStatus5Count(Integer customStatus5Count) {
        this.customStatus5Count = customStatus5Count;
    }

    public Integer getCustomStatus6Count() {
        return customStatus6Count;
    }

    public void setCustomStatus6Count(Integer customStatus6Count) {
        this.customStatus6Count = customStatus6Count;
    }

    public Integer getCustomStatus7Count() {
        return customStatus7Count;
    }

    public void setCustomStatus7Count(Integer customStatus7Count) {
        this.customStatus7Count = customStatus7Count;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the total count of all tests in the run.
     *
     * @return total test count
     */
    public int getTotalCount() {
        int total = 0;
        if (passedCount != null) total += passedCount;
        if (blockedCount != null) total += blockedCount;
        if (untestedCount != null) total += untestedCount;
        if (retestCount != null) total += retestCount;
        if (failedCount != null) total += failedCount;
        return total;
    }

    @Override
    public String toString() {
        return "TestRun{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isCompleted=" + isCompleted +
                ", passedCount=" + passedCount +
                ", failedCount=" + failedCount +
                '}';
    }
}
