package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Templates API.
 * Templates define field layouts for test cases and results, determining which fields are visible and required.
 * Common templates include: Test Case (Text), Test Case (Steps), Exploratory Session.
 */
@Component
public class TemplatesTools {

    private final TestrailApiClient apiClient;

    public TemplatesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = """
            Retrieves all available templates (field layouts) for a project.
            Templates define which fields are visible and required when creating/editing test cases.
            Different templates support different testing workflows.
            Requires TestRail 5.2 or later.
            
            Common default templates include:
            - **Test Case (Text)** - Simple text-based test case description
            - **Test Case (Steps)** - Step-by-step test case with expected results
            - **Exploratory Session** - Template for exploratory testing sessions
            
            Each template has:
            - **id**: Unique identifier used when creating test cases
            - **name**: Display name of the template
            - **is_default**: Boolean indicating if this is the default template for new cases
            
            **When to use:** Use this tool when you need to understand available test case templates,
            get template IDs for creating test cases with specific field layouts, check which template is the default,
            understand what testing workflows are supported in a project, or prepare test case import workflows.
            
            **Might lead to:** add_case (to create case with specific template), get_case_fields (to see fields per template).
            
            **Example prompts:**
            - "Show me all templates in project 1"
            - "What templates are available for project 5?"
            - "What's the ID for the 'Test Case (Steps)' template?"
            - "Which template is the default for new test cases?"
            - "List all field layouts available"
            """)
    public Object[] getTemplates(Integer projectId) {
        return apiClient.getTemplates(projectId);
    }
}
