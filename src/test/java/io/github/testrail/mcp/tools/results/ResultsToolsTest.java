package io.github.testrail.mcp.tools.results;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestResult;
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
 * Unit tests for ResultsTools.
 */
@ExtendWith(MockitoExtension.class)
class ResultsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private ResultsTools resultsTools;

    @BeforeEach
    void setUp() {
        resultsTools = new ResultsTools(apiClient);
    }

    @Test
    void getResults_shouldReturnResults() {
        TestResult result1 = new TestResult();
        result1.setId(1);
        result1.setStatusId(1);
        TestResult result2 = new TestResult();
        result2.setId(2);
        result2.setStatusId(5);

        when(apiClient.getResults(100, null, null, 50, 0)).thenReturn(List.of(result1, result2));

        List<TestResult> results = resultsTools.getResults(100, null, null, 50, 0);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStatusId()).isEqualTo(1);
        assertThat(results.get(1).getStatusId()).isEqualTo(5);
        verify(apiClient).getResults(100, null, null, 50, 0);
    }

    @Test
    void getResultsForRun_shouldReturnAllRunResults() {
        TestResult result1 = new TestResult();
        result1.setId(1);

        when(apiClient.getResultsForRun(100, null, null, null, null, null, null, null)).thenReturn(List.of(result1));

        List<TestResult> results = resultsTools.getResultsForRun(100, null, null, null, null, null, null, null);

        assertThat(results).hasSize(1);
        verify(apiClient).getResultsForRun(100, null, null, null, null, null, null, null);
    }

    @Test
    void addResult_shouldAddResultWithAllFields() {
        TestResult expected = new TestResult();
        expected.setId(500);
        expected.setStatusId(1);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addResult(eq(100), dataCaptor.capture())).thenReturn(expected);

        TestResult result = resultsTools.addResult(
                100,
                1,
                "Test passed successfully",
                "BUG-123",
                "30s",
                "1.0.0"
        );

        assertThat(result.getStatusId()).isEqualTo(1);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("status_id")).isEqualTo(1);
        assertThat(capturedData.get("comment")).isEqualTo("Test passed successfully");
        assertThat(capturedData.get("defects")).isEqualTo("BUG-123");
        assertThat(capturedData.get("elapsed")).isEqualTo("30s");
        assertThat(capturedData.get("version")).isEqualTo("1.0.0");
    }

    @Test
    void addResult_shouldAddResultWithMinimalFields() {
        TestResult expected = new TestResult();
        expected.setId(501);
        expected.setStatusId(5);

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(apiClient.addResult(eq(100), dataCaptor.capture())).thenReturn(expected);

        TestResult result = resultsTools.addResult(100, 5, null, null, null, null);

        assertThat(result.getStatusId()).isEqualTo(5);

        Map<String, Object> capturedData = dataCaptor.getValue();
        assertThat(capturedData.get("status_id")).isEqualTo(5);
        assertThat(capturedData).doesNotContainKey("comment");
        assertThat(capturedData).doesNotContainKey("defects");
    }

    @Test
    void addResults_shouldAddMultipleResults() {
        TestResult result1 = new TestResult();
        result1.setId(600);
        TestResult result2 = new TestResult();
        result2.setId(601);
        TestResult result3 = new TestResult();
        result3.setId(602);

        ArgumentCaptor<List<Map<String, Object>>> dataCaptor = ArgumentCaptor.forClass(List.class);
        when(apiClient.addResults(eq(100), dataCaptor.capture()))
                .thenReturn(List.of(result1, result2, result3));

        List<TestResult> results = resultsTools.addResults(100, "1, 2, 3", 1, "Bulk pass");

        assertThat(results).hasSize(3);

        List<Map<String, Object>> capturedData = dataCaptor.getValue();
        assertThat(capturedData).hasSize(3);
        assertThat(capturedData.get(0).get("test_id")).isEqualTo(1);
        assertThat(capturedData.get(1).get("test_id")).isEqualTo(2);
        assertThat(capturedData.get(2).get("test_id")).isEqualTo(3);
        assertThat(capturedData.get(0).get("status_id")).isEqualTo(1);
        assertThat(capturedData.get(0).get("comment")).isEqualTo("Bulk pass");
    }

    @Test
    void addResults_shouldAddResultsWithoutComment() {
        when(apiClient.addResults(eq(100), any())).thenReturn(List.of());

        List<TestResult> results = resultsTools.addResults(100, "1, 2", 1, null);

        assertThat(results).isEmpty();
        verify(apiClient).addResults(eq(100), argThat(list -> {
            Map<String, Object> first = list.get(0);
            return !first.containsKey("comment");
        }));
    }

    @Test
    void addResultsForCases_shouldAddResultsByCaseId() {
        TestResult result1 = new TestResult();
        result1.setId(700);

        ArgumentCaptor<List<Map<String, Object>>> dataCaptor = ArgumentCaptor.forClass(List.class);
        when(apiClient.addResultsForCases(eq(100), dataCaptor.capture()))
                .thenReturn(List.of(result1));

        List<TestResult> results = resultsTools.addResultsForCases(100, "123, 456", 1, "Passed");

        assertThat(results).hasSize(1);

        List<Map<String, Object>> capturedData = dataCaptor.getValue();
        assertThat(capturedData).hasSize(2);
        assertThat(capturedData.get(0).get("case_id")).isEqualTo(123);
        assertThat(capturedData.get(1).get("case_id")).isEqualTo(456);
        assertThat(capturedData.get(0).get("status_id")).isEqualTo(1);
    }
}
