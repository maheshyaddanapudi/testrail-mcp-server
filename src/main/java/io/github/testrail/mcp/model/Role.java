package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a user role in TestRail.
 * Requires TestRail 7.3 or later.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
    private Integer id;
    private String name;
    
    @JsonProperty("is_default")
    private Boolean isDefault;
    
    @JsonProperty("is_project_admin")
    private Boolean isProjectAdmin; // Only available in TestRail Enterprise

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

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsProjectAdmin() {
        return isProjectAdmin;
    }

    public void setIsProjectAdmin(Boolean isProjectAdmin) {
        this.isProjectAdmin = isProjectAdmin;
    }
}
