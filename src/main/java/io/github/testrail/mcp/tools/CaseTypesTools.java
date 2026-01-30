package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Case Types API.
 * Case types categorize test cases by their nature (e.g., Automated, Functionality, Performance, Security, Usability).
 * Each case type has a unique ID, name, and indicates if it's the default type.
 */
@Component
public class CaseTypesTools {

    private final TestrailApiClient apiClient;

    public CaseTypesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = """
            Retrieves all available case types in TestRail.
            Case types categorize test cases by their testing approach or focus area.
            Common types include: Automated, Functionality, Performance, Security, Usability, Compatibility, and Other.
            
            Each case type has:
            - **id**: Unique identifier used when creating/updating test cases
            - **name**: Display name of the case type
            - **is_default**: Boolean indicating if this is the default type for new cases
            
            **When to use:** Use this tool when you need to understand available case type categories,
            get case type IDs for creating/filtering test cases, check which type is the default,
            audit test case categorization options, or prepare test case import/creation workflows.
            
            **Might lead to:** add_case or update_case (to assign case type), get_cases (to filter by type).
            
            **Example prompts:**
            - "Show me all case types"
            - "What case types are available in TestRail?"
            - "What's the ID for the 'Automated' case type?"
            - "Which case type is the default?"
            - "List all test case categories"
            """)
    public Object[] getCaseTypes() {
        return apiClient.getCaseTypes();
    }
}
