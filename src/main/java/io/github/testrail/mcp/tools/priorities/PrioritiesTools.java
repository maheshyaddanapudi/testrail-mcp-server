package io.github.testrail.mcp.tools.priorities;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Priorities API.
 * Priorities indicate the importance or urgency of test cases for execution.
 * Common priorities include: Don't Test, Low, Medium, High, Must Test.
 */
@Component
public class PrioritiesTools {

    private final TestrailApiClient apiClient;

    public PrioritiesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_priorities",
            description = """
                    Retrieves all available test case priorities.
                    Priorities indicate the importance or urgency of test cases for execution planning and risk management.
                    Typical priorities range from "1 - Don't Test" to "4 - Must Test" or "5 - Critical".
                    
                    Each priority has:
                    - **id**: Unique identifier used when creating/updating test cases
                    - **name**: Full display name (e.g., "4 - Must Test")
                    - **short_name**: Abbreviated version (e.g., "4 - Must")
                    - **priority**: Numeric order value (higher = more important)
                    - **is_default**: Boolean indicating if this is the default priority for new cases
                    
                    **When to use:** Use this tool when you need to understand available priority levels,
                    get priority IDs for creating/filtering test cases, plan test execution based on priority,
                    identify which priority is the default, or prepare test case import/prioritization workflows.
                    
                    **Might lead to:** add_case or update_case (to assign priority), get_cases (to filter by priority),
                    add_run (to create test runs with priority-based selection).
                    
                    **Example prompts:**
                    - "Show me all test case priorities"
                    - "What priorities are available in TestRail?"
                    - "What's the ID for 'Must Test' priority?"
                    - "Which priority is the default?"
                    - "List all priority levels from low to high"
                    """,
            category = "metadata",
            examples = {
                    "execute_tool('get_priorities', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "priorities", "importance", "urgency", "critical"}
    )
    public Object[] getPriorities() {
        return apiClient.getPriorities();
    }
}
