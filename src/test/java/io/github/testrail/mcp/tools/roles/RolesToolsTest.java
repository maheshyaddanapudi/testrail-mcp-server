package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private RolesTools rolesTools;

    @BeforeEach
    void setUp() {
        rolesTools = new RolesTools(apiClient);
    }

    @Test
    void getRoles_shouldReturnAllRoles() {
        Role role1 = new Role();
        role1.setId(1);
        role1.setName("Tester");
        role1.setIsDefault(false);

        Role role2 = new Role();
        role2.setId(2);
        role2.setName("Lead");
        role2.setIsDefault(true);

        when(apiClient.getRoles()).thenReturn(List.of(role1, role2));

        List<Role> result = rolesTools.getRoles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Tester");
        assertThat(result.get(1).getName()).isEqualTo("Lead");
        assertThat(result.get(1).getIsDefault()).isTrue();
        verify(apiClient).getRoles();
    }
}
