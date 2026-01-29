package io.github.testrail.mcp.tools.sections;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail section operations.
 */
@Component
public class SectionsTools {

    private static final Logger log = LoggerFactory.getLogger(SectionsTools.class);

    private final TestrailApiClient apiClient;

    public SectionsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Tool(description = """
            Retrieves detailed information about a specific section by its ID.
            Returns section name, description, parent section, and hierarchy depth.

            **When to use:** Use this tool when you need to get details about a specific section,
            verify a section exists before adding test cases, or check section hierarchy.

            **Might lead to:** get_test_cases (to list cases in section), add_test_case (to add cases),
            update_section (to modify), get_sections (to see siblings).

            **Example prompts:**
            - "Show me section 10"
            - "Get details of the Login Tests section"
            - "What's the parent of section 25?"
            """)
    public Section getSection(
            @ToolParam(description = "The unique identifier of the section.")
            Integer sectionId
    ) {
        log.info("Tool: get_section called with sectionId={}", sectionId);
        return apiClient.getSection(sectionId);
    }

    @Tool(description = """
            Retrieves all sections (folders) for organizing test cases in a project.
            Sections form a hierarchy for categorizing test cases by feature, component, or any logical grouping.

            **When to use:** Use this tool when you need to explore project structure,
            find where to add a new test case, understand test case organization, or list available sections.

            **Might lead to:** get_section (for details), add_section (to create new),
            add_test_case (to add cases to a section), get_test_cases (to list cases in a section).

            **Example prompts:**
            - "List all sections in project 1"
            - "Show me the folder structure for project 5"
            - "What sections are in suite 10?"
            """)
    public List<Section> getSections(
            @ToolParam(description = "The ID of the project.")
            Integer projectId,
            @ToolParam(description = "The ID of the test suite (for projects with multiple suites).", required = false)
            Integer suiteId,
            @ToolParam(description = "Maximum number of results to return.", required = false)
            Integer limit,
            @ToolParam(description = "Number of results to skip for pagination.", required = false)
            Integer offset
    ) {
        log.info("Tool: get_sections called with projectId={}, suiteId={}", projectId, suiteId);
        return apiClient.getSections(projectId, suiteId, limit, offset);
    }

    @Tool(description = """
            Creates a new section (folder) in TestRail for organizing test cases.
            Sections can be nested to create a hierarchy (e.g., Feature > Module > Component).

            **When to use:** Use this tool when you need to organize test cases by feature/module,
            create folder structure for test cases, or set up sections for a new testing area.

            **Might lead to:** add_test_case (to add cases to the new section),
            add_section (to create sub-sections), get_sections (to verify creation).

            **Example prompts:**
            - "Create a section called 'Login Tests' in project 1"
            - "Add a subsection 'API Tests' under section 5"
            - "Create a new folder for checkout tests"
            """)
    public Section addSection(
            @ToolParam(description = "The ID of the project.")
            Integer projectId,
            @ToolParam(description = "The name of the section to create.")
            String name,
            @ToolParam(description = "Optional description for the section.", required = false)
            String description,
            @ToolParam(description = "The ID of the parent section (for creating sub-sections). Leave null for root section.", required = false)
            Integer parentId,
            @ToolParam(description = "The ID of the test suite (for projects with multiple suites).", required = false)
            Integer suiteId
    ) {
        log.info("Tool: add_section called for project={}, name={}", projectId, name);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        if (description != null) data.put("description", description);
        if (parentId != null) data.put("parent_id", parentId);
        if (suiteId != null) data.put("suite_id", suiteId);

        return apiClient.addSection(projectId, data);
    }

    @Tool(description = """
            Updates an existing section's name or description.

            **When to use:** Use this tool when you need to rename a section,
            update the description, or correct section information.

            **Might lead to:** get_section (to verify changes), get_sections (to see updated list).

            **Example prompts:**
            - "Rename section 10 to 'Authentication Tests'"
            - "Update the description of section 25"
            """)
    public Section updateSection(
            @ToolParam(description = "The ID of the section to update.")
            Integer sectionId,
            @ToolParam(description = "New name for the section.", required = false)
            String name,
            @ToolParam(description = "New description.", required = false)
            String description
    ) {
        log.info("Tool: update_section called for sectionId={}", sectionId);

        Map<String, Object> data = new HashMap<>();
        if (name != null) data.put("name", name);
        if (description != null) data.put("description", description);

        return apiClient.updateSection(sectionId, data);
    }

    @Tool(description = """
            Deletes a section from TestRail.

            **WARNING: This will also delete all test cases within the section.**

            By default, performs a soft delete (moves to trash). Use soft=false for permanent deletion.

            **When to use:** Use this tool when you need to remove a section and its test cases,
            clean up obsolete sections, or reorganize the project structure.

            **Might lead to:** get_sections (to verify deletion).

            **Example prompts:**
            - "Delete section 10"
            - "Remove the old API tests section"
            - "Permanently delete section 25"
            """)
    public OperationResult deleteSection(
            @ToolParam(description = "The ID of the section to delete.")
            Integer sectionId,
            @ToolParam(description = "Whether to perform a soft delete (move to trash). Default is true.", required = false)
            Boolean soft
    ) {
        boolean isSoft = soft == null || soft;
        log.warn("Tool: delete_section called for sectionId={}, soft={}", sectionId, isSoft);
        apiClient.deleteSection(sectionId, isSoft);

        String message = isSoft
                ? "Section " + sectionId + " has been moved to trash (soft delete)."
                : "Section " + sectionId + " has been permanently deleted.";
        return OperationResult.success(message);
    }

    @Tool(description = """
            Moves a section to a different location in the hierarchy.
            Can change the parent section or reorder within the same parent.

            **When to use:** Use this tool when you need to reorganize the section structure,
            move a section under a different parent, or change the display order of sections.

            **Might lead to:** get_sections (to verify new structure), get_section (to verify move).

            **Example prompts:**
            - "Move section 10 under section 5"
            - "Reorder section 15 to come after section 20"
            - "Move the Login Tests section to be a child of Authentication"
            """)
    public Section moveSection(
            @ToolParam(description = "The ID of the section to move.")
            Integer sectionId,
            @ToolParam(description = "The new parent section ID. Use null for root level.", required = false)
            Integer parentId,
            @ToolParam(description = "The ID of the section to place this after (for ordering within same parent).", required = false)
            Integer afterId
    ) {
        log.info("Tool: move_section called for sectionId={}, parentId={}, afterId={}", sectionId, parentId, afterId);

        Map<String, Object> data = new HashMap<>();
        if (parentId != null) data.put("parent_id", parentId);
        if (afterId != null) data.put("after_id", afterId);

        return apiClient.moveSection(sectionId, data);
    }
}
