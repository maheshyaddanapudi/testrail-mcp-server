package io.github.testrail.mcp.tools.casefields;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.CaseField;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP tools for TestRail Case Fields API.
 * Case fields are custom fields that extend test case data structure beyond built-in fields.
 * They allow organizations to capture domain-specific information (e.g., "Affected Component", "Test Data Requirements").
 */
@Component
public class CaseFieldsTools {

    private final TestrailApiClient apiClient;

    public CaseFieldsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_case_fields",
            description = """
                    Retrieves all available test case custom field definitions.
                    Returns field metadata including type, label, description, configurations, and project applicability.
                    Custom fields can be global (all projects) or project-specific.
                    
                    Field types include: String, Integer, Text, URL, Checkbox, Dropdown, User, Date, Milestone, Steps, Multi-select.
                    
                    **When to use:** Use this tool when you need to understand what custom fields are available,
                    check field configurations before creating/updating test cases, discover field system names for API usage,
                    audit custom field setup across projects, or verify field types and options.
                    
                    **Might lead to:** add_case_field (to create new field), add_case or update_case (to use fields).
                    
                    **Example prompts:**
                    - "Show me all custom case fields"
                    - "What custom fields are available for test cases?"
                    - "List all case field definitions and their types"
                    - "What's the system name for the 'Affected Component' field?"
                    """,
            category = "custom-fields",
            examples = {
                    "execute_tool('get_case_fields', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "case", "fields", "custom", "definitions", "metadata"}
    )
    public Object[] getCaseFields() {
        return apiClient.getCaseFields();
    }

    @InternalTool(
            name = "add_case_field",
            description = """
                    Creates a new custom field for test cases.
                    Allows extending test case data structure with organization-specific fields.
                    
                    Supports 11 field types: String, Integer, Text, URL, Checkbox, Dropdown, User, Date, Milestone, Steps, Multiselect.
                    Can be configured as global (all projects) or project-specific.
                    Field names are automatically prefixed with "custom_" (e.g., "my_field" becomes "custom_my_field").
                    
                    **When to use:** Use this tool when you need to capture additional test case information not covered by built-in fields,
                    standardize custom data collection across projects, add domain-specific metadata to test cases,
                    or support specialized testing workflows (e.g., regulatory compliance, performance metrics).
                    
                    **Might lead to:** get_case_fields (to verify creation), add_case or update_case (to populate new field).
                    
                    **Example prompts:**
                    - "Create a dropdown custom field called 'Affected Component' with options: Frontend, Backend, Database"
                    - "Add a text custom field 'Test Data Requirements' for all projects"
                    - "Create a multiselect field 'Tags' with options: Smoke, Regression, Integration"
                    - "Add a user field 'Test Designer' to track who designed each test"
                    """,
            category = "custom-fields",
            examples = {
                    "execute_tool('add_case_field', {caseField: {type_id: 6, name: 'affected_component', label: 'Affected Component', configs: [{options: {items: '1, Frontend\\n2, Backend\\n3, Database'}}]}})"
            },
            keywords = {"add", "create", "new", "case", "field", "custom", "extend", "metadata"}
    )
    public CaseField addCaseField(
            @InternalToolParam(description = "Case field configuration map with keys: type_id (1-11), name, label, description, configs, and other properties")
            Map<String, Object> caseField
    ) {
        return apiClient.addCaseField(caseField);
    }
}
