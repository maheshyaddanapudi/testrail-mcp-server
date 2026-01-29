package io.github.testrail.mcp.tools.tests;

import io.github.testrail.mcp.client.TestrailApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TestsTools")
class TestsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private TestsTools testsTools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testsTools = new TestsTools(apiClient);
    }

    @Nested
    @DisplayName("getTest")
    class GetTest {

        @Test
        @DisplayName("should retrieve test by ID")
        void shouldRetrieveTestById() {
            // Given
            Integer testId = 123;
            io.github.testrail.mcp.model.Test expectedTest = new io.github.testrail.mcp.model.Test();
            expectedTest.setId(testId);
            expectedTest.setTitle("Test login functionality");
            expectedTest.setStatusId(1);

            when(apiClient.getTest(testId, null)).thenReturn(expectedTest);

            // When
            io.github.testrail.mcp.model.Test result = testsTools.getTest(testId, null);

            // Then
            assertNotNull(result);
            assertEquals(testId, result.getId());
            assertEquals("Test login functionality", result.getTitle());
            verify(apiClient).getTest(testId, null);
        }

        @Test
        @DisplayName("should retrieve test with additional data")
        void shouldRetrieveTestWithData() {
            // Given
            Integer testId = 456;
            String withData = "steps";
            io.github.testrail.mcp.model.Test expectedTest = new io.github.testrail.mcp.model.Test();
            expectedTest.setId(testId);

            when(apiClient.getTest(testId, withData)).thenReturn(expectedTest);

            // When
            io.github.testrail.mcp.model.Test result = testsTools.getTest(testId, withData);

            // Then
            assertNotNull(result);
            assertEquals(testId, result.getId());
            verify(apiClient).getTest(testId, withData);
        }
    }

    @Nested
    @DisplayName("getTests")
    class GetTests {

        @Test
        @DisplayName("should retrieve all tests for a run")
        void shouldRetrieveAllTestsForRun() {
            // Given
            Integer runId = 100;
            io.github.testrail.mcp.model.Test test1 = new io.github.testrail.mcp.model.Test();
            test1.setId(1);
            test1.setTitle("Test 1");

            io.github.testrail.mcp.model.Test test2 = new io.github.testrail.mcp.model.Test();
            test2.setId(2);
            test2.setTitle("Test 2");

            List<io.github.testrail.mcp.model.Test> expectedTests = List.of(test1, test2);

            when(apiClient.getTests(runId, null, null, null, null)).thenReturn(expectedTests);

            // When
            List<io.github.testrail.mcp.model.Test> result = testsTools.getTests(runId, null, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Test 1", result.get(0).getTitle());
            assertEquals("Test 2", result.get(1).getTitle());
            verify(apiClient).getTests(runId, null, null, null, null);
        }

        @Test
        @DisplayName("should retrieve tests filtered by status")
        void shouldRetrieveTestsFilteredByStatus() {
            // Given
            Integer runId = 100;
            String statusId = "4,5"; // Failed and retest
            io.github.testrail.mcp.model.Test test1 = new io.github.testrail.mcp.model.Test();
            test1.setId(1);
            test1.setStatusId(4);

            List<io.github.testrail.mcp.model.Test> expectedTests = List.of(test1);

            when(apiClient.getTests(runId, statusId, null, null, null)).thenReturn(expectedTests);

            // When
            List<io.github.testrail.mcp.model.Test> result = testsTools.getTests(runId, statusId, null, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(4, result.get(0).getStatusId());
            verify(apiClient).getTests(runId, statusId, null, null, null);
        }

        @Test
        @DisplayName("should retrieve tests filtered by label")
        void shouldRetrieveTestsFilteredByLabel() {
            // Given
            Integer runId = 100;
            String labelId = "10,20";
            io.github.testrail.mcp.model.Test test1 = new io.github.testrail.mcp.model.Test();
            test1.setId(1);

            List<io.github.testrail.mcp.model.Test> expectedTests = List.of(test1);

            when(apiClient.getTests(runId, null, labelId, null, null)).thenReturn(expectedTests);

            // When
            List<io.github.testrail.mcp.model.Test> result = testsTools.getTests(runId, null, labelId, null, null);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(apiClient).getTests(runId, null, labelId, null, null);
        }

        @Test
        @DisplayName("should retrieve tests with pagination")
        void shouldRetrieveTestsWithPagination() {
            // Given
            Integer runId = 100;
            Integer limit = 50;
            Integer offset = 100;
            io.github.testrail.mcp.model.Test test1 = new io.github.testrail.mcp.model.Test();
            test1.setId(1);

            List<io.github.testrail.mcp.model.Test> expectedTests = List.of(test1);

            when(apiClient.getTests(runId, null, null, limit, offset)).thenReturn(expectedTests);

            // When
            List<io.github.testrail.mcp.model.Test> result = testsTools.getTests(runId, null, null, limit, offset);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(apiClient).getTests(runId, null, null, limit, offset);
        }

        @Test
        @DisplayName("should retrieve tests with all filters")
        void shouldRetrieveTestsWithAllFilters() {
            // Given
            Integer runId = 100;
            String statusId = "1,4";
            String labelId = "10";
            Integer limit = 25;
            Integer offset = 50;

            when(apiClient.getTests(runId, statusId, labelId, limit, offset)).thenReturn(List.of());

            // When
            List<io.github.testrail.mcp.model.Test> result = testsTools.getTests(runId, statusId, labelId, limit, offset);

            // Then
            assertNotNull(result);
            verify(apiClient).getTests(runId, statusId, labelId, limit, offset);
        }
    }
}
