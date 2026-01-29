package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents a TestRail result field (custom field for test results).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultField {
    
    private Long id;
    
    @JsonProperty("type_id")
    private Integer typeId;
    
    private String name;
    
    @JsonProperty("system_name")
    private String systemName;
    
    private String label;
    private String description;
    
    @JsonProperty("display_order")
    private Integer displayOrder;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_system")
    private Boolean isSystem;
    
    @JsonProperty("is_multi")
    private Boolean isMulti;
    
    @JsonProperty("include_all")
    private Boolean includeAll;
    
    @JsonProperty("template_ids")
    private List<Long> templateIds;
    
    private List<Map<String, Object>> configs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsMulti() {
        return isMulti;
    }

    public void setIsMulti(Boolean isMulti) {
        this.isMulti = isMulti;
    }

    public Boolean getIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(Boolean includeAll) {
        this.includeAll = includeAll;
    }

    public List<Long> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(List<Long> templateIds) {
        this.templateIds = templateIds;
    }

    public List<Map<String, Object>> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Map<String, Object>> configs) {
        this.configs = configs;
    }
}
