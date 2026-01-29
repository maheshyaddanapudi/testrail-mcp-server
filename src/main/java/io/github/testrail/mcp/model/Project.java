package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail project.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

    private Integer id;
    private String name;
    private String announcement;

    @JsonProperty("show_announcement")
    private Boolean showAnnouncement;

    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @JsonProperty("completed_on")
    private Long completedOn;

    @JsonProperty("suite_mode")
    private Integer suiteMode;

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

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Boolean getShowAnnouncement() {
        return showAnnouncement;
    }

    public void setShowAnnouncement(Boolean showAnnouncement) {
        this.showAnnouncement = showAnnouncement;
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

    public Integer getSuiteMode() {
        return suiteMode;
    }

    public void setSuiteMode(Integer suiteMode) {
        this.suiteMode = suiteMode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the suite mode as a human-readable string.
     *
     * @return suite mode description
     */
    public String getSuiteModeDescription() {
        if (suiteMode == null) {
            return "Unknown";
        }
        return switch (suiteMode) {
            case 1 -> "Single Suite";
            case 2 -> "Single Suite with Baselines";
            case 3 -> "Multiple Suites";
            default -> "Unknown (" + suiteMode + ")";
        };
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isCompleted=" + isCompleted +
                ", suiteMode=" + suiteMode +
                '}';
    }
}
