package io.github.testrail.mcp.tools.runs;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestRun;
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
 * Unit tests for RunsTools.
 */
@ExtendWith(MockitoExtension.class)
class RunsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private RunsTools runsTools;

    @BeforeEach
    void setUp() {
        runsTools = new RunsTools(apiClient);
    }

    @Test
    void getRun_shouldReturnRun() {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setName("Sprint 10 Regression");
        expected.setPassedCount(50);
        expected.setFailedCount(5);

        when(apiClient.getRun(100)).thenReturn(expected);

        TestRun result = runsTools.getRun(100);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getName()).isEqualTo("Sprint 10 Regression");
        assertThat(result.getPassedCount()).isEqualTo(50);
        verify(apiClient).getRun(100);
    }

    @Test
    void getRuns_shouldReturnRunsWithPagination() {
        TestRun run1 = new TestRun();
        run1.setId(1);
        TestRun run2 = new TestRun();
        run2.setId(2);

        when(apiClient.getRuns(1, 50, 0)).thenReturn(List.of(run1, run2));

        List<TestRun> result = runsTools.getRuns(1, 50, 0);

        assertThat(result).hasSize(2);
        verify(apiClient).getRuns(1, 50, 0);
    }

    @Test
    void addRun_shouldCreateRunWithAllFields() {
        TestRun expected = new TestRun();
        expected.setId(200);
        expected.setName("New Run");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addRun(eq(1), dataCaptor.capture())).thenReturn(expected);

        TestRun result = runsTools.addRun(
                1,
                "New Run",
                "Description",
                5,
                10,
                20,
                true,
                null
        );

        assertThat(result.getId()).isEqualTo(200);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("New Run");
        assertThat(capturedData.get("description")).isEqualTo("Description");
        assertThat(capturedData.get("suite_id")).isEqualTo(5);
        assertThat(capturedData.get("milestone_id")).isEqualTo(10);
        assertThat(capturedData.get("assignedto_id")).isEqualTo(20);
        assertThat(capturedData.get("include_all")).isEqualTo(true);
    }

    @Test
    void addRun_shouldCreateRunWithCaseIds() {
        TestRun expected = new TestRun();
        expected.setId(201);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addRun(eq(1), dataCaptor.capture())).thenReturn(expected);

        TestRun result = runsTools.addRun(
                1,
                "Specific Cases Run",
                null,
                null,
                null,
                null,
                null,
                "1, 2, 3"
        );

        assertThat(result.getId()).isEqualTo(201);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("include_all")).isEqualTo(false);
        assertThat((List<Integer>) capturedData.get("case_ids")).containsExactly(1, 2, 3);
    }

    @Test
    void addRun_shouldCreateRunWithMinimalFields() {
        TestRun expected = new TestRun();
        expected.setId(202);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addRun(eq(1), dataCaptor.capture())).thenReturn(expected);

        TestRun result = runsTools.addRun(1, "Minimal Run", null, null, null, null, null, null);

        assertThat(result.getId()).isEqualTo(202);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Minimal Run");
        assertThat(capturedData).doesNotContainKey("description");
    }

    @Test
    void updateRun_shouldUpdateSpecifiedFields() {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setName("Updated Run");

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.updateRun(eq(100), dataCaptor.capture())).thenReturn(expected);

        TestRun result = runsTools.updateRun(100, "Updated Run", null, 15, null);

        assertThat(result.getName()).isEqualTo("Updated Run");

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("name")).isEqualTo("Updated Run");
        assertThat(capturedData.get("milestone_id")).isEqualTo(15);
        assertThat(capturedData).doesNotContainKey("description");
    }

    @Test
    void closeRun_shouldCloseRun() {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setIsCompleted(true);

        when(apiClient.closeRun(100)).thenReturn(expected);

        TestRun result = runsTools.closeRun(100);

        assertThat(result.getIsCompleted()).isTrue();
        verify(apiClient).closeRun(100);
    }

    @Test
    void deleteRun_shouldDeleteAndReturnSuccess() {
        doNothing().when(apiClient).deleteRun(100);

        OperationResult result = runsTools.deleteRun(100);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("R100");
        assertThat(result.getMessage()).contains("deleted");
        verify(apiClient).deleteRun(100);
    }
}
