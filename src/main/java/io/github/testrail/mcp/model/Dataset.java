package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Dataset {
    private Integer id;
    private String name;
    private List<DatasetVariable> variables;
}
