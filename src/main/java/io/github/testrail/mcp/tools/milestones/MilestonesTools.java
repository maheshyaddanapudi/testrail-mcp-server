package io.github.testrail.mcp.tools.milestones;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Milestone;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.tools.annotation.ToolCategory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
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
    
    @Tool(description = "Retrieves a milestone by ID from TestRail. " + "Returns complete milestone details including name, description, dates, completion status, and sub-milestones. " + "Example: Get milestone 5 to check its due date and completion status.")
    public Milestone getMilestone(
        @ToolParam(description = "The unique ID of the milestone to retrieve", required = true)
        Integer milestoneId
    ) {
        return apiClient.getMilestone(milestoneId);
    }
    
    @Tool(description = "Retrieves all milestones for a project with optional filtering. " + "Supports filtering by completion status, started status, and pagination. " + "Example: Get all completed milestones for project 1, or get upcoming milestones with isStarted=false.")
    public Object[] getMilestones(
        @ToolParam(description = "The ID of the project to retrieve milestones for", required = true)
        Integer projectId,
        
        @ToolParam(description = "Filter by completion status: true for completed, false for open, null for all")
        Boolean isCompleted,
        
        @ToolParam(description = "Filter by started status: true for started, false for upcoming, null for all")
        Boolean isStarted,
        
        @ToolParam(description = "Maximum number of milestones to return (default 250)")
        Integer limit,
        
        @ToolParam(description = "Number of milestones to skip for pagination")
        Integer offset
    ) {
        return apiClient.getMilestones(projectId, isCompleted, isStarted, limit, offset);
    }
    
    @Tool(description = "Creates a new milestone in a project. " + "Requires a name and optionally accepts description, due date, start date, parent milestone, and references. " + "Example: Create milestone 'Sprint 5' with due date 1735689600 (Unix timestamp) for project 1.")
    public Milestone addMilestone(
        @ToolParam(description = "The ID of the project to add the milestone to", required = true)
        Integer projectId,
        
        @ToolParam(description = "The name of the milestone", required = true)
        String name,
        
        @ToolParam(description = "The description of the milestone (optional)")
        String description,
        
        @ToolParam(description = "The due date as Unix timestamp (optional)")
        Long dueOn,
        
        @ToolParam(description = "The start date as Unix timestamp (optional)")
        Long startOn,
        
        @ToolParam(description = "The ID of the parent milestone (optional)")
        Integer parentId,
        
        @ToolParam(description = "Comma-separated list of references/requirements (optional)")
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
    
    @Tool(description = "Updates an existing milestone. " + "Supports partial updates - only provided fields will be modified. " + "Can update name, description, dates, completion status, started status, and parent milestone. " + "Example: Mark milestone 5 as completed by setting isCompleted=true.")
    public Milestone updateMilestone(
        @ToolParam(description = "The ID of the milestone to update", required = true)
        Integer milestoneId,
        
        @ToolParam(description = "The new name of the milestone (optional)")
        String name,
        
        @ToolParam(description = "The new description (optional)")
        String description,
        
        @ToolParam(description = "The new due date as Unix timestamp (optional)")
        Long dueOn,
        
        @ToolParam(description = "The new start date as Unix timestamp (optional)")
        Long startOn,
        
        @ToolParam(description = "Mark as completed (true) or open (false)")
        Boolean isCompleted,
        
        @ToolParam(description = "Mark as started (true) or upcoming (false)")
        Boolean isStarted,
        
        @ToolParam(description = "The new parent milestone ID (optional)")
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
    
    @Tool(description = "Deletes a milestone permanently. " + "This action cannot be undone. " + "Example: Delete milestone 10 when it's no longer needed.")
    public OperationResult deleteMilestone(
        @ToolParam(description = "The ID of the milestone to delete", required = true)
        Integer milestoneId
    ) {
        apiClient.deleteMilestone(milestoneId);
        return OperationResult.success("Milestone deleted successfully");
    }
}
