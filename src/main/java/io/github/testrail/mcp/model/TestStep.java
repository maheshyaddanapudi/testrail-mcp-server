package io.github.testrail.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single step in a TestRail test case (when using separated steps template).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestStep {

    private String content;
    private String expected;

    @JsonProperty("additional_info")
    private String additionalInfo;

    private String refs;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getRefs() {
        return refs;
    }

    public void setRefs(String refs) {
        this.refs = refs;
    }

    @Override
    public String toString() {
        return "TestStep{" +
                "content='" + content + '\'' +
                ", expected='" + expected + '\'' +
                '}';
    }
}
