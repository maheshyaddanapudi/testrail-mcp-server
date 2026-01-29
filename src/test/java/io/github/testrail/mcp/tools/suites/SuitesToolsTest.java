package io.github.testrail.mcp.tools.suites;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Suite;
import io.github.testrail.mcp.model.OperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SuitesToolsTest {
    
    @Mock
    private TestrailApiClient apiClient;
    
    @InjectMocks
    private SuitesTools suitesTools;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void getSuite() {
        Suite suite = new Suite();
        suite.setId(1);
        suite.setName("API Tests");
        
        when(apiClient.getSuite(1)).thenReturn(suite);
        
        Suite result = suitesTools.getSuite(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("API Tests", result.getName());
        verify(apiClient).getSuite(1);
    }
    
    @Test
    void getSuites() {
        Suite[] suites = {new Suite(), new Suite()};
        
        when(apiClient.getSuites(1)).thenReturn(suites);
        
        Object[] result = suitesTools.getSuites(1);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        verify(apiClient).getSuites(1);
    }
    
    @Test
    void addSuite() {
        Suite suite = new Suite();
        suite.setId(1);
        suite.setName("New Suite");
        
        when(apiClient.addSuite(eq(1), anyMap())).thenReturn(suite);
        
        Suite result = suitesTools.addSuite(1, "New Suite", "Description");
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Suite", result.getName());
        verify(apiClient).addSuite(eq(1), anyMap());
    }
    
    @Test
    void addSuiteMinimal() {
        Suite suite = new Suite();
        suite.setId(1);
        
        when(apiClient.addSuite(eq(1), anyMap())).thenReturn(suite);
        
        Suite result = suitesTools.addSuite(1, "Minimal Suite", null);
        
        assertNotNull(result);
        verify(apiClient).addSuite(eq(1), anyMap());
    }
    
    @Test
    void updateSuite() {
        Suite suite = new Suite();
        suite.setId(1);
        suite.setName("Updated Suite");
        
        when(apiClient.updateSuite(eq(1), anyMap())).thenReturn(suite);
        
        Suite result = suitesTools.updateSuite(1, "Updated Suite", "New Description");
        
        assertNotNull(result);
        assertEquals("Updated Suite", result.getName());
        verify(apiClient).updateSuite(eq(1), anyMap());
    }
    
    @Test
    void deleteSuite() {
        doNothing().when(apiClient).deleteSuite(1, null);
        
        OperationResult result = suitesTools.deleteSuite(1, null);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(apiClient).deleteSuite(1, null);
    }
    
    @Test
    void deleteSuiteSoft() {
        doNothing().when(apiClient).deleteSuite(1, 1);
        
        OperationResult result = suitesTools.deleteSuite(1, 1);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("preview"));
        verify(apiClient).deleteSuite(1, 1);
    }
}
