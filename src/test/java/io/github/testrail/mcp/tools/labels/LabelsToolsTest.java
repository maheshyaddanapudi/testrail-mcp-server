package io.github.testrail.mcp.tools.labels;

import io.github.testrail.mcp.tools.labels.*;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Label;
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
class LabelsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private LabelsTools labelsTools;

    @BeforeEach
    void setUp() {
        labelsTools = new LabelsTools(apiClient);
    }

    @Test
    void testGetLabel() {
        Label label = new Label();
        label.setId(1);
        when(apiClient.getLabel(1)).thenReturn(label);

        Label result = labelsTools.getLabel(1);

        assertThat(result.getId()).isEqualTo(1);
        verify(apiClient).getLabel(1);
    }

    @Test
    void testGetLabels() {
        List<Label> labels = List.of(new Label(), new Label());
        when(apiClient.getLabels(1, 10, 0)).thenReturn(labels);

        List<Label> result = labelsTools.getLabels(1, 10, 0);

        assertThat(result).hasSize(2);
        verify(apiClient).getLabels(1, 10, 0);
    }

    @Test
    void testUpdateLabel() {
        Label label = new Label();
        label.setId(1);
        Map<String, Object> data = Map.of("title", "Updated");
        when(apiClient.updateLabel(1, data)).thenReturn(label);

        Label result = labelsTools.updateLabel(1, data);

        assertThat(result.getId()).isEqualTo(1);
        verify(apiClient).updateLabel(1, data);
    }
}
