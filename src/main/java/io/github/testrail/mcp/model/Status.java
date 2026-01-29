package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a TestRail status.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    
    private Long id;
    private String name;
    private String label;
    
    @JsonProperty("color_dark")
    private Integer colorDark;
    
    @JsonProperty("color_medium")
    private Integer colorMedium;
    
    @JsonProperty("color_bright")
    private Integer colorBright;
    
    @JsonProperty("is_system")
    private Boolean isSystem;
    
    @JsonProperty("is_untested")
    private Boolean isUntested;
    
    @JsonProperty("is_final")
    private Boolean isFinal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getColorDark() {
        return colorDark;
    }

    public void setColorDark(Integer colorDark) {
        this.colorDark = colorDark;
    }

    public Integer getColorMedium() {
        return colorMedium;
    }

    public void setColorMedium(Integer colorMedium) {
        this.colorMedium = colorMedium;
    }

    public Integer getColorBright() {
        return colorBright;
    }

    public void setColorBright(Integer colorBright) {
        this.colorBright = colorBright;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsUntested() {
        return isUntested;
    }

    public void setIsUntested(Boolean isUntested) {
        this.isUntested = isUntested;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
