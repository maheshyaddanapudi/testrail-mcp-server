package io.github.testrail.mcp.tools;

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

    @BeforeEach
    void setUp() {
        sharedStepsTools = new SharedStepsTools(apiClient);
    }

    @Test
    void getSharedStep_shouldReturnSharedStep() {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(1);
        sharedStep.setTitle("Login Steps");

        when(apiClient.getSharedStep(1)).thenReturn(sharedStep);

        SharedStep result = sharedStepsTools.getSharedStep(1);

        assertThat(result.getTitle()).isEqualTo("Login Steps");
        verify(apiClient).getSharedStep(1);
    }

    @Test
    void getSharedStepHistory_shouldReturnHistory() {
        SharedStepHistory history = new SharedStepHistory();
        history.setSharedStepId(1);
        history.setVersionId(1);

        when(apiClient.getSharedStepHistory(1)).thenReturn(List.of(history));

        List<SharedStepHistory> result = sharedStepsTools.getSharedStepHistory(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSharedStepId()).isEqualTo(1);
        verify(apiClient).getSharedStepHistory(1);
    }

    @Test
    void getSharedSteps_shouldReturnAllSharedSteps() {
        SharedStep step1 = new SharedStep();
        step1.setId(1);
        SharedStep step2 = new SharedStep();
        step2.setId(2);

        when(apiClient.getSharedSteps(10)).thenReturn(List.of(step1, step2));

        List<SharedStep> result = sharedStepsTools.getSharedSteps(10);

        assertThat(result).hasSize(2);
        verify(apiClient).getSharedSteps(10);
    }

    @Test
    void addSharedStep_shouldCreateSharedStep() {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(3);
        sharedStep.setTitle("New Shared Step");

        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Shared Step");

        when(apiClient.addSharedStep(eq(10), any(Map.class))).thenReturn(sharedStep);

        SharedStep result = sharedStepsTools.addSharedStep(10, data);

        assertThat(result.getTitle()).isEqualTo("New Shared Step");
        verify(apiClient).addSharedStep(eq(10), any(Map.class));
    }

    @Test
    void updateSharedStep_shouldUpdateSharedStep() {
        SharedStep sharedStep = new SharedStep();
        sharedStep.setId(1);
        sharedStep.setTitle("Updated Steps");

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Steps");

        when(apiClient.updateSharedStep(eq(1), any(Map.class))).thenReturn(sharedStep);

        SharedStep result = sharedStepsTools.updateSharedStep(1, data);

        assertThat(result.getTitle()).isEqualTo("Updated Steps");
        verify(apiClient).updateSharedStep(eq(1), any(Map.class));
    }

    @Test
    void deleteSharedStep_shouldDeleteSuccessfully() {
        doNothing().when(apiClient).deleteSharedStep(1);

        sharedStepsTools.deleteSharedStep(1);

        verify(apiClient).deleteSharedStep(1);
    }
}
