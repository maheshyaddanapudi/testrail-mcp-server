package io.github.testrail.mcp.tools.statuses;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.client.TestrailApiClient;
import org.springframework.stereotype.Component;

/**
 * MCP tools for TestRail Statuses API.
 * Statuses represent the outcome of test execution (Passed, Failed, Blocked, Retest, Untested).
 * Organizations can add custom statuses beyond the default system statuses.
 */
@Component
public class StatusesTools {

    private final TestrailApiClient apiClient;

    public StatusesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_statuses",
            description = """
                    Retrieves all available test result statuses (both system and custom).
                    Statuses represent the outcome of test execution and are used when adding test results.
                    
                    Default system statuses include:
                    - **Passed** (ID: 1) - Test executed successfully
                    - **Blocked** (ID: 2) - Test cannot be executed due to dependency
                    - **Untested** (ID: 3) - Test not yet executed
                    - **Retest** (ID: 4) - Test needs to be re-executed
                    - **Failed** (ID: 5) - Test execution failed
                    
                    Organizations can add custom statuses via Administration > Customizations.
                    
                    Each status has:
                    - **id**: Unique identifier used when adding test results
                    - **name**: System name (e.g., "passed", "failed")
                    - **label**: Display name (e.g., "Passed", "Failed")
                    - **color_bright/dark/medium**: RGB color values for UI display
                    - **is_system**: Boolean indicating if it's a built-in status
                    - **is_final**: Boolean indicating if it represents a final test state
                    - **is_untested**: Boolean indicating if it represents an untested state
                    
                    **When to use:** Use this tool when you need to understand available result statuses,
                    get status IDs for adding test results, check which statuses are available for reporting,
                    identify custom statuses added by the organization, or prepare test result submission workflows.
                    
                    **Might lead to:** add_result or add_result_for_case (to submit test results with status),
                    get_results (to filter by status), add_run (to create test runs).
                    
                    **Example prompts:**
                    - "Show me all test statuses"
                    - "What statuses are available for test results?"
                    - "What's the ID for 'Failed' status?"
                    - "List all custom statuses"
                    - "What status represents a blocked test?"
                    """,
            category = "metadata",
            examples = {
                    "execute_tool('get_statuses', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "statuses", "passed", "failed", "blocked", "retest"}
    )
    public Object[] getStatuses() {
        return apiClient.getStatuses();
    }
}
