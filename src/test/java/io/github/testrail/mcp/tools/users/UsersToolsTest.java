package io.github.testrail.mcp.tools.users;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsersToolsTest {
    
    @Mock
    private TestrailApiClient apiClient;
    
    @InjectMocks
    private UsersTools usersTools;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void getUser() {
        User user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        
        when(apiClient.getUser(1)).thenReturn(user);
        
        User result = usersTools.getUser(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(apiClient).getUser(1);
    }
    
    @Test
    void getCurrentUser() {
        User user = new User();
        user.setId(1);
        user.setName("Current User");
        
        when(apiClient.getCurrentUser()).thenReturn(user);
        
        User result = usersTools.getCurrentUser();
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Current User", result.getName());
        verify(apiClient).getCurrentUser();
    }
    
    @Test
    void getUserByEmail() {
        User user = new User();
        user.setId(1);
        user.setEmail("john@example.com");
        
        when(apiClient.getUserByEmail("john@example.com")).thenReturn(user);
        
        User result = usersTools.getUserByEmail("john@example.com");
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("john@example.com", result.getEmail());
        verify(apiClient).getUserByEmail("john@example.com");
    }
    
    @Test
    void getUsers() {
        User[] users = {new User(), new User()};
        
        when(apiClient.getUsers(null)).thenReturn(users);
        
        Object[] result = usersTools.getUsers(null);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        verify(apiClient).getUsers(null);
    }
    
    @Test
    void getUsersForProject() {
        User[] users = {new User()};
        
        when(apiClient.getUsers(1)).thenReturn(users);
        
        Object[] result = usersTools.getUsers(1);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        verify(apiClient).getUsers(1);
    }
}
