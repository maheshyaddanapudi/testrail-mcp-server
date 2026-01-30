package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private GroupsTools groupsTools;

    @BeforeEach
    void setUp() {
        groupsTools = new GroupsTools(apiClient);
    }

    @Test
    void testGetGroup() {
        Group group = new Group();
        group.setId(1);
        when(apiClient.getGroup(1)).thenReturn(group);

        Group result = groupsTools.getGroup(1);

        assertThat(result.getId()).isEqualTo(1);
        verify(apiClient).getGroup(1);
    }

    @Test
    void testGetGroups() {
        List<Group> groups = List.of(new Group(), new Group());
        when(apiClient.getGroups()).thenReturn(groups);

        List<Group> result = groupsTools.getGroups();

        assertThat(result).hasSize(2);
        verify(apiClient).getGroups();
    }

    @Test
    void testAddGroup() {
        Group group = new Group();
        group.setId(2);
        Map<String, Object> data = Map.of("name", "Test Group");
        when(apiClient.addGroup(data)).thenReturn(group);

        Group result = groupsTools.addGroup(data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).addGroup(data);
    }

    @Test
    void testUpdateGroup() {
        Group group = new Group();
        group.setId(2);
        Map<String, Object> data = Map.of("name", "Updated Group");
        when(apiClient.updateGroup(2, data)).thenReturn(group);

        Group result = groupsTools.updateGroup(2, data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).updateGroup(2, data);
    }

    @Test
    void testDeleteGroup() {
        doNothing().when(apiClient).deleteGroup(2);

        groupsTools.deleteGroup(2);

        verify(apiClient).deleteGroup(2);
    }
}
