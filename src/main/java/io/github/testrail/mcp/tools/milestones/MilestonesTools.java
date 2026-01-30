package io.github.testrail.mcp.tools.milestones;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Milestone;
import io.github.testrail.mcp.model.OperationResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP tools for managing TestRail milestones.
 */
@Component
public class MilestonesTools {
    
    private final TestrailApiClient apiClient;
    
    public MilestonesTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    @InternalTool(
            name = "get_milestone",
            description = """
                    Retrieves a milestone by ID from TestRail.
                    Returns complete milestone details including name, description, dates, completion status, and sub-milestones.
                    
                    **When to use:** Use this tool when you need to check milestone details, verify due dates,
                    review completion status, or understand milestone hierarchy.
                    
                    **Might lead to:** update_milestone (to modify), get_milestones (to see all milestones),
                    get_plans (to see plans linked to milestone).
                    
                    **Example prompts:**
                    - "Show me milestone 5"
                    - "Get the details of milestone 10"
                    - "What's the due date for milestone 3?"
                    """,
            category = "milestones",
            examples = {
                    "execute_tool('get_milestone', {milestoneId: 5})",
                    "execute_tool('get_milestone', {milestoneId: 10})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "milestone", "details", "status"}
    )
    public Milestone getMilestone(
        @InternalToolParam(description = "The unique ID of the milestone to retrieve")
        Integer milestoneId
    ) {
        return apiClient.getMilestone(milestoneId);
    }
    
    @InternalTool(
            name = "get_milestones",
            description = """
                    Retrieves all milestones for a project with optional filtering.
                    Supports filtering by completion status, started status, and pagination.
                    
                    **When to use:** Use this tool when you need to list all milestones in a project,
                    find upcoming milestones, review completed milestones, or plan future releases.
                    
                    **Filters available:**
                    - **is_completed**: true for completed, false for open, null for all
                    - **is_started**: true for started, false for upcoming, null for all
                    - **limit/offset**: Paginate through large result sets
                    
                    **Might lead to:** get_milestone (for details), add_milestone (to create new),
                    get_plans (to see plans for milestone).
                    
                    **Example prompts:**
                    - "List all milestones in project 1"
                    - "Show me completed milestones for project 2"
                    - "Get upcoming milestones in project 3"
                    """,
            category = "milestones",
            examples = {
                    "execute_tool('get_milestones', {projectId: 1})",
                    "execute_tool('get_milestones', {projectId: 2, isCompleted: true})",
                    "execute_tool('get_milestones', {projectId: 3, isStarted: false, limit: 20})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "milestones", "all", "upcoming", "completed"}
    )
    public Object[] getMilestones(
        @InternalToolParam(description = "The ID of the project to retrieve milestones for")
        Integer projectId,
        
        @InternalToolParam(description = "Filter by completion status: true for completed, false for open, null for all", required = false)
        Boolean isCompleted,
        
        @InternalToolParam(description = "Filter by started status: true for started, false for upcoming, null for all", required = false)
        Boolean isStarted,
        
        @InternalToolParam(description = "Maximum number of milestones to return (default 250)", required = false, defaultValue = "250")
        Integer limit,
        
        @InternalToolParam(description = "Number of milestones to skip for pagination", required = false, defaultValue = "0")
        Integer offset
    ) {
        return apiClient.getMilestones(projectId, isCompleted, isStarted, limit, offset);
    }
    
    @InternalTool(
            name = "add_milestone",
            description = """
                    Creates a new milestone in a project.
                    Requires a name and optionally accepts description, due date, start date, parent milestone, and references.
                    
                    **When to use:** Use this tool when you need to create a new release milestone,
                    plan a sprint, set up project phases, or organize testing cycles.
                    
                    **Required fields:**
                    - **name**: The name of the milestone
                    
                    **Optional fields:**
                    - **description**: Detailed description
                    - **due_on**: Due date as Unix timestamp
                    - **start_on**: Start date as Unix timestamp
                    - **parent_id**: Parent milestone for hierarchy
                    - **refs**: Comma-separated references/requirements
                    
                    **Might lead to:** get_milestone (to verify), update_milestone (to modify),
                    add_plan (to create plan for milestone).
                    
                    **Example prompts:**
                    - "Create milestone 'Sprint 5' in project 1"
                    - "Add a new milestone for Release 2.0 in project 3"
                    - "Create milestone with due date January 1, 2025"
                    """,
            category = "milestones",
            examples = {
                    "execute_tool('add_milestone', {projectId: 1, name: 'Sprint 5'})",
                    "execute_tool('add_milestone', {projectId: 3, name: 'Release 2.0', dueOn: 1735689600, description: 'Major release'})"
            },
            keywords = {"add", "create", "new", "milestone", "sprint", "release", "phase"}
    )
    public Milestone addMilestone(
        @InternalToolParam(description = "The ID of the project to add the milestone to")
        Integer projectId,
        
        @InternalToolParam(description = "The name of the milestone")
        String name,
        
        @InternalToolParam(description = "The description of the milestone (optional)", required = false)
        String description,
        
        @InternalToolParam(description = "The due date as Unix timestamp (optional)", required = false)
        Long dueOn,
        
        @InternalToolParam(description = "The start date as Unix timestamp (optional)", required = false)
        Long startOn,
        
        @InternalToolParam(description = "The ID of the parent milestone (optional)", required = false)
        Integer parentId,
        
        @InternalToolParam(description = "Comma-separated list of references/requirements (optional)", required = false)
        String refs
    ) {
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("name", name);
        if (description != null) milestone.put("description", description);
        if (dueOn != null) milestone.put("due_on", dueOn);
        if (startOn != null) milestone.put("start_on", startOn);
        if (parentId != null) milestone.put("parent_id", parentId);
        if (refs != null) milestone.put("refs", refs);
        
        return apiClient.addMilestone(projectId, milestone);
    }
    
    @InternalTool(
            name = "update_milestone",
            description = """
                    Updates an existing milestone.
                    Supports partial updates - only provided fields will be modified.
                    Can update name, description, dates, completion status, started status, and parent milestone.
                    
                    **When to use:** Use this tool when you need to modify milestone details,
                    change dates, mark as completed/started, or reorganize milestone hierarchy.
                    
                    **Updatable fields:**
                    - **name**: Milestone name
                    - **description**: Description
                    - **due_on/start_on**: Dates as Unix timestamps
                    - **is_completed**: Mark as completed or open
                    - **is_started**: Mark as started or upcoming
                    - **parent_id**: Parent milestone for hierarchy
                    
                    **Might lead to:** get_milestone (to verify), get_milestones (to see updated list).
                    
                    **Example prompts:**
                    - "Mark milestone 5 as completed"
                    - "Update milestone 10 due date to January 15, 2025"
                    - "Change milestone 3 name to 'Sprint 6'"
                    """,
            category = "milestones",
            examples = {
                    "execute_tool('update_milestone', {milestoneId: 5, isCompleted: true})",
                    "execute_tool('update_milestone', {milestoneId: 10, dueOn: 1736899200})",
                    "execute_tool('update_milestone', {milestoneId: 3, name: 'Sprint 6'})"
            },
            keywords = {"update", "modify", "change", "edit", "revise", "milestone", "complete", "start"}
    )
    public Milestone updateMilestone(
        @InternalToolParam(description = "The ID of the milestone to update")
        Integer milestoneId,
        
        @InternalToolParam(description = "The new name of the milestone (optional)", required = false)
        String name,
        
        @InternalToolParam(description = "The new description (optional)", required = false)
        String description,
        
        @InternalToolParam(description = "The new due date as Unix timestamp (optional)", required = false)
        Long dueOn,
        
        @InternalToolParam(description = "The new start date as Unix timestamp (optional)", required = false)
        Long startOn,
        
        @InternalToolParam(description = "Mark as completed (true) or open (false)", required = false)
        Boolean isCompleted,
        
        @InternalToolParam(description = "Mark as started (true) or upcoming (false)", required = false)
        Boolean isStarted,
        
        @InternalToolParam(description = "The new parent milestone ID (optional)", required = false)
        Integer parentId
    ) {
        Map<String, Object> milestone = new HashMap<>();
        if (name != null) milestone.put("name", name);
        if (description != null) milestone.put("description", description);
        if (dueOn != null) milestone.put("due_on", dueOn);
        if (startOn != null) milestone.put("start_on", startOn);
        if (isCompleted != null) milestone.put("is_completed", isCompleted);
        if (isStarted != null) milestone.put("is_started", isStarted);
        if (parentId != null) milestone.put("parent_id", parentId);
        
        return apiClient.updateMilestone(milestoneId, milestone);
    }
    
    @InternalTool(
            name = "delete_milestone",
            description = """
                    Deletes a milestone permanently.
                    
                    **WARNING: This action cannot be undone.**
                    
                    **When to use:** Use this tool ONLY when you need to remove a milestone created by mistake
                    or a milestone that is no longer needed. Be careful as this is permanent.
                    
                    **Might lead to:** get_milestones (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete milestone 10"
                    - "Remove milestone 5 permanently"
                    """,
            category = "milestones",
            examples = {
                    "execute_tool('delete_milestone', {milestoneId: 10})",
                    "execute_tool('delete_milestone', {milestoneId: 5})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "milestone", "cleanup"}
    )
    public OperationResult deleteMilestone(
        @InternalToolParam(description = "The ID of the milestone to delete")
        Integer milestoneId
    ) {
        apiClient.deleteMilestone(milestoneId);
        return OperationResult.success("Milestone deleted successfully");
    }
}
