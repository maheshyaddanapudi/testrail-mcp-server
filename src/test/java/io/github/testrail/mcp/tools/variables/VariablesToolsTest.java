package io.github.testrail.mcp.tools.variables;

import io.github.testrail.mcp.tools.variables.*;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Variable;
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
class VariablesToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private VariablesTools variablesTools;

    @BeforeEach
    void setUp() {
        variablesTools = new VariablesTools(apiClient);
    }

    @Test
    void testGetVariables() {
        List<Variable> variables = List.of(new Variable(), new Variable());
        when(apiClient.getVariables(1)).thenReturn(variables);

        List<Variable> result = variablesTools.getVariables(1);

        assertThat(result).hasSize(2);
        verify(apiClient).getVariables(1);
    }

    @Test
    void testAddVariable() {
        Variable variable = new Variable();
        variable.setId(2);
        Map<String, Object> data = Map.of("name", "email");
        when(apiClient.addVariable(1, data)).thenReturn(variable);

        Variable result = variablesTools.addVariable(1, data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).addVariable(1, data);
    }

    @Test
    void testUpdateVariable() {
        Variable variable = new Variable();
        variable.setId(2);
        Map<String, Object> data = Map.of("name", "user_email");
        when(apiClient.updateVariable(2, data)).thenReturn(variable);

        Variable result = variablesTools.updateVariable(2, data);

        assertThat(result.getId()).isEqualTo(2);
        verify(apiClient).updateVariable(2, data);
    }

    @Test
    void testDeleteVariable() {
        doNothing().when(apiClient).deleteVariable(2);

        variablesTools.deleteVariable(2);

        verify(apiClient).deleteVariable(2);
    }
}
