package io.github.testrail.mcp.tools.groups;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail user groups.
 * Groups organize users for easier access control and project assignment.
 * Example: "QA Team", "Developers", "External Testers" groups for different team structures.
 * Requires TestRail 7.5 or later.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupsTools {

    private final TestrailApiClient apiClient;

    @InternalTool(
            name = "get_group",
            description = """
                    Retrieves a single user group by ID including all member user IDs.
                    Groups organize users for easier access control and project assignment.
                    Returns group name and array of user IDs belonging to the group.
                    Requires TestRail 7.5 or later.
                    
                    **When to use:** Use this tool when you need to view a specific group's members,
                    verify group composition before updates, audit group membership,
                    or check which users belong to a team/department group.
                    
                    **Might lead to:** update_group (to modify membership), get_users (to see user details),
                    get_projects (to see project access for group members).
                    
                    **Example prompts:**
                    - "Show me group 1"
                    - "Get members of the 'QA Team' group"
                    - "Who belongs to group 5?"
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_group', {groupId: 1})",
                    "execute_tool('get_group', {groupId: 5})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "group", "members", "team", "users"}
    )
    public Group getGroup(
            @InternalToolParam(description = "The ID of the group to retrieve")
            int groupId
    ) {
        log.info("Getting group {}", groupId);
        return apiClient.getGroup(groupId);
    }

    @InternalTool(
            name = "get_groups",
            description = """
                    Retrieves all user groups including their member user IDs.
                    Groups organize users for team structures, access control, and project assignments.
                    Returns array of groups with names and user ID arrays.
                    Requires TestRail 7.5 or later.
                    
                    **When to use:** Use this tool when you need to browse all user groups,
                    discover team structures and organizational units, audit group configurations,
                    or prepare for user management workflows.
                    
                    **Might lead to:** get_group (to view specific group details), add_group (to create new groups),
                    get_users (to see user details for group members).
                    
                    **Example prompts:**
                    - "List all user groups"
                    - "Show me all teams in TestRail"
                    - "What groups exist for organizing users?"
                    """,
            category = "users",
            examples = {
                    "execute_tool('get_groups', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "groups", "teams", "all", "users"}
    )
    public List<Group> getGroups() {
        log.info("Getting all groups");
        return apiClient.getGroups();
    }

    @InternalTool(
            name = "add_group",
            description = """
                    Creates a new user group with specified members.
                    Groups simplify user management by organizing users into teams, departments, or functional units.
                    Requires TestRail 7.5 or later.
                    
                    Required data fields:
                    - **name** (string): Group name (e.g., "QA Team", "Developers", "External Testers")
                    - **user_ids** (array): Array of user IDs to include in the group
                    
                    **When to use:** Use this tool when you need to create new team structures,
                    organize users by department or function, establish access control groups,
                    or simplify project-level user assignments.
                    
                    **Might lead to:** get_group (to verify creation), update_group (to modify membership),
                    add_project_user (to assign group to projects).
                    
                    **Example prompts:**
                    - "Create a group called 'QA Team' with users 1, 2, 3"
                    - "Add a new group for external testers"
                    - "Define a 'Developers' group with specific users"
                    """,
            category = "users",
            examples = {
                    "execute_tool('add_group', {group: {name: 'QA Team', user_ids: [1, 2, 3]}})",
                    "execute_tool('add_group', {group: {name: 'Developers', user_ids: [4, 5, 6]}})"
            },
            keywords = {"add", "create", "new", "group", "team", "organize", "users"}
    )
    public Group addGroup(
            @InternalToolParam(description = "Group map with keys: name (required), user_ids (required array of integers)")
            Map<String, Object> group
    ) {
        log.info("Adding new group");
        return apiClient.addGroup(group);
    }

    @InternalTool(
            name = "update_group",
            description = """
                    Updates an existing user group's name or membership.
                    **IMPORTANT: The user_ids array must contain the FULL list of users.**
                    This is not additive - it replaces the entire membership list.
                    To add a user, include all existing users plus the new user.
                    To remove a user, include all users except the one to remove.
                    Requires TestRail 7.5 or later.
                    
                    Updatable fields:
                    - **name** (string): Change group name
                    - **user_ids** (array): Replace entire user membership list
                    
                    **When to use:** Use this tool when you need to add/remove users from groups,
                    rename groups to reflect organizational changes, adjust team compositions,
                    or maintain group accuracy. Always get current membership first before updating.
                    
                    **Might lead to:** get_group (to verify update or get current membership before updating).
                    
                    **Example prompts:**
                    - "Add user 6 to group 1"
                    - "Remove user 3 from the 'QA Team' group"
                    - "Rename group 5 to 'Senior QA Team'"
                    """,
            category = "users",
            examples = {
                    "execute_tool('update_group', {groupId: 1, group: {user_ids: [1, 2, 3, 6]}})",
                    "execute_tool('update_group', {groupId: 5, group: {name: 'Senior QA Team'}})"
            },
            keywords = {"update", "modify", "change", "edit", "group", "team", "members", "rename"}
    )
    public Group updateGroup(
            @InternalToolParam(description = "The ID of the group to update")
            int groupId,
            @InternalToolParam(description = "Group map with keys: name, user_ids (full replacement array)")
            Map<String, Object> group
    ) {
        log.info("Updating group {}", groupId);
        return apiClient.updateGroup(groupId, group);
    }

    @InternalTool(
            name = "delete_group",
            description = """
                    Permanently deletes a user group.
                    Users in the group are NOT deleted - only the group itself is removed.
                    Project access granted via this group may be affected.
                    This action cannot be undone.
                    Requires TestRail 7.5 or later.
                    
                    **When to use:** Use this tool ONLY when a group is obsolete and no longer needed,
                    after verifying no critical project access depends on it, or when consolidating groups.
                    Check group usage and project assignments before deletion.
                    
                    **Might lead to:** get_groups (to verify deletion), get_projects (to check project access impact).
                    
                    **Example prompts:**
                    - "Delete group 10"
                    - "Remove the obsolete 'Old Team' group"
                    - "Permanently delete group 'External Contractors'"
                    """,
            category = "users",
            examples = {
                    "execute_tool('delete_group', {groupId: 10})",
                    "execute_tool('delete_group', {groupId: 15})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "group", "team", "cleanup"}
    )
    public void deleteGroup(
            @InternalToolParam(description = "The ID of the group to delete")
            int groupId
    ) {
        log.info("Deleting group {}", groupId);
        apiClient.deleteGroup(groupId);
    }
}
