package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.SharedStep;
import io.github.testrail.mcp.model.SharedStepHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharedStepsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private SharedStepsTools sharedStepsTools;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        sharedStepsTools = new SharedStepsTools(apiClient, objectMapper);
    }

    @Test
    void getSharedStep_shouldReturnSharedStep() throws Exception {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(1);
        sharedStep.setTitle("Login Steps");

        when(apiClient.getSharedStep(1)).thenReturn(sharedStep);

        String result = sharedStepsTools.getSharedStep(1);

        assertThat(result).contains("Login Steps");
        verify(apiClient).getSharedStep(1);
    }

    @Test
    void getSharedStepHistory_shouldReturnHistory() throws Exception {
        SharedStepHistory history = new SharedStepHistory();
        history.setSharedStepId(1);
        history.setVersionId(1);

        when(apiClient.getSharedStepHistory(1)).thenReturn(List.of(history));

        String result = sharedStepsTools.getSharedStepHistory(1);

        assertThat(result).isNotEmpty();
        verify(apiClient).getSharedStepHistory(1);
    }

    @Test
    void getSharedSteps_shouldReturnAllSharedSteps() throws Exception {
        SharedStep step1 = new SharedStep();
        step1.setId(1);
        SharedStep step2 = new SharedStep();
        step2.setId(2);

        when(apiClient.getSharedSteps(10)).thenReturn(List.of(step1, step2));

        String result = sharedStepsTools.getSharedSteps(10);

        assertThat(result).isNotEmpty();
        verify(apiClient).getSharedSteps(10);
    }

    @Test
    void addSharedStep_shouldCreateSharedStep() throws Exception {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(3);
        sharedStep.setTitle("New Shared Step");

        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Shared Step");

        when(apiClient.addSharedStep(eq(10), any(Map.class))).thenReturn(sharedStep);

        String dataJson = objectMapper.writeValueAsString(data);
        String result = sharedStepsTools.addSharedStep(10, dataJson);

        assertThat(result).contains("New Shared Step");
        verify(apiClient).addSharedStep(eq(10), any(Map.class));
    }

    @Test
    void updateSharedStep_shouldUpdateSharedStep() throws Exception {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(1);
        sharedStep.setTitle("Updated Steps");

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Steps");

        when(apiClient.updateSharedStep(eq(1), any(Map.class))).thenReturn(sharedStep);

        String dataJson = objectMapper.writeValueAsString(data);
        String result = sharedStepsTools.updateSharedStep(1, dataJson);

        assertThat(result).contains("Updated Steps");
        verify(apiClient).updateSharedStep(eq(1), any(Map.class));
    }

    @Test
    void deleteSharedStep_shouldDeleteSuccessfully() {
        doNothing().when(apiClient).deleteSharedStep(1);

        String result = sharedStepsTools.deleteSharedStep(1);

        assertThat(result).contains("success");
        verify(apiClient).deleteSharedStep(1);
    }
}
