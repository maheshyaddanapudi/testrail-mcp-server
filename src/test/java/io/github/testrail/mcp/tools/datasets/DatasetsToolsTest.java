package io.github.testrail.mcp.tools.datasets;

import io.github.testrail.mcp.tools.datasets.*;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Dataset;
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
class DatasetsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private DatasetsTools datasetsTools;

    @BeforeEach
    void setUp() {
        datasetsTools = new DatasetsTools(apiClient);
    }

    @Test
    void testGetDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(1);
        when(apiClient.getDataset(1)).thenReturn(dataset);

        Dataset result = datasetsTools.getDataset(1);

        assertThat(result.getId()).isEqualTo(1);
        verify(apiClient).getDataset(1);
    }

    @Test
    void testGetDatasets() {
        List<Dataset> datasets = List.of(new Dataset(), new Dataset());
        when(apiClient.getDatasets(1)).thenReturn(datasets);

        List<Dataset> result = datasetsTools.getDatasets(1);

        assertThat(result).hasSize(2);
        verify(apiClient).getDatasets(1);
    }

    @Test
    void testAddDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(2);
        Map<String, Object> data = Map.of("name", "Test");
        when(apiClient.addDataset(1, data)).thenReturn(dataset);

        Dataset result = datasetsTools.addDataset(1, data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).addDataset(1, data);
    }

    @Test
    void testUpdateDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(2);
        Map<String, Object> data = Map.of("name", "Updated");
        when(apiClient.updateDataset(2, data)).thenReturn(dataset);

        Dataset result = datasetsTools.updateDataset(2, data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).updateDataset(2, data);
    }

    @Test
    void testDeleteDataset() {
        doNothing().when(apiClient).deleteDataset(2);

        datasetsTools.deleteDataset(2);

        verify(apiClient).deleteDataset(2);
    }
}
