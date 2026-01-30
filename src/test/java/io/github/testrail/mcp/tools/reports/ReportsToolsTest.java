package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private ReportsTools reportsTools;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        reportsTools = new ReportsTools(apiClient);
    }

    @Test
    void getReports_shouldReturnReports() {
        Report report = new Report();
        report.setId(1);
        report.setName("Test Summary");

        when(apiClient.getReports(10)).thenReturn(List.of(report));

        List<Report> result = reportsTools.getReports(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Summary");
        verify(apiClient).getReports(10);
    }

    @Test
    void runReport_shouldReturnReportData() throws Exception {
        JsonNode reportData = objectMapper.readTree("{\"total_tests\":100}");

        when(apiClient.runReport(100)).thenReturn(reportData);

        JsonNode result = reportsTools.runReport(100);

        assertThat(result.has("total_tests")).isTrue();
        assertThat(result.get("total_tests").asInt()).isEqualTo(100);
        verify(apiClient).runReport(100);
    }

    @Test
    void getCrossProjectReports_shouldReturnReports() {
        Report report = new Report();
        report.setId(2);
        report.setIsCrossProject(true);

        when(apiClient.getCrossProjectReports()).thenReturn(List.of(report));

        List<Report> result = reportsTools.getCrossProjectReports();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsCrossProject()).isTrue();
        verify(apiClient).getCrossProjectReports();
    }

    @Test
    void runCrossProjectReport_shouldReturnReportData() throws Exception {
        JsonNode reportData = objectMapper.readTree("{\"total_projects\":5}");

        when(apiClient.runCrossProjectReport(200)).thenReturn(reportData);

        JsonNode result = reportsTools.runCrossProjectReport(200);

        assertThat(result.has("total_projects")).isTrue();
        assertThat(result.get("total_projects").asInt()).isEqualTo(5);
        verify(apiClient).runCrossProjectReport(200);
    }
}
