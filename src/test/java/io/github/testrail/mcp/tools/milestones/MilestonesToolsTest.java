package io.github.testrail.mcp.tools.milestones;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Milestone;
import io.github.testrail.mcp.model.OperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MilestonesToolsTest {
    
    @Mock
    private TestrailApiClient apiClient;
    
    @InjectMocks
    private MilestonesTools milestonesTools;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void getMilestone() {
        Milestone milestone = new Milestone();
        milestone.setId(1);
        milestone.setName("Sprint 1");
        
        when(apiClient.getMilestone(1)).thenReturn(milestone);
        
        Milestone result = milestonesTools.getMilestone(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Sprint 1", result.getName());
        verify(apiClient).getMilestone(1);
    }
    
    @Test
    void getMilestones() {
        Milestone[] milestones = {new Milestone(), new Milestone()};
        
        when(apiClient.getMilestones(1, null, null, null, null)).thenReturn(milestones);
        
        Object[] result = milestonesTools.getMilestones(1, null, null, null, null);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        verify(apiClient).getMilestones(1, null, null, null, null);
    }
    
    @Test
    void getMilestonesWithFilters() {
        Milestone[] milestones = {new Milestone()};
        
        when(apiClient.getMilestones(1, true, false, 100, 0)).thenReturn(milestones);
        
        Object[] result = milestonesTools.getMilestones(1, true, false, 100, 0);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        verify(apiClient).getMilestones(1, true, false, 100, 0);
    }
    
    @Test
    void addMilestone() {
        Milestone milestone = new Milestone();
        milestone.setId(1);
        milestone.setName("Sprint 2");
        
        when(apiClient.addMilestone(eq(1), anyMap())).thenReturn(milestone);
        
        Milestone result = milestonesTools.addMilestone(1, "Sprint 2", "Description", 1735689600L, 1735603200L, null, "REQ-123");
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Sprint 2", result.getName());
        verify(apiClient).addMilestone(eq(1), anyMap());
    }
    
    @Test
    void addMilestoneMinimal() {
        Milestone milestone = new Milestone();
        milestone.setId(1);
        
        when(apiClient.addMilestone(eq(1), anyMap())).thenReturn(milestone);
        
        Milestone result = milestonesTools.addMilestone(1, "Sprint 3", null, null, null, null, null);
        
        assertNotNull(result);
        verify(apiClient).addMilestone(eq(1), anyMap());
    }
    
    @Test
    void updateMilestone() {
        Milestone milestone = new Milestone();
        milestone.setId(1);
        milestone.setIsCompleted(true);
        
        when(apiClient.updateMilestone(eq(1), anyMap())).thenReturn(milestone);
        
        Milestone result = milestonesTools.updateMilestone(1, "Updated Name", null, null, null, true, null, null);
        
        assertNotNull(result);
        assertEquals(true, result.getIsCompleted());
        verify(apiClient).updateMilestone(eq(1), anyMap());
    }
    
    @Test
    void deleteMilestone() {
        doNothing().when(apiClient).deleteMilestone(1);
        
        OperationResult result = milestonesTools.deleteMilestone(1);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(apiClient).deleteMilestone(1);
    }
}
