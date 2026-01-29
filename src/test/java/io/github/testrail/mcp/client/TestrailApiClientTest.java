package io.github.testrail.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.model.OperationResult;
import io.github.testrail.mcp.model.Project;
import io.github.testrail.mcp.model.TestCase;
import io.github.testrail.mcp.model.TestRun;
import io.github.testrail.mcp.model.TestResult;
import io.github.testrail.mcp.model.Section;
import io.github.testrail.mcp.client.TestrailApiException;
import io.github.testrail.mcp.model.TestPlan;
import io.github.testrail.mcp.model.Attachment;
import io.github.testrail.mcp.model.SharedStep;
import io.github.testrail.mcp.model.SharedStepHistory;
import io.github.testrail.mcp.model.Report;
import io.github.testrail.mcp.model.Role;
import io.github.testrail.mcp.model.Dataset;
import io.github.testrail.mcp.model.Group;
import io.github.testrail.mcp.model.Label;
import io.github.testrail.mcp.model.Variable;
import com.fasterxml.jackson.databind.JsonNode;
// Note: Test class is NOT imported to avoid conflict with JUnit @Test annotation
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TestrailApiClient.
 */
class TestrailApiClientTest {

    private static MockWebServer mockWebServer;
    private TestrailApiClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = String.format("http://localhost:%s/", mockWebServer.getPort());

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic dGVzdDprZXk=")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        apiClient = new TestrailApiClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Drain any remaining requests to prevent test pollution
        // Try to drain up to 10 pending requests with a short timeout
        for (int i = 0; i < 10; i++) {
            if (mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS) == null) {
                break; // No more pending requests
            }
        }
    }

    // ==================== Cases Tests ====================

    @Test
    void getCase_shouldReturnTestCase() throws Exception {
        TestCase expected = new TestCase();
        expected.setId(123);
        expected.setTitle("Login Test");
        expected.setSectionId(1);
        expected.setPriorityId(2);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        TestCase result = apiClient.getCase(123);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getTitle()).isEqualTo("Login Test");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_case/123");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void getCase_shouldThrowException_whenNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\": \"Field :case_id is not a valid test case.\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        assertThatThrownBy(() -> apiClient.getCase(999))
                .isInstanceOf(TestrailApiException.class)
                .hasMessageContaining("TestRail API error");
    }

    @Test
    void getCases_shouldReturnListOfCases() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> case1 = new HashMap<>();
        case1.put("id", 1);
        case1.put("title", "Test 1");
        Map<String, Object> case2 = new HashMap<>();
        case2.put("id", 2);
        case2.put("title", "Test 2");
        cases.add(case1);
        cases.add(case2);
        response.put("cases", cases);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestCase> result = apiClient.getCases(1, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getId()).isEqualTo(2);
    }

    @Test
    void getCases_shouldIncludeFiltersInUrl() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"cases\": []}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getCases(1, 5, 10, 50, 100);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath())
                .contains("get_cases/1")
                .contains("suite_id=5")
                .contains("section_id=10")
                .contains("limit=50")
                .contains("offset=100");
    }

    @Test
    void addCase_shouldCreateTestCase() throws Exception {
        TestCase expected = new TestCase();
        expected.setId(456);
        expected.setTitle("New Test");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Test");
        data.put("custom_steps", "Step 1\nStep 2");

        TestCase result = apiClient.addCase(1, data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(456);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_case/1");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void updateCase_shouldUpdateTestCase() throws Exception {
        TestCase expected = new TestCase();
        expected.setId(123);
        expected.setTitle("Updated Test");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Test");

        TestCase result = apiClient.updateCase(123, data);

        assertThat(result.getTitle()).isEqualTo("Updated Test");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_case/123");
    }

    @Test
    void deleteCase_shouldDeleteTestCase() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        assertThatCode(() -> apiClient.deleteCase(123)).doesNotThrowAnyException();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_case/123");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    // ==================== Projects Tests ====================

    @Test
    void getProject_shouldReturnProject() throws Exception {
        Project expected = new Project();
        expected.setId(1);
        expected.setName("Mobile App");
        expected.setSuiteMode(1);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Project result = apiClient.getProject(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Mobile App");
    }

    @Test
    void getProjects_shouldReturnListOfProjects() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> projects = new ArrayList<>();
        Map<String, Object> project1 = new HashMap<>();
        project1.put("id", 1);
        project1.put("name", "Project 1");
        projects.add(project1);
        response.put("projects", projects);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Project> result = apiClient.getProjects(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Project 1");
    }

    @Test
    void addProject_shouldCreateProject() throws Exception {
        Project expected = new Project();
        expected.setId(5);
        expected.setName("New Project");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Project");

        Project result = apiClient.addProject(data);

        assertThat(result.getId()).isEqualTo(5);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_project");
    }

    @Test
    void deleteProject_shouldDeleteProject() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        assertThatCode(() -> apiClient.deleteProject(1)).doesNotThrowAnyException();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_project/1");
    }

    // ==================== Runs Tests ====================

    @Test
    void getRun_shouldReturnTestRun() throws Exception {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setName("Sprint 10 Regression");
        expected.setPassedCount(50);
        expected.setFailedCount(5);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        TestRun result = apiClient.getRun(100);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sprint 10 Regression");
        assertThat(result.getPassedCount()).isEqualTo(50);
    }

    @Test
    void getRuns_shouldReturnListOfRuns() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> runs = new ArrayList<>();
        Map<String, Object> run1 = new HashMap<>();
        run1.put("id", 1);
        run1.put("name", "Run 1");
        runs.add(run1);
        response.put("runs", runs);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestRun> result = apiClient.getRuns(1, null, null, null, null, null, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void addRun_shouldCreateTestRun() throws Exception {
        TestRun expected = new TestRun();
        expected.setId(200);
        expected.setName("New Run");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Run");

        TestRun result = apiClient.addRun(1, data);

        assertThat(result.getId()).isEqualTo(200);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_run/1");
    }

    @Test
    void closeRun_shouldCloseTestRun() throws Exception {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setIsCompleted(true);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        TestRun result = apiClient.closeRun(100);

        assertThat(result.getIsCompleted()).isTrue();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/close_run/100");
    }

    // ==================== Results Tests ====================

    @Test
    void getResults_shouldReturnResults() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", 1);
        result1.put("status_id", 1);
        result1.put("comment", "Passed");
        results.add(result1);
        response.put("results", results);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestResult> resultList = apiClient.getResults(100, null, null, null, null);

        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).getStatusId()).isEqualTo(1);
    }

    @Test
    void addResult_shouldAddResult() throws Exception {
        TestResult expected = new TestResult();
        expected.setId(500);
        expected.setStatusId(1);
        expected.setComment("Test passed");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("status_id", 1);
        data.put("comment", "Test passed");

        TestResult result = apiClient.addResult(100, data);

        assertThat(result.getStatusId()).isEqualTo(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_result/100");
    }

    // ==================== Sections Tests ====================

    @Test
    void getSection_shouldReturnSection() throws Exception {
        Section expected = new Section();
        expected.setId(10);
        expected.setName("Login Tests");
        expected.setDepth(0);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Section result = apiClient.getSection(10);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Login Tests");
    }

    @Test
    void getSections_shouldReturnSections() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> sections = new ArrayList<>();
        Map<String, Object> section1 = new HashMap<>();
        section1.put("id", 1);
        section1.put("name", "Section 1");
        sections.add(section1);
        response.put("sections", sections);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Section> result = apiClient.getSections(1, null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void addSection_shouldCreateSection() throws Exception {
        Section expected = new Section();
        expected.setId(20);
        expected.setName("New Section");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Section");

        Section result = apiClient.addSection(1, data);

        assertThat(result.getId()).isEqualTo(20);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_section/1");
    }

    @Test
    void deleteSection_shouldDeleteWithSoftFlag() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        apiClient.deleteSection(10, true);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_section/10?soft=1");
    }

    @Test
    void deleteSection_shouldDeletePermanently() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        apiClient.deleteSection(10, false);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_section/10");
    }

    // ==================== Error Handling Tests ====================

    @Test
    void apiCall_shouldThrowException_onUnauthorized() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"Authentication failed\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        assertThatThrownBy(() -> apiClient.getProject(1))
                .isInstanceOf(TestrailApiException.class)
                .hasMessageContaining("TestRail API error");
    }

    @Test
    void apiCall_shouldThrowException_onServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal server error\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        assertThatThrownBy(() -> apiClient.getProject(1))
                .isInstanceOf(TestrailApiException.class);
    }

    // ==================== Additional Coverage Tests ====================

    @Test
    void updateProject_shouldUpdateProject() throws Exception {
        Project expected = new Project();
        expected.setId(1);
        expected.setName("Updated Project");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Project");

        Project result = apiClient.updateProject(1, data);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Updated Project");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_project/1");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void updateRun_shouldUpdateTestRun() throws Exception {
        TestRun expected = new TestRun();
        expected.setId(100);
        expected.setName("Updated Run");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Run");

        TestRun result = apiClient.updateRun(100, data);

        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getName()).isEqualTo("Updated Run");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_run/100");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void deleteRun_shouldDeleteTestRun() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        assertThatCode(() -> apiClient.deleteRun(100)).doesNotThrowAnyException();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_run/100");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void updateSection_shouldUpdateSection() throws Exception {
        Section expected = new Section();
        expected.setId(10);
        expected.setName("Updated Section");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Section");

        Section result = apiClient.updateSection(10, data);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("Updated Section");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_section/10");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void moveSection_shouldMoveSection() throws Exception {
        Section expected = new Section();
        expected.setId(10);
        expected.setParentId(5);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(expected))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("parent_id", 5);

        Section result = apiClient.moveSection(10, data);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getParentId()).isEqualTo(5);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/move_section/10");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void getResultsForRun_shouldReturnResults() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", 1);
        result1.put("test_id", 100);
        result1.put("status_id", 1);
        results.add(result1);
        response.put("results", results);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestResult> resultList = apiClient.getResultsForRun(100, null, null, null, null, null, 10, 0);

        assertThat(resultList).hasSize(1);
        assertThat(resultList.get(0).getTestId()).isEqualTo(100);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_results_for_run/100?limit=10&offset=0");
    }

    @Test
    void getResultsForRun_shouldHandleNullParameters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("results", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestResult> resultList = apiClient.getResultsForRun(100, null, null, null, null, null, null, null);

        assertThat(resultList).isEmpty();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_results_for_run/100");
    }

    @Test
    void addResults_shouldAddMultipleResults() throws Exception {
        // Response is a direct array, not wrapped in "results" field
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", 1);
        result1.put("test_id", 100);
        result1.put("status_id", 1);
        results.add(result1);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(results))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Map<String, Object>> resultsData = new ArrayList<>();
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("test_id", 100);
        resultData.put("status_id", 1);
        resultsData.add(resultData);

        List<TestResult> resultList = apiClient.addResults(100, resultsData);

        assertThat(resultList).hasSize(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_results/100");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void addResultsForCases_shouldAddResultsForSpecificCases() throws Exception {
        // Response is a direct array, not wrapped in "results" field
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", 1);
        result1.put("case_id", 123);
        result1.put("status_id", 1);
        results.add(result1);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(results))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Map<String, Object>> resultsData = new ArrayList<>();
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("case_id", 123);
        resultData.put("status_id", 1);
        resultsData.add(resultData);

        List<TestResult> resultList = apiClient.addResultsForCases(100, resultsData);

        assertThat(resultList).hasSize(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_results_for_cases/100");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void getSections_shouldHandleAllParameters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> sections = new ArrayList<>();
        Map<String, Object> section1 = new HashMap<>();
        section1.put("id", 1);
        section1.put("name", "Section 1");
        sections.add(section1);
        response.put("sections", sections);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Section> resultList = apiClient.getSections(1, 2, 10, 5);

        assertThat(resultList).hasSize(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_sections/1");
        assertThat(request.getPath()).contains("suite_id=2");
        assertThat(request.getPath()).contains("limit=10");
        assertThat(request.getPath()).contains("offset=5");
    }

    @Test
    void getRuns_shouldHandleAllParameters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> runs = new ArrayList<>();
        Map<String, Object> run1 = new HashMap<>();
        run1.put("id", 1);
        run1.put("name", "Run 1");
        runs.add(run1);
        response.put("runs", runs);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestRun> resultList = apiClient.getRuns(1, null, null, null, null, null, null, 10, 5);

        assertThat(resultList).hasSize(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_runs/1");
        assertThat(request.getPath()).contains("limit=10");
        assertThat(request.getPath()).contains("offset=5");
    }

    @Test
    void getResults_shouldHandleAllParameters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> result1 = new HashMap<>();
        result1.put("id", 1);
        result1.put("status_id", 1);
        results.add(result1);
        response.put("results", results);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<TestResult> resultList = apiClient.getResults(100, null, null, 10, 5);

        assertThat(resultList).hasSize(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_results/100");
        assertThat(request.getPath()).contains("limit=10");
        assertThat(request.getPath()).contains("offset=5");
    }

    // ==================== Filter Parameters Tests ====================

    @Test
    void getProjects_shouldApplyFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("projects", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getProjects(true, 50, 10);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_projects");
        assertThat(request.getPath()).contains("is_completed=1");
        assertThat(request.getPath()).contains("limit=50");
        assertThat(request.getPath()).contains("offset=10");
    }

    @Test
    void getProjects_shouldHandleIsCompletedFalse() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("projects", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getProjects(false, null, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("is_completed=0");
    }

    @Test
    void getRuns_shouldApplyAllFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("runs", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getRuns(1, true, 1640000000L, 1650000000L, "1,2,3", "5,6", "10,11", 100, 20);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_runs/1");
        assertThat(request.getPath()).contains("is_completed=1");
        assertThat(request.getPath()).contains("created_after=1640000000");
        assertThat(request.getPath()).contains("created_before=1650000000");
        assertThat(request.getPath()).contains("created_by=1,2,3");
        assertThat(request.getPath()).contains("milestone_id=5,6");
        assertThat(request.getPath()).contains("suite_id=10,11");
        assertThat(request.getPath()).contains("limit=100");
        assertThat(request.getPath()).contains("offset=20");
    }

    @Test
    void getResults_shouldApplyDefectsAndStatusFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("results", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getResults(100, "TR-123", "1,5", 50, 10);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_results/100");
        assertThat(request.getPath()).contains("defects_filter=TR-123");
        assertThat(request.getPath()).contains("status_id=1,5");
        assertThat(request.getPath()).contains("limit=50");
        assertThat(request.getPath()).contains("offset=10");
    }

    @Test
    void getResultsForRun_shouldApplyAllFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("results", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getResultsForRun(100, 1640000000L, 1650000000L, "1,2", "TR-456", "1,5", 100, 50);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_results_for_run/100");
        assertThat(request.getPath()).contains("created_after=1640000000");
        assertThat(request.getPath()).contains("created_before=1650000000");
        assertThat(request.getPath()).contains("created_by=1,2");
        assertThat(request.getPath()).contains("defects_filter=TR-456");
        assertThat(request.getPath()).contains("status_id=1,5");
        assertThat(request.getPath()).contains("limit=100");
        assertThat(request.getPath()).contains("offset=50");
    }

    // ==================== Tests API Tests ====================

    @Test
    @DisplayName("getTest should retrieve test by ID")
    void testGetTest() throws Exception {
        io.github.testrail.mcp.model.Test test = new io.github.testrail.mcp.model.Test();
        test.setId(123);
        test.setTitle("Test login functionality");
        test.setStatusId(1);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(test))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Test result = apiClient.getTest(123, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getTitle()).isEqualTo("Test login functionality");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_test/123");
    }

    @Test
    @DisplayName("getTest should retrieve test with data parameter")
    void testGetTestWithData() throws Exception {
        io.github.testrail.mcp.model.Test test = new io.github.testrail.mcp.model.Test();
        test.setId(456);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(test))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Test result = apiClient.getTest(456, "steps");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(456);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_test/456?with_data=steps");
    }

    @Test
    @DisplayName("getTests should retrieve tests for a run")
    void testGetTests() throws Exception {
        io.github.testrail.mcp.model.Test test1 = new io.github.testrail.mcp.model.Test();
        test1.setId(1);
        test1.setTitle("Test 1");

        io.github.testrail.mcp.model.Test test2 = new io.github.testrail.mcp.model.Test();
        test2.setId(2);
        test2.setTitle("Test 2");

        Map<String, Object> response = new HashMap<>();
        response.put("tests", List.of(test1, test2));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<io.github.testrail.mcp.model.Test> result = apiClient.getTests(100, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Test 2");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_tests/100");
    }

    @Test
    @DisplayName("getTests should apply all filters")
    void testGetTestsWithFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("tests", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getTests(100, "1,4,5", "10,20", 50, 100);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_tests/100");
        assertThat(request.getPath()).contains("status_id=1,4,5");
        assertThat(request.getPath()).contains("label_id=10,20");
        assertThat(request.getPath()).contains("limit=50");
        assertThat(request.getPath()).contains("offset=100");
    }

    // ==================== Plans API Tests ====================

    @Test
    @DisplayName("getPlan should retrieve plan by ID")
    void testGetPlan() throws Exception {
        io.github.testrail.mcp.model.TestPlan plan = new io.github.testrail.mcp.model.TestPlan();
        plan.setId(123);
        plan.setName("Sprint 23 Testing");
        plan.setIsCompleted(false);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(plan))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.TestPlan result = apiClient.getPlan(123);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getName()).isEqualTo("Sprint 23 Testing");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_plan/123");
    }

    @Test
    @DisplayName("getPlans should retrieve plans for a project")
    void testGetPlans() throws Exception {
        io.github.testrail.mcp.model.TestPlan plan1 = new io.github.testrail.mcp.model.TestPlan();
        plan1.setId(1);
        plan1.setName("Plan 1");

        io.github.testrail.mcp.model.TestPlan plan2 = new io.github.testrail.mcp.model.TestPlan();
        plan2.setId(2);
        plan2.setName("Plan 2");

        Map<String, Object> response = new HashMap<>();
        response.put("plans", List.of(plan1, plan2));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<io.github.testrail.mcp.model.TestPlan> result = apiClient.getPlans(1, null, null, null, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Plan 1");
        assertThat(result.get(1).getName()).isEqualTo("Plan 2");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_plans/1");
    }

    @Test
    @DisplayName("getPlans should apply all filters")
    void testGetPlansWithFilters() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("plans", new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.getPlans(1, 1640000000L, 1650000000L, "1,2", 1, "5,10", 50, 100);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_plans/1");
        assertThat(request.getPath()).contains("created_after=1640000000");
        assertThat(request.getPath()).contains("created_before=1650000000");
        assertThat(request.getPath()).contains("created_by=1,2");
        assertThat(request.getPath()).contains("is_completed=1");
        assertThat(request.getPath()).contains("milestone_id=5,10");
        assertThat(request.getPath()).contains("limit=50");
        assertThat(request.getPath()).contains("offset=100");
    }

    @Test
    @DisplayName("addPlan should create a new plan")
    void testAddPlan() throws Exception {
        io.github.testrail.mcp.model.TestPlan plan = new io.github.testrail.mcp.model.TestPlan();
        plan.setId(123);
        plan.setName("Sprint 24 Testing");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(plan))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Sprint 24 Testing");
        data.put("description", "Test plan for sprint 24");

        io.github.testrail.mcp.model.TestPlan result = apiClient.addPlan(1, data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getName()).isEqualTo("Sprint 24 Testing");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_plan/1");
    }

    @Test
    @DisplayName("updatePlan should update an existing plan")
    void testUpdatePlan() throws Exception {
        io.github.testrail.mcp.model.TestPlan plan = new io.github.testrail.mcp.model.TestPlan();
        plan.setId(123);
        plan.setName("Updated Plan Name");

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(plan))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Plan Name");

        io.github.testrail.mcp.model.TestPlan result = apiClient.updatePlan(123, data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getName()).isEqualTo("Updated Plan Name");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_plan/123");
    }

    @Test
    @DisplayName("closePlan should close a plan")
    void testClosePlan() throws Exception {
        io.github.testrail.mcp.model.TestPlan plan = new io.github.testrail.mcp.model.TestPlan();
        plan.setId(123);
        plan.setIsCompleted(true);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(plan))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.TestPlan result = apiClient.closePlan(123);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123);
        assertThat(result.getIsCompleted()).isTrue();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/close_plan/123");
    }

    @Test
    @DisplayName("deletePlan should delete a plan")
    void testDeletePlan() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deletePlan(123);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_plan/123");
    }

    // ==================== Users API Tests ====================

    @Test
    void testGetUser() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.User user = apiClient.getUser(1);

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_user/1");
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Current User\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.User user = apiClient.getCurrentUser();

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Current User");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_current_user");
    }

    @Test
    void testGetUserByEmail() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"email\":\"john@example.com\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.User user = apiClient.getUserByEmail("john@example.com");

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_user_by_email&email=john@example.com");
    }

    @Test
    void testGetUsers() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"User 1\"},{\"id\":2,\"name\":\"User 2\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] users = apiClient.getUsers(null);

        assertThat(users).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_users");
    }

    @Test
    void testGetUsersForProject() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"User 1\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] users = apiClient.getUsers(1);

        assertThat(users).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_users/1");
    }

    // ==================== Suites API Tests ====================

    @Test
    void testGetSuite() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"API Tests\",\"project_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Suite suite = apiClient.getSuite(1);

        assertThat(suite.getId()).isEqualTo(1);
        assertThat(suite.getName()).isEqualTo("API Tests");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_suite/1");
    }

    @Test
    void testGetSuites() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"suites\":[{\"id\":1,\"name\":\"Suite 1\"},{\"id\":2,\"name\":\"Suite 2\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] suites = apiClient.getSuites(1);

        assertThat(suites).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_suites/1");
    }

    @Test
    void testAddSuite() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"New Suite\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Suite suite = apiClient.addSuite(1, Map.of("name", "New Suite"));

        assertThat(suite.getId()).isEqualTo(1);
        assertThat(suite.getName()).isEqualTo("New Suite");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_suite/1");
    }

    @Test
    void testUpdateSuite() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Updated Suite\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Suite suite = apiClient.updateSuite(1, Map.of("name", "Updated Suite"));

        assertThat(suite.getId()).isEqualTo(1);
        assertThat(suite.getName()).isEqualTo("Updated Suite");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_suite/1");
    }

    @Test
    void testDeleteSuite() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteSuite(1, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_suite/1");
    }

    @Test
    void testDeleteSuiteSoft() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteSuite(1, 1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_suite/1?soft=1");
    }

    // ==================== Milestones API Tests ====================

    @Test
    void testGetMilestone() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Sprint 1\",\"project_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Milestone milestone = apiClient.getMilestone(1);

        assertThat(milestone.getId()).isEqualTo(1);
        assertThat(milestone.getName()).isEqualTo("Sprint 1");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_milestone/1");
    }

    @Test
    void testGetMilestones() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"milestones\":[{\"id\":1,\"name\":\"Milestone 1\"},{\"id\":2,\"name\":\"Milestone 2\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] milestones = apiClient.getMilestones(1, null, null, null, null);

        assertThat(milestones).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_milestones/1");
    }

    @Test
    void testGetMilestonesWithFilters() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"milestones\":[{\"id\":1,\"name\":\"Milestone 1\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] milestones = apiClient.getMilestones(1, true, false, 100, 0);

        assertThat(milestones).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_milestones/1?is_completed=1&is_started=0&limit=100&offset=0");
    }

    @Test
    void testAddMilestone() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Sprint 2\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Milestone milestone = apiClient.addMilestone(1, Map.of("name", "Sprint 2"));

        assertThat(milestone.getId()).isEqualTo(1);
        assertThat(milestone.getName()).isEqualTo("Sprint 2");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_milestone/1");
    }

    @Test
    void testUpdateMilestone() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Updated Milestone\",\"is_completed\":true}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Milestone milestone = apiClient.updateMilestone(1, Map.of("is_completed", true));

        assertThat(milestone.getId()).isEqualTo(1);
        assertThat(milestone.getIsCompleted()).isTrue();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_milestone/1");
    }

    @Test
    void testDeleteMilestone() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteMilestone(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_milestone/1");
    }

    // ==================== Configurations Tests ====================

    @Test
    void testGetConfigs() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Browsers\",\"project_id\":1,\"configs\":[{\"id\":2,\"name\":\"Chrome\"}]}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] configs = apiClient.getConfigs(1);

        assertThat(configs).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_configs/1");
    }

    @Test
    void testAddConfigGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Browsers\",\"project_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.ConfigurationGroup configGroup = apiClient.addConfigGroup(1, Map.of("name", "Browsers"));

        assertThat(configGroup.getId()).isEqualTo(1);
        assertThat(configGroup.getName()).isEqualTo("Browsers");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_config_group/1");
    }

    @Test
    void testAddConfig() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":2,\"name\":\"Chrome\",\"group_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Configuration config = apiClient.addConfig(1, Map.of("name", "Chrome"));

        assertThat(config.getId()).isEqualTo(2);
        assertThat(config.getName()).isEqualTo("Chrome");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_config/1");
    }

    @Test
    void testUpdateConfigGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Updated Browsers\",\"project_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.ConfigurationGroup configGroup = apiClient.updateConfigGroup(1, Map.of("name", "Updated Browsers"));

        assertThat(configGroup.getId()).isEqualTo(1);
        assertThat(configGroup.getName()).isEqualTo("Updated Browsers");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_config_group/1");
    }

    @Test
    void testUpdateConfig() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":2,\"name\":\"Firefox\",\"group_id\":1}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.Configuration config = apiClient.updateConfig(2, Map.of("name", "Firefox"));

        assertThat(config.getId()).isEqualTo(2);
        assertThat(config.getName()).isEqualTo("Firefox");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_config/2");
    }

    @Test
    void testDeleteConfigGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteConfigGroup(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_config_group/1");
    }

    @Test
    void testDeleteConfig() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteConfig(2);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_config/2");
    }

    // ==================== Case Fields Tests ====================

    @Test
    void testGetCaseFields() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"custom_field\",\"type_id\":1,\"label\":\"Custom Field\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] fields = apiClient.getCaseFields();

        assertThat(fields).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_case_fields");
    }

    @Test
    void testAddCaseField() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"custom_field\",\"type_id\":1,\"label\":\"Custom Field\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        io.github.testrail.mcp.model.CaseField field = apiClient.addCaseField(Map.of("name", "custom_field", "type_id", 1));

        assertThat(field.getId()).isEqualTo(1);
        assertThat(field.getName()).isEqualTo("custom_field");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_case_field");
    }

    // ==================== Case Types Tests ====================

    @Test
    void testGetCaseTypes() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Functional\",\"is_default\":true},{\"id\":2,\"name\":\"Performance\",\"is_default\":false}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] types = apiClient.getCaseTypes();

        assertThat(types).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_case_types");
    }

    // ==================== Priorities Tests ====================

    @Test
    void testGetPriorities() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Critical\",\"short_name\":\"Crit\",\"priority\":1},{\"id\":2,\"name\":\"High\",\"short_name\":\"High\",\"priority\":2}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] priorities = apiClient.getPriorities();

        assertThat(priorities).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_priorities");
    }

    // ==================== Statuses Tests ====================

    @Test
    void testGetStatuses() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"passed\",\"label\":\"Passed\",\"is_final\":true},{\"id\":2,\"name\":\"failed\",\"label\":\"Failed\",\"is_final\":true}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] statuses = apiClient.getStatuses();

        assertThat(statuses).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_statuses");
    }

    // ==================== Templates Tests ====================

    @Test
    void testGetTemplates() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Test Case (Text)\",\"is_default\":true},{\"id\":2,\"name\":\"Test Case (Steps)\",\"is_default\":false}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] templates = apiClient.getTemplates(1);

        assertThat(templates).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_templates/1");
    }

    // ==================== Result Fields Tests ====================

    @Test
    void testGetResultFields() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"custom_result_field\",\"type_id\":1,\"label\":\"Custom Result Field\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Object[] fields = apiClient.getResultFields();

        assertThat(fields).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_result_fields");
    }

    // ==================== Attachments Tests ====================

    @Test
    void testGetAttachmentsForCase() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"attachments\":[{\"id\":1,\"name\":\"test.jpg\",\"size\":1024}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForCase(123, null, null);

        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getName()).isEqualTo("test.jpg");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_case/123");
    }

    @Test
    void testGetAttachmentsForCaseWithPagination() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"attachments\":[{\"id\":1,\"name\":\"test.jpg\",\"size\":1024}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForCase(123, 10, 5);

        assertThat(attachments).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_case/123?limit=10&offset=5");
    }

    @Test
    void testGetAttachmentsForPlan() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"plan_attachment.pdf\",\"size\":2048}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForPlan(456, null, null);

        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).getName()).isEqualTo("plan_attachment.pdf");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_plan/456");
    }

    @Test
    void testGetAttachmentsForPlanEntry() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"entry_file.txt\",\"size\":512}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForPlanEntry(789, "abc-123");

        assertThat(attachments).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_plan_entry/789/abc-123");
    }

    @Test
    void testGetAttachmentsForRun() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"attachments\":[{\"id\":1,\"name\":\"run_log.txt\",\"size\":4096}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForRun(111, null, null);

        assertThat(attachments).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_run/111");
    }

    @Test
    void testGetAttachmentsForTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"attachments\":[{\"id\":1,\"name\":\"test_screenshot.png\",\"size\":8192}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Attachment> attachments = apiClient.getAttachmentsForTest(222);

        assertThat(attachments).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachments_for_test/222");
    }

    @Test
    void testGetAttachment() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"uuid-123\",\"name\":\"document.pdf\",\"size\":16384}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Attachment attachment = apiClient.getAttachment("uuid-123");

        assertThat(attachment.getName()).isEqualTo("document.pdf");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_attachment/uuid-123");
    }

    @Test
    void testDeleteAttachment() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteAttachment("uuid-456");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_attachment/uuid-456");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    // ==================== Shared Steps Tests ====================

    @Test
    void testGetSharedStep() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"title\":\"Login Steps\",\"project_id\":10}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        SharedStep sharedStep = apiClient.getSharedStep(1);

        assertThat(sharedStep.getId()).isEqualTo(1);
        assertThat(sharedStep.getTitle()).isEqualTo("Login Steps");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_shared_step/1");
    }

    @Test
    void testGetSharedStepHistory() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"shared_step_id\":1,\"version_id\":1,\"title\":\"Login Steps v1\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<SharedStepHistory> history = apiClient.getSharedStepHistory(1);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getSharedStepId()).isEqualTo(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_shared_step_history/1");
    }

    @Test
    void testGetSharedSteps() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"title\":\"Login Steps\"},{\"id\":2,\"title\":\"Logout Steps\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<SharedStep> sharedSteps = apiClient.getSharedSteps(10);

        assertThat(sharedSteps).hasSize(2);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_shared_steps/10");
    }

    @Test
    void testAddSharedStep() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"title\":\"New Shared Step\",\"project_id\":10}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("title", "New Shared Step");
        SharedStep sharedStep = apiClient.addSharedStep(10, data);

        assertThat(sharedStep.getId()).isEqualTo(3);
        assertThat(sharedStep.getTitle()).isEqualTo("New Shared Step");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_shared_step/10");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void testUpdateSharedStep() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"title\":\"Updated Login Steps\",\"project_id\":10}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Updated Login Steps");
        SharedStep sharedStep = apiClient.updateSharedStep(1, data);

        assertThat(sharedStep.getTitle()).isEqualTo("Updated Login Steps");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_shared_step/1");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void testDeleteSharedStep() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteSharedStep(1);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_shared_step/1");
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    // ==================== Reports Tests ====================

    @Test
    void testGetReports() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Test Summary Report\",\"report_template_id\":100}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Report> reports = apiClient.getReports(10);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getName()).isEqualTo("Test Summary Report");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_reports/10");
    }

    @Test
    void testRunReport() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"report_data\":{\"total_tests\":100,\"passed\":85}}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        JsonNode reportData = apiClient.runReport(100);

        assertThat(reportData).isNotNull();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/run_report/100");
    }

    @Test
    void testGetCrossProjectReports() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[{\"id\":1,\"name\":\"Cross-Project Summary\",\"is_cross_project\":true}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Report> reports = apiClient.getCrossProjectReports();

        assertThat(reports).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_cross_project_reports/");
    }

    @Test
    void testRunCrossProjectReport() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"report_data\":{\"total_projects\":5,\"total_tests\":500}}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        JsonNode reportData = apiClient.runCrossProjectReport(200);

        assertThat(reportData).isNotNull();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/run_cross_project_report/200");
    }

    // ==================== Roles Tests ====================

    @Test
    void testGetRoles() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"roles\":[{\"id\":1,\"name\":\"Tester\",\"is_default\":false},{\"id\":2,\"name\":\"Lead\",\"is_default\":true}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Role> roles = apiClient.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles.get(0).getName()).isEqualTo("Tester");
        assertThat(roles.get(1).getIsDefault()).isTrue();
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_roles");
    }

    // ==================== Phase 5 Tests ====================

    @Test
    void testGetBdd() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("@APP-1\nFeature: Login\nScenario: Valid login\nGiven user is on login page\nWhen user enters credentials\nThen user is logged in")
                .addHeader(HttpHeaders.CONTENT_TYPE, "text/plain"));

        String bdd = apiClient.getBdd(123);

        assertThat(bdd).contains("@APP-1");
        assertThat(bdd).contains("Feature: Login");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_bdd/123");
    }

    @Test
    void testAddBdd() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":456,\"title\":\"Login Test\",\"section_id\":789}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        TestCase testCase = apiClient.addBdd(789, "Feature: Login");

        assertThat(testCase.getId()).isEqualTo(456);
        assertThat(testCase.getTitle()).isEqualTo("Login Test");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_bdd/789");
    }

    @Test
    void testGetDataset() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":183,\"name\":\"Default\",\"variables\":[{\"id\":1,\"name\":\"username\",\"value\":\"admin\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Dataset dataset = apiClient.getDataset(183);

        assertThat(dataset.getId()).isEqualTo(183);
        assertThat(dataset.getName()).isEqualTo("Default");
        assertThat(dataset.getVariables()).hasSize(1);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_dataset/183");
    }

    @Test
    void testGetDatasets() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"datasets\":[{\"id\":1,\"name\":\"Dataset1\"},{\"id\":2,\"name\":\"Dataset2\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Dataset> datasets = apiClient.getDatasets(1);

        assertThat(datasets).hasSize(2);
        assertThat(datasets.get(0).getName()).isEqualTo("Dataset1");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_datasets/1");
    }

    @Test
    void testAddDataset() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"New Dataset\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> datasetData = Map.of("name", "New Dataset");
        Dataset dataset = apiClient.addDataset(1, datasetData);

        assertThat(dataset.getId()).isEqualTo(3);
        assertThat(dataset.getName()).isEqualTo("New Dataset");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_dataset/1");
    }

    @Test
    void testUpdateDataset() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"Updated Dataset\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> datasetData = Map.of("name", "Updated Dataset");
        Dataset dataset = apiClient.updateDataset(3, datasetData);

        assertThat(dataset.getName()).isEqualTo("Updated Dataset");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_dataset/3");
    }

    @Test
    void testDeleteDataset() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteDataset(3);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_dataset/3");
    }

    @Test
    void testGetGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Developers\",\"user_ids\":[1,2,3]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Group group = apiClient.getGroup(1);

        assertThat(group.getId()).isEqualTo(1);
        assertThat(group.getName()).isEqualTo("Developers");
        assertThat(group.getUserIds()).hasSize(3);
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_group/1");
    }

    @Test
    void testGetGroups() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"groups\":[{\"id\":1,\"name\":\"Group1\"},{\"id\":2,\"name\":\"Group2\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Group> groups = apiClient.getGroups();

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).getName()).isEqualTo("Group1");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_groups");
    }

    @Test
    void testAddGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"New Group\",\"user_ids\":[1,2]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> groupData = Map.of("name", "New Group", "user_ids", List.of(1, 2));
        Group group = apiClient.addGroup(groupData);

        assertThat(group.getId()).isEqualTo(3);
        assertThat(group.getName()).isEqualTo("New Group");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_group");
    }

    @Test
    void testUpdateGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"Updated Group\",\"user_ids\":[1,2,3]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> groupData = Map.of("name", "Updated Group", "user_ids", List.of(1, 2, 3));
        Group group = apiClient.updateGroup(3, groupData);

        assertThat(group.getName()).isEqualTo("Updated Group");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_group/3");
    }

    @Test
    void testDeleteGroup() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteGroup(3);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_group/3");
    }

    @Test
    void testGetLabel() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"name\":\"Release 2.0\",\"title\":\"Release 2.0\",\"created_by\":1,\"created_on\":1234567890}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Label label = apiClient.getLabel(1);

        assertThat(label.getId()).isEqualTo(1);
        assertThat(label.getName()).isEqualTo("Release 2.0");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_label/1");
    }

    @Test
    void testGetLabels() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"labels\":[{\"id\":1,\"title\":\"Label1\"},{\"id\":2,\"title\":\"Label2\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Label> labels = apiClient.getLabels(1, 10, 0);

        assertThat(labels).hasSize(2);
        assertThat(labels.get(0).getTitle()).isEqualTo("Label1");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).contains("/get_labels/1");
    }

    @Test
    void testUpdateLabel() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":1,\"title\":\"Updated Label\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> labelData = Map.of("title", "Updated Label");
        Label label = apiClient.updateLabel(1, labelData);

        assertThat(label.getTitle()).isEqualTo("Updated Label");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_label/1");
    }

    @Test
    void testGetVariables() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"variables\":[{\"id\":1,\"name\":\"username\"},{\"id\":2,\"name\":\"password\"}]}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<Variable> variables = apiClient.getVariables(1);

        assertThat(variables).hasSize(2);
        assertThat(variables.get(0).getName()).isEqualTo("username");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/get_variables/1");
    }

    @Test
    void testAddVariable() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"email\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> variableData = Map.of("name", "email");
        Variable variable = apiClient.addVariable(1, variableData);

        assertThat(variable.getId()).isEqualTo(3);
        assertThat(variable.getName()).isEqualTo("email");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/add_variable/1");
    }

    @Test
    void testUpdateVariable() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":3,\"name\":\"user_email\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Map<String, Object> variableData = Map.of("name", "user_email");
        Variable variable = apiClient.updateVariable(3, variableData);

        assertThat(variable.getName()).isEqualTo("user_email");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/update_variable/3");
    }

    @Test
    void testDeleteVariable() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        apiClient.deleteVariable(3);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/delete_variable/3");
    }
}
