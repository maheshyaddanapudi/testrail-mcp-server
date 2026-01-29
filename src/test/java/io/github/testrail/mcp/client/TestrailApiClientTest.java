package io.github.testrail.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.model.*;
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
}
