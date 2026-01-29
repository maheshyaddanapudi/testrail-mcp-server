package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Label {
    private Integer id;
    private String name;
    private String title;
    
    @JsonProperty("created_by")
    private Integer createdBy;
    
    @JsonProperty("created_on")
    private Long createdOn;
}
