package io.github.testrail.mcp.tools.plans;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.TestPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PlansTools")
class PlansToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private PlansTools plansTools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        plansTools = new PlansTools(apiClient);
    }

    @Nested
    @DisplayName("getPlan")
    class GetPlan {

        @Test
        @DisplayName("should retrieve plan by ID")
        void shouldRetrievePlanById() {
            // Given
            Integer planId = 123;
            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(planId);
            expectedPlan.setName("Sprint 23 Testing");
            expectedPlan.setIsCompleted(false);

            when(apiClient.getPlan(planId)).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.getPlan(planId);

            // Then
            assertNotNull(result);
            assertEquals(planId, result.getId());
            assertEquals("Sprint 23 Testing", result.getName());
            assertFalse(result.getIsCompleted());
            verify(apiClient).getPlan(planId);
        }
    }

    @Nested
    @DisplayName("getPlans")
    class GetPlans {

        @Test
        @DisplayName("should retrieve all plans for a project")
        void shouldRetrieveAllPlansForProject() {
            // Given
            Integer projectId = 1;
            TestPlan plan1 = new TestPlan();
            plan1.setId(1);
            plan1.setName("Plan 1");

            TestPlan plan2 = new TestPlan();
            plan2.setId(2);
            plan2.setName("Plan 2");

            List<TestPlan> expectedPlans = List.of(plan1, plan2);

            when(apiClient.getPlans(projectId, null, null, null, null, null, null, null))
                    .thenReturn(expectedPlans);

            // When
            List<TestPlan> result = plansTools.getPlans(projectId, null, null, null, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Plan 1", result.get(0).getName());
            assertEquals("Plan 2", result.get(1).getName());
            verify(apiClient).getPlans(projectId, null, null, null, null, null, null, null);
        }

        @Test
        @DisplayName("should retrieve plans filtered by completion status")
        void shouldRetrievePlansFilteredByCompletionStatus() {
            // Given
            Integer projectId = 1;
            Integer isCompleted = 0; // Active only
            TestPlan plan1 = new TestPlan();
            plan1.setId(1);
            plan1.setIsCompleted(false);

            List<TestPlan> expectedPlans = List.of(plan1);

            when(apiClient.getPlans(projectId, null, null, null, isCompleted, null, null, null))
                    .thenReturn(expectedPlans);

            // When
            List<TestPlan> result = plansTools.getPlans(projectId, null, null, null, isCompleted, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertFalse(result.get(0).getIsCompleted());
            verify(apiClient).getPlans(projectId, null, null, null, isCompleted, null, null, null);
        }

        @Test
        @DisplayName("should retrieve plans with all filters")
        void shouldRetrievePlansWithAllFilters() {
            // Given
            Integer projectId = 1;
            Long createdAfter = 1640000000L;
            Long createdBefore = 1650000000L;
            String createdBy = "1,2";
            Integer isCompleted = 1;
            String milestoneId = "5,10";
            Integer limit = 50;
            Integer offset = 100;

            when(apiClient.getPlans(projectId, createdAfter, createdBefore, createdBy, isCompleted, milestoneId, limit, offset))
                    .thenReturn(List.of());

            // When
            List<TestPlan> result = plansTools.getPlans(projectId, createdAfter, createdBefore, createdBy, isCompleted, milestoneId, limit, offset);

            // Then
            assertNotNull(result);
            verify(apiClient).getPlans(projectId, createdAfter, createdBefore, createdBy, isCompleted, milestoneId, limit, offset);
        }
    }

    @Nested
    @DisplayName("addPlan")
    class AddPlan {

        @Test
        @DisplayName("should create plan with required fields only")
        void shouldCreatePlanWithRequiredFieldsOnly() {
            // Given
            Integer projectId = 1;
            String name = "Sprint 24 Testing";
            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(123);
            expectedPlan.setName(name);

            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            when(apiClient.addPlan(eq(projectId), dataCaptor.capture())).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.addPlan(projectId, name, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(123, result.getId());
            assertEquals(name, result.getName());

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals(name, capturedData.get("name"));
            assertEquals(1, capturedData.size());

            verify(apiClient).addPlan(eq(projectId), any());
        }

        @Test
        @DisplayName("should create plan with all fields")
        void shouldCreatePlanWithAllFields() {
            // Given
            Integer projectId = 1;
            String name = "Sprint 24 Testing";
            String description = "Complete testing for Sprint 24";
            Integer milestoneId = 5;
            Long startOn = 1640000000L;
            Long dueOn = 1650000000L;

            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(123);

            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            when(apiClient.addPlan(eq(projectId), dataCaptor.capture())).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.addPlan(projectId, name, description, milestoneId, startOn, dueOn);

            // Then
            assertNotNull(result);

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals(name, capturedData.get("name"));
            assertEquals(description, capturedData.get("description"));
            assertEquals(milestoneId, capturedData.get("milestone_id"));
            assertEquals(startOn, capturedData.get("start_on"));
            assertEquals(dueOn, capturedData.get("due_on"));

            verify(apiClient).addPlan(eq(projectId), any());
        }
    }

    @Nested
    @DisplayName("updatePlan")
    class UpdatePlan {

        @Test
        @DisplayName("should update plan name only")
        void shouldUpdatePlanNameOnly() {
            // Given
            Integer planId = 123;
            String newName = "Updated Plan Name";
            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(planId);
            expectedPlan.setName(newName);

            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            when(apiClient.updatePlan(eq(planId), dataCaptor.capture())).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.updatePlan(planId, newName, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(newName, result.getName());

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals(newName, capturedData.get("name"));
            assertEquals(1, capturedData.size());

            verify(apiClient).updatePlan(eq(planId), any());
        }

        @Test
        @DisplayName("should update plan with all fields")
        void shouldUpdatePlanWithAllFields() {
            // Given
            Integer planId = 123;
            String name = "Updated Plan";
            String description = "Updated description";
            Integer milestoneId = 10;
            Long startOn = 1640000000L;
            Long dueOn = 1650000000L;

            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(planId);

            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            when(apiClient.updatePlan(eq(planId), dataCaptor.capture())).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.updatePlan(planId, name, description, milestoneId, startOn, dueOn);

            // Then
            assertNotNull(result);

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertEquals(name, capturedData.get("name"));
            assertEquals(description, capturedData.get("description"));
            assertEquals(milestoneId, capturedData.get("milestone_id"));
            assertEquals(startOn, capturedData.get("start_on"));
            assertEquals(dueOn, capturedData.get("due_on"));

            verify(apiClient).updatePlan(eq(planId), any());
        }

        @Test
        @DisplayName("should update plan with no fields (empty update)")
        void shouldUpdatePlanWithNoFields() {
            // Given
            Integer planId = 123;
            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(planId);

            ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
            when(apiClient.updatePlan(eq(planId), dataCaptor.capture())).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.updatePlan(planId, null, null, null, null, null);

            // Then
            assertNotNull(result);

            Map<String, Object> capturedData = dataCaptor.getValue();
            assertTrue(capturedData.isEmpty());

            verify(apiClient).updatePlan(eq(planId), any());
        }
    }

    @Nested
    @DisplayName("closePlan")
    class ClosePlan {

        @Test
        @DisplayName("should close plan")
        void shouldClosePlan() {
            // Given
            Integer planId = 123;
            TestPlan expectedPlan = new TestPlan();
            expectedPlan.setId(planId);
            expectedPlan.setIsCompleted(true);

            when(apiClient.closePlan(planId)).thenReturn(expectedPlan);

            // When
            TestPlan result = plansTools.closePlan(planId);

            // Then
            assertNotNull(result);
            assertEquals(planId, result.getId());
            assertTrue(result.getIsCompleted());
            verify(apiClient).closePlan(planId);
        }
    }

    @Nested
    @DisplayName("deletePlan")
    class DeletePlan {

        @Test
        @DisplayName("should delete plan successfully")
        void shouldDeletePlanSuccessfully() {
            // Given
            Integer planId = 123;
            doNothing().when(apiClient).deletePlan(planId);

            // When
            OperationResult result = plansTools.deletePlan(planId);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Test plan 123 deleted successfully", result.getMessage());
            verify(apiClient).deletePlan(planId);
        }

        @Test
        @DisplayName("should handle delete plan failure")
        void shouldHandleDeletePlanFailure() {
            // Given
            Integer planId = 123;
            doThrow(new RuntimeException("API error")).when(apiClient).deletePlan(planId);

            // When
            OperationResult result = plansTools.deletePlan(planId);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Failed to delete plan"));
            verify(apiClient).deletePlan(planId);
        }
    }
}
