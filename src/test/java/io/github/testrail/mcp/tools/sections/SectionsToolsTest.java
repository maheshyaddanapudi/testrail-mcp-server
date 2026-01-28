package io.github.testrail.mcp.tools.sections;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.Section;
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
 * Unit tests for SectionsTools.
 */
@ExtendWith(MockitoExtension.class)
class SectionsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private SectionsTools sectionsTools;

    @BeforeEach
    void setUp() {
        sectionsTools = new SectionsTools(apiClient);
    }

    @Test
    void getSection_shouldReturnSection() {
        Section expected = new Section();
        expected.setId(10);
        expected.setName("Login Tests");
        expected.setDepth(0);

        when(apiClient.getSection(10)).thenReturn(expected);

        Section result = sectionsTools.getSection(10);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("Login Tests");
        verify(apiClient).getSection(10);
    }

    @Test
    void getSections_shouldReturnSectionsWithFilters() {
        Section section1 = new Section();
        section1.setId(1);
        section1.setName("Section 1");
        Section section2 = new Section();
        section2.setId(2);
        section2.setName("Section 2");

        when(apiClient.getSections(1, 5, 50, 0)).thenReturn(List.of(section1, section2));

        List<Section> result = sectionsTools.getSections(1, 5, 50, 0);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Section 1");
        verify(apiClient).getSections(1, 5, 50, 0);
    }

    @Test
    void getSections_shouldHandleNullFilters() {
        when(apiClient.getSections(1, null, null, null)).thenReturn(List.of());

        List<Section> result = sectionsTools.getSections(1, null, null, null);

        assertThat(result).isEmpty();
        verify(apiClient).getSections(1, null, null, null);
    }

    @Test
    void addSection_shouldCreateSectionWithAllFields() {
        Section expected = new Section();
        expected.setId(20);
        expected.setName("New Section");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addSection(eq(1), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.addSection(
                1,
                "New Section",
                "Description of section",
                5,
                10
        );

        assertThat(result.getId()).isEqualTo(20);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("New Section");
        assertThat(capturedData.get("description")).isEqualTo("Description of section");
        assertThat(capturedData.get("parent_id")).isEqualTo(5);
        assertThat(capturedData.get("suite_id")).isEqualTo(10);
    }

    @Test
    void addSection_shouldCreateRootSection() {
        Section expected = new Section();
        expected.setId(21);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addSection(eq(1), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.addSection(1, "Root Section", null, null, null);

        assertThat(result.getId()).isEqualTo(21);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Root Section");
        assertThat(capturedData).doesNotContainKey("description");
        assertThat(capturedData).doesNotContainKey("parent_id");
    }

    @Test
    void updateSection_shouldUpdateSpecifiedFields() {
        Section expected = new Section();
        expected.setId(10);
        expected.setName("Updated Section");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.updateSection(eq(10), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.updateSection(10, "Updated Section", "New description");

        assertThat(result.getName()).isEqualTo("Updated Section");

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Updated Section");
        assertThat(capturedData.get("description")).isEqualTo("New description");
    }

    @Test
    void updateSection_shouldUpdateOnlyName() {
        Section expected = new Section();
        expected.setId(10);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.updateSection(eq(10), dataCaptor.capture())).thenReturn(expected);

        sectionsTools.updateSection(10, "New Name", null);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("New Name");
        assertThat(capturedData).doesNotContainKey("description");
    }

    @Test
    void deleteSection_shouldSoftDeleteByDefault() {
        doNothing().when(apiClient).deleteSection(10, true);

        OperationResult result = sectionsTools.deleteSection(10, null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("trash");
        assertThat(result.getMessage()).contains("soft delete");
        verify(apiClient).deleteSection(10, true);
    }

    @Test
    void deleteSection_shouldSoftDeleteWhenTrue() {
        doNothing().when(apiClient).deleteSection(10, true);

        OperationResult result = sectionsTools.deleteSection(10, true);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("soft delete");
        verify(apiClient).deleteSection(10, true);
    }

    @Test
    void deleteSection_shouldPermanentlyDeleteWhenFalse() {
        doNothing().when(apiClient).deleteSection(10, false);

        OperationResult result = sectionsTools.deleteSection(10, false);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("permanently deleted");
        verify(apiClient).deleteSection(10, false);
    }

    @Test
    void moveSection_shouldMoveWithParentId() {
        Section expected = new Section();
        expected.setId(10);
        expected.setParentId(5);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.moveSection(eq(10), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.moveSection(10, 5, null);

        assertThat(result.getParentId()).isEqualTo(5);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("parent_id")).isEqualTo(5);
        assertThat(capturedData).doesNotContainKey("after_id");
    }

    @Test
    void moveSection_shouldMoveWithAfterId() {
        Section expected = new Section();
        expected.setId(10);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.moveSection(eq(10), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.moveSection(10, null, 15);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData).doesNotContainKey("parent_id");
        assertThat(capturedData.get("after_id")).isEqualTo(15);
    }

    @Test
    void moveSection_shouldMoveWithBothIds() {
        Section expected = new Section();
        expected.setId(10);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.moveSection(eq(10), dataCaptor.capture())).thenReturn(expected);

        Section result = sectionsTools.moveSection(10, 5, 15);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("parent_id")).isEqualTo(5);
        assertThat(capturedData.get("after_id")).isEqualTo(15);
    }
}
