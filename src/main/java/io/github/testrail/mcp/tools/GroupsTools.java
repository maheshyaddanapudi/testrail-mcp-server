package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = "Get a single user group by ID. Requires TestRail 7.5+")
    public Group getGroup(int groupId) {
        log.info("Getting group {}", groupId);
        return apiClient.getGroup(groupId);
    }

    @Tool(description = "Get all user groups. Requires TestRail 7.5+")
    public List<Group> getGroups() {
        log.info("Getting all groups");
        return apiClient.getGroups();
    }

    @Tool(description = "Create a new user group. Requires TestRail 7.5+")
    public Group addGroup(Map<String, Object> group) {
        log.info("Adding new group");
        return apiClient.addGroup(group);
    }

    @Tool(description = "Update an existing user group. The user_ids array must contain the full list of users. Requires TestRail 7.5+")
    public Group updateGroup(int groupId, Map<String, Object> group) {
        log.info("Updating group {}", groupId);
        return apiClient.updateGroup(groupId, group);
    }

    @Tool(description = "Delete a user group. Requires TestRail 7.5+")
    public void deleteGroup(int groupId) {
        log.info("Deleting group {}", groupId);
        apiClient.deleteGroup(groupId);
    }
}
