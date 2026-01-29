package io.github.testrail.mcp.tools.annotation;

/**
 * Categories for grouping TestRail MCP tools.
 */
public enum ToolCategory {

    CASES("Test Cases", "Operations related to test case management"),
    PROJECTS("Projects", "Operations related to project management"),
    RUNS("Test Runs", "Operations related to test run management"),
    RESULTS("Test Results", "Operations related to test results"),
    SECTIONS("Sections", "Operations related to test case sections"),
    PLANS("Test Plans", "Operations related to test plan management"),
    TESTS("Tests", "Operations related to individual test instances"),
    USERS("Users", "Operations related to user management"),
    SUITES("Test Suites", "Operations related to test suite management"),
    MILESTONES("Milestones", "Operations related to milestone management");

    private final String displayName;
    private final String description;

    ToolCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
