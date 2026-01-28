package io.github.testrail.mcp.tools.cases;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestCase;
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
 * Unit tests for CasesTools.
 */
@ExtendWith(MockitoExtension.class)
class CasesToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private CasesTools casesTools;

    @BeforeEach
    void setUp() {
        casesTools = new CasesTools(apiClient);
    }

    @Test
    void getTestCase_shouldReturnCase() {
        TestCase expected = new TestCase();
        expected.setId(123);
        expected.setTitle("Login Test");

        when(apiClient.getCase(123)).thenReturn(expected);

        TestCase result = casesTools.getTestCase(123);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getTitle()).isEqualTo("Login Test");
        verify(apiClient).getCase(123);
    }

    @Test
    void getTestCases_shouldReturnCasesWithFilters() {
        TestCase case1 = new TestCase();
        case1.setId(1);
        TestCase case2 = new TestCase();
        case2.setId(2);

        when(apiClient.getCases(1, 5, 10, 50, 0)).thenReturn(List.of(case1, case2));

        List<TestCase> result = casesTools.getTestCases(1, 5, 10, 50, 0);

        assertThat(result).hasSize(2);
        verify(apiClient).getCases(1, 5, 10, 50, 0);
    }

    @Test
    void getTestCases_shouldHandleNullFilters() {
        when(apiClient.getCases(1, null, null, null, null)).thenReturn(List.of());

        List<TestCase> result = casesTools.getTestCases(1, null, null, null, null);

        assertThat(result).isEmpty();
        verify(apiClient).getCases(1, null, null, null, null);
    }

    @Test
    void addTestCase_shouldCreateCaseWithAllFields() {
        TestCase expected = new TestCase();
        expected.setId(456);
        expected.setTitle("New Test");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addCase(eq(1), dataCaptor.capture())).thenReturn(expected);

        TestCase result = casesTools.addTestCase(
                1,
                "New Test",
                "Step 1\nStep 2",
                "Should pass",
                "User logged in",
                3,
                1,
                "JIRA-123"
        );

        assertThat(result.getId()).isEqualTo(456);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("title")).isEqualTo("New Test");
        assertThat(capturedData.get("custom_steps")).isEqualTo("Step 1\nStep 2");
        assertThat(capturedData.get("custom_expected")).isEqualTo("Should pass");
        assertThat(capturedData.get("custom_preconds")).isEqualTo("User logged in");
        assertThat(capturedData.get("priority_id")).isEqualTo(3);
        assertThat(capturedData.get("type_id")).isEqualTo(1);
        assertThat(capturedData.get("refs")).isEqualTo("JIRA-123");
    }

    @Test
    void addTestCase_shouldCreateCaseWithMinimalFields() {
        TestCase expected = new TestCase();
        expected.setId(789);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addCase(eq(1), dataCaptor.capture())).thenReturn(expected);

        TestCase result = casesTools.addTestCase(1, "Minimal Test", null, null, null, null, null, null);

        assertThat(result.getId()).isEqualTo(789);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("title")).isEqualTo("Minimal Test");
        assertThat(capturedData).doesNotContainKey("custom_steps");
        assertThat(capturedData).doesNotContainKey("custom_expected");
    }

    @Test
    void updateTestCase_shouldUpdateSpecifiedFields() {
        TestCase expected = new TestCase();
        expected.setId(123);
        expected.setTitle("Updated Title");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.updateCase(eq(123), dataCaptor.capture())).thenReturn(expected);

        TestCase result = casesTools.updateTestCase(
                123,
                "Updated Title",
                null,
                "New expected result",
                null,
                4,
                null,
                null
        );

        assertThat(result.getTitle()).isEqualTo("Updated Title");

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("title")).isEqualTo("Updated Title");
        assertThat(capturedData.get("custom_expected")).isEqualTo("New expected result");
        assertThat(capturedData.get("priority_id")).isEqualTo(4);
        assertThat(capturedData).doesNotContainKey("custom_steps");
        assertThat(capturedData).doesNotContainKey("custom_preconds");
    }

    @Test
    void deleteTestCase_shouldDeleteAndReturnSuccess() {
        doNothing().when(apiClient).deleteCase(123);

        OperationResult result = casesTools.deleteTestCase(123);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("C123");
        assertThat(result.getMessage()).contains("deleted");
        verify(apiClient).deleteCase(123);
    }

    @Test
    void cloneTestCase_shouldCloneToSameSection() {
        TestCase original = new TestCase();
        original.setId(100);
        original.setTitle("Original Test");
        original.setSectionId(5);
        original.setSteps("Original steps");
        original.setExpectedResult("Original expected");
        original.setPriorityId(2);
        original.setTypeId(1);

        TestCase cloned = new TestCase();
        cloned.setId(101);
        cloned.setTitle("Copy of Original Test");

        when(apiClient.getCase(100)).thenReturn(original);
        when(apiClient.addCase(eq(5), any())).thenReturn(cloned);

        TestCase result = casesTools.cloneTestCase(100, null, null, null, null);

        assertThat(result.getId()).isEqualTo(101);
        verify(apiClient).getCase(100);
        verify(apiClient).addCase(eq(5), any());
    }

    @Test
    void cloneTestCase_shouldCloneToDifferentSection() {
        TestCase original = new TestCase();
        original.setId(100);
        original.setTitle("Original Test");
        original.setSectionId(5);

        TestCase cloned = new TestCase();
        cloned.setId(102);

        when(apiClient.getCase(100)).thenReturn(original);
        when(apiClient.addCase(eq(10), any())).thenReturn(cloned);

        TestCase result = casesTools.cloneTestCase(100, 10, null, null, null);

        assertThat(result.getId()).isEqualTo(102);
        verify(apiClient).addCase(eq(10), any());
    }

    @Test
    void cloneTestCase_shouldApplyModifications() {
        TestCase original = new TestCase();
        original.setId(100);
        original.setTitle("Original Test");
        original.setSectionId(5);
        original.setSteps("Original steps");
        original.setExpectedResult("Original expected");
        original.setPriorityId(2);

        TestCase cloned = new TestCase();
        cloned.setId(103);
        cloned.setTitle("New Title");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.getCase(100)).thenReturn(original);
        when(apiClient.addCase(eq(5), dataCaptor.capture())).thenReturn(cloned);

        TestCase result = casesTools.cloneTestCase(100, null, "New Title", "New steps", "New expected");

        assertThat(result.getId()).isEqualTo(103);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("title")).isEqualTo("New Title");
        assertThat(capturedData.get("custom_steps")).isEqualTo("New steps");
        assertThat(capturedData.get("custom_expected")).isEqualTo("New expected");
        assertThat(capturedData.get("priority_id")).isEqualTo(2); // Preserved from original
    }

    @Test
    void cloneTestCase_shouldGenerateDefaultTitle() {
        TestCase original = new TestCase();
        original.setId(100);
        original.setTitle("My Test");
        original.setSectionId(5);

        TestCase cloned = new TestCase();
        cloned.setId(104);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.getCase(100)).thenReturn(original);
        when(apiClient.addCase(eq(5), dataCaptor.capture())).thenReturn(cloned);

        casesTools.cloneTestCase(100, null, null, null, null);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("title")).isEqualTo("Copy of My Test");
    }
}
