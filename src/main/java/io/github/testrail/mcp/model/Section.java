package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail section (folder for organizing test cases).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {

    private Integer id;

    @JsonProperty("suite_id")
    private Integer suiteId;

    private String name;
    private String description;

    @JsonProperty("parent_id")
    private Integer parentId;

    @JsonProperty("display_order")
    private Integer displayOrder;

    private Integer depth;

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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    /**
     * Returns whether this section is a root section (has no parent).
     *
     * @return true if this is a root section
     */
    public boolean isRootSection() {
        return parentId == null;
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", depth=" + depth +
                '}';
    }
}
