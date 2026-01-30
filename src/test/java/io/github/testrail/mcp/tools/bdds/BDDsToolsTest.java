package io.github.testrail.mcp.tools.bdds;

import io.github.testrail.mcp.tools.bdds.*;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BDDsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private BDDsTools bddsTools;

    @BeforeEach
    void setUp() {
        bddsTools = new BDDsTools(apiClient);
    }

    @Test
    void testGetBdd() {
        String featureContent = "@APP-1\nFeature: Login";
        when(apiClient.getBdd(123)).thenReturn(featureContent);

        String result = bddsTools.getBdd(123);

        assertThat(result).isEqualTo(featureContent);
        verify(apiClient).getBdd(123);
    }

    @Test
    void testAddBdd() {
        TestCase testCase = new TestCase();
        testCase.setId(456);
        when(apiClient.addBdd(789, "Feature: Login")).thenReturn(testCase);

        TestCase result = bddsTools.addBdd(789, "Feature: Login");

        assertThat(result.getId()).isEqualTo(456);
        verify(apiClient).addBdd(789, "Feature: Login");
    }
}
