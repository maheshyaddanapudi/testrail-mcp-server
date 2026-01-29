package io.github.testrail.mcp.tools.projects;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectsTools.
 */
@ExtendWith(MockitoExtension.class)
class ProjectsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private ProjectsTools projectsTools;

    @BeforeEach
    void setUp() {
        projectsTools = new ProjectsTools(apiClient);
    }

    @Test
    void getProject_shouldReturnProject() {
        Project expected = new Project();
        expected.setId(1);
        expected.setName("Mobile App");
        expected.setSuiteMode(1);

        when(apiClient.getProject(1)).thenReturn(expected);

        Project result = projectsTools.getProject(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Mobile App");
        verify(apiClient).getProject(1);
    }

    @Test
    void getProjects_shouldReturnAllProjects() {
        Project project1 = new Project();
        project1.setId(1);
        project1.setName("Project 1");
        Project project2 = new Project();
        project2.setId(2);
        project2.setName("Project 2");

        when(apiClient.getProjects()).thenReturn(List.of(project1, project2));

        List<Project> result = projectsTools.getProjects();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Project 1");
        assertThat(result.get(1).getName()).isEqualTo("Project 2");
        verify(apiClient).getProjects();
    }

    @Test
    void addProject_shouldCreateProjectWithAllFields() {
        Project expected = new Project();
        expected.setId(5);
        expected.setName("New Project");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addProject(dataCaptor.capture())).thenReturn(expected);

        Project result = projectsTools.addProject(
                "New Project",
                "Welcome to the new project!",
                true,
                3
        );

        assertThat(result.getId()).isEqualTo(5);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("New Project");
        assertThat(capturedData.get("announcement")).isEqualTo("Welcome to the new project!");
        assertThat(capturedData.get("show_announcement")).isEqualTo(true);
        assertThat(capturedData.get("suite_mode")).isEqualTo(3);
    }

    @Test
    void addProject_shouldCreateProjectWithMinimalFields() {
        Project expected = new Project();
        expected.setId(6);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addProject(dataCaptor.capture())).thenReturn(expected);

        Project result = projectsTools.addProject("Minimal Project", null, null, null);

        assertThat(result.getId()).isEqualTo(6);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Minimal Project");
        assertThat(capturedData).doesNotContainKey("announcement");
        assertThat(capturedData).doesNotContainKey("show_announcement");
        assertThat(capturedData).doesNotContainKey("suite_mode");
    }

    @Test
    void updateProject_shouldUpdateSpecifiedFields() {
        Project expected = new Project();
        expected.setId(1);
        expected.setName("Updated Name");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.updateProject(eq(1), dataCaptor.capture())).thenReturn(expected);

        Project result = projectsTools.updateProject(1, "Updated Name", null, null, true);

        assertThat(result.getName()).isEqualTo("Updated Name");

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Updated Name");
        assertThat(capturedData.get("is_completed")).isEqualTo(true);
        assertThat(capturedData).doesNotContainKey("announcement");
    }

    @Test
    void deleteProject_shouldDeleteAndReturnSuccess() {
        doNothing().when(apiClient).deleteProject(1);

        OperationResult result = projectsTools.deleteProject(1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("1");
        assertThat(result.getMessage()).contains("deleted");
        verify(apiClient).deleteProject(1);
    }
}
