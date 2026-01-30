package io.github.testrail.mcp.tools.projects;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for TestRail project operations.
 */
@Component
public class ProjectsTools {

    private static final Logger log = LoggerFactory.getLogger(ProjectsTools.class);

    private final TestrailApiClient apiClient;

    public ProjectsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_project",
            description = """
                    Retrieves detailed information about a specific TestRail project by its ID.
                    Returns project name, announcement, suite mode, and completion status.
                    
                    **When to use:** Use this tool when you need to check project settings or status,
                    verify project configuration before creating test suites/runs, or get project details for reporting.
                    
                    **Might lead to:** get_test_cases (to list cases), get_runs (to see test runs),
                    get_sections (to explore structure), update_project (to modify settings).
                    
                    **Example prompts:**
                    - "Show me project 1"
                    - "Get details of the Mobile App project"
                    - "What's the status of project 5?"
                    """,
            category = "projects",
            examples = {
                    "execute_tool('get_project', {projectId: 1})",
                    "execute_tool('get_project', {projectId: 5})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "project", "details", "status"}
    )
    public Project getProject(
            @InternalToolParam(description = "The unique identifier of the project.")
            Integer projectId
    ) {
        log.info("Tool: get_project called with projectId={}", projectId);
        return apiClient.getProject(projectId);
    }

    @InternalTool(
            name = "get_projects",
            description = """
                    Retrieves all projects from TestRail with optional filters.
                    Returns basic project information including name, announcement, and suite mode.
                    
                    **When to use:** Use this tool when you need to see all available projects,
                    explore what testing initiatives exist, or find a project ID for further operations.
                    Filter by completion status to see only active or completed projects.
                    
                    **Might lead to:** get_project (for detailed info), get_test_cases (to explore),
                    add_project (to create new).
                    
                    **Example prompts:**
                    - "List all projects"
                    - "Show me active projects in TestRail"
                    - "What completed projects do we have?"
                    - "Get the first 10 projects"
                    """,
            category = "projects",
            examples = {
                    "execute_tool('get_projects', {})",
                    "execute_tool('get_projects', {isCompleted: false})",
                    "execute_tool('get_projects', {isCompleted: true, limit: 10})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "projects", "all", "active", "completed"}
    )
    public List<Project> getProjects(
            @InternalToolParam(description = "Filter by completion status: true for completed projects, false for active projects, null for all", required = false) Boolean isCompleted,
            @InternalToolParam(description = "Maximum number of projects to return (1-250)", required = false, defaultValue = "250") Integer limit,
            @InternalToolParam(description = "Number of projects to skip for pagination", required = false, defaultValue = "0") Integer offset
    ) {
        log.info("Tool: get_projects called with filters - isCompleted: {}, limit: {}, offset: {}", isCompleted, limit, offset);
        return apiClient.getProjects(isCompleted, limit, offset);
    }

    @InternalTool(
            name = "add_project",
            description = """
                    Creates a new project in TestRail.
                    Projects are the top-level containers for test suites, cases, and runs.
                    Requires admin privileges.
                    
                    **When to use:** Use this tool when you need to set up a new testing initiative,
                    create a project for a new product/feature, or establish separate testing areas for different teams.
                    
                    **Might lead to:** add_section (to create sections), add_test_case (to add cases),
                    get_project (to verify creation).
                    
                    **Example prompts:**
                    - "Create a new project called 'Mobile App v2'"
                    - "Add a project for API testing"
                    - "Set up a new TestRail project for the checkout feature"
                    """,
            category = "projects",
            examples = {
                    "execute_tool('add_project', {name: 'Mobile App v2'})",
                    "execute_tool('add_project', {name: 'API Testing', suiteMode: 3})",
                    "execute_tool('add_project', {name: 'Checkout Feature', announcement: 'Testing checkout flow', showAnnouncement: true})"
            },
            keywords = {"add", "create", "new", "project", "setup", "initialize"}
    )
    public Project addProject(
            @InternalToolParam(description = "The name of the project to create.")
            String name,
            @InternalToolParam(description = "An optional announcement to display on the project's overview page.", required = false)
            String announcement,
            @InternalToolParam(description = "Whether to show the announcement on the project page.", required = false)
            Boolean showAnnouncement,
            @InternalToolParam(description = "Suite mode: 1=Single Suite, 2=Single Suite with Baselines, 3=Multiple Suites. Default is 1.", required = false, defaultValue = "1")
            Integer suiteMode
    ) {
        log.info("Tool: add_project called with name={}", name);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        if (announcement != null) data.put("announcement", announcement);
        if (showAnnouncement != null) data.put("show_announcement", showAnnouncement);
        if (suiteMode != null) data.put("suite_mode", suiteMode);

        return apiClient.addProject(data);
    }

    @InternalTool(
            name = "update_project",
            description = """
                    Updates an existing project's settings in TestRail.
                    
                    **When to use:** Use this tool when you need to change a project's name,
                    update the announcement, or modify project settings.
                    
                    **Might lead to:** get_project (to verify changes).
                    
                    **Example prompts:**
                    - "Rename project 1 to 'Mobile App v3'"
                    - "Update the announcement for project 5"
                    - "Mark project 10 as completed"
                    """,
            category = "projects",
            examples = {
                    "execute_tool('update_project', {projectId: 1, name: 'Mobile App v3'})",
                    "execute_tool('update_project', {projectId: 5, announcement: 'New testing phase'})",
                    "execute_tool('update_project', {projectId: 10, isCompleted: true})"
            },
            keywords = {"update", "modify", "change", "edit", "rename", "project", "settings"}
    )
    public Project updateProject(
            @InternalToolParam(description = "The ID of the project to update.")
            Integer projectId,
            @InternalToolParam(description = "New name for the project.", required = false)
            String name,
            @InternalToolParam(description = "New announcement text.", required = false)
            String announcement,
            @InternalToolParam(description = "Whether to show the announcement.", required = false)
            Boolean showAnnouncement,
            @InternalToolParam(description = "Whether to mark the project as completed.", required = false)
            Boolean isCompleted
    ) {
        log.info("Tool: update_project called for projectId={}", projectId);

        Map<String, Object> data = new HashMap<>();
        if (name != null) data.put("name", name);
        if (announcement != null) data.put("announcement", announcement);
        if (showAnnouncement != null) data.put("show_announcement", showAnnouncement);
        if (isCompleted != null) data.put("is_completed", isCompleted);

        return apiClient.updateProject(projectId, data);
    }

    @InternalTool(
            name = "delete_project",
            description = """
                    Permanently deletes a project from TestRail.
                    
                    **WARNING: This action cannot be undone. All test suites, cases, runs, and results will be deleted.**
                    
                    **When to use:** Use this tool ONLY when you need to completely remove a project
                    and all its data. This is typically used for cleanup of test or obsolete projects.
                    
                    **Might lead to:** get_projects (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete project 99"
                    - "Remove the test project"
                    """,
            category = "projects",
            examples = {
                    "execute_tool('delete_project', {projectId: 99})",
                    "execute_tool('delete_project', {projectId: 10})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "project", "cleanup"}
    )
    public OperationResult deleteProject(
            @InternalToolParam(description = "The ID of the project to delete. WARNING: This permanently removes the project and ALL its data.")
            Integer projectId
    ) {
        log.warn("Tool: delete_project called for projectId={}", projectId);
        apiClient.deleteProject(projectId);
        return OperationResult.success("Project " + projectId + " and all its data have been permanently deleted.");
    }
}
