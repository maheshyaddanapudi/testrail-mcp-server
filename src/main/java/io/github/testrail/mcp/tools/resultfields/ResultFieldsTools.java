package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Result Fields API.
 * Result fields are custom fields that extend test result data beyond built-in fields (status, comment, elapsed time).
 * They allow organizations to capture additional execution information (e.g., "Defect ID", "Environment", "Build Number").
 */
@Component
public class ResultFieldsTools {

    private final TestrailApiClient apiClient;

    public ResultFieldsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = """
            Retrieves all available test result custom field definitions.
            Returns field metadata including type, label, description, configurations, and project applicability.
            Result fields extend test results with organization-specific data beyond built-in fields.
            Custom fields can be global (all projects) or project-specific.
            
            Field types include: String, Integer, Text, URL, Checkbox, Dropdown, User, Date, Milestone, Step Results, Multi-select.
            
            Built-in result fields include: status_id, comment, version, elapsed, defects, assignedto_id.
            Custom fields are prefixed with "custom_" (e.g., "custom_build_number").
            
            **When to use:** Use this tool when you need to understand what custom result fields are available,
            check field configurations before adding test results, discover field system names for API usage,
            audit custom result field setup across projects, or verify field types and options for result submission.
            
            **Might lead to:** add_result or add_result_for_case (to populate custom result fields),
            get_results (to retrieve results with custom fields).
            
            **Example prompts:**
            - "Show me all custom result fields"
            - "What custom fields are available for test results?"
            - "List all result field definitions and their types"
            - "What's the system name for the 'Build Number' result field?"
            - "What result fields can I populate when submitting test results?"
            """)
    public Object[] getResultFields() {
        return apiClient.getResultFields();
    }
}
