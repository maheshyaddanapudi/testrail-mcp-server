package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an attachment in TestRail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    private Object id; // Can be integer (legacy) or string (UUID after 7.1)
    private String name;
    private String filename;
    private Long size;
    
    @JsonProperty("created_on")
    private Long createdOn;
    
    @JsonProperty("project_id")
    private Integer projectId;
    
    @JsonProperty("case_id")
    private Integer caseId;
    
    @JsonProperty("user_id")
    private Integer userId;
    
    @JsonProperty("result_id")
    private Integer resultId;
    
    @JsonProperty("entity_type")
    private String entityType;
    
    @JsonProperty("entity_id")
    private String entityId;
    
    @JsonProperty("data_id")
    private String dataId;
    
    private String filetype;
    
    @JsonProperty("legacy_id")
    private Integer legacyId;
    
    @JsonProperty("is_image")
    private Boolean isImage;
    
    private String icon;
    
    @JsonProperty("icon_name")
    private String iconName;
    
    @JsonProperty("entity_attachments_id")
    private Integer entityAttachmentsId;
    
    @JsonProperty("client_id")
    private Integer clientId;

    // Getters and Setters
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getCaseId() {
        return caseId;
    }

    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getResultId() {
        return resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Integer getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(Integer legacyId) {
        this.legacyId = legacyId;
    }

    public Boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(Boolean isImage) {
        this.isImage = isImage;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Integer getEntityAttachmentsId() {
        return entityAttachmentsId;
    }

    public void setEntityAttachmentsId(Integer entityAttachmentsId) {
        this.entityAttachmentsId = entityAttachmentsId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
}
