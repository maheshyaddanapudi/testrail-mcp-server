package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Group {
    private Integer id;
    private String name;
    
    @JsonProperty("user_ids")
    private List<Integer> userIds;
}
