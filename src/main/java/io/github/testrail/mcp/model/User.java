package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail user.
 */
public class User {
    
    private Integer id;
    private String name;
    private String email;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_admin")
    private Boolean isAdmin;
    
    @JsonProperty("role_id")
    private Integer roleId;
    
    private String role;
    
    @JsonProperty("email_notifications")
    private Boolean emailNotifications;
    
    @JsonProperty("group_ids")
    private Integer[] groupIds;
    
    @JsonProperty("mfa_required")
    private Boolean mfaRequired;
    
    @JsonProperty("sso_enabled")
    private Boolean ssoEnabled;
    
    @JsonProperty("assigned_projects")
    private Integer[] assignedProjects;
    
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsAdmin() {
        return isAdmin;
    }
    
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Boolean getEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public Integer[] getGroupIds() {
        return groupIds;
    }
    
    public void setGroupIds(Integer[] groupIds) {
        this.groupIds = groupIds;
    }
    
    public Boolean getMfaRequired() {
        return mfaRequired;
    }
    
    public void setMfaRequired(Boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }
    
    public Boolean getSsoEnabled() {
        return ssoEnabled;
    }
    
    public void setSsoEnabled(Boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }
    
    public Integer[] getAssignedProjects() {
        return assignedProjects;
    }
    
    public void setAssignedProjects(Integer[] assignedProjects) {
        this.assignedProjects = assignedProjects;
    }
}
