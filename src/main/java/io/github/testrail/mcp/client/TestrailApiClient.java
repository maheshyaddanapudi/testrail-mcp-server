package io.github.testrail.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.testrail.mcp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with the TestRail API.
 *
 * <p>This client provides methods for all TestRail API operations including
 * test cases, projects, runs, results, and sections.</p>
 */
@Component
public class TestrailApiClient {

    private static final Logger log = LoggerFactory.getLogger(TestrailApiClient.class);

    private final WebClient webClient;

    public TestrailApiClient(WebClient testrailWebClient) {
        this.webClient = testrailWebClient;
    }

    // ==================== Cases API ====================

    /**
     * Gets a test case by ID.
     *
     * @param caseId the test case ID
     * @return the test case
     */
    public TestCase getCase(Integer caseId) {
        log.debug("Getting test case: {}", caseId);
        return get("get_case/" + caseId, TestCase.class);
    }

    /**
     * Gets test cases for a project with optional filters.
     *
     * @param projectId the project ID
     * @param suiteId optional suite ID
     * @param sectionId optional section ID
     * @param limit maximum results (1-250)
     * @param offset pagination offset
     * @return list of test cases
     */
    public List<TestCase> getCases(Integer projectId, Integer suiteId, Integer sectionId,
                                    Integer limit, Integer offset) {
        log.debug("Getting test cases for project: {}", projectId);

        StringBuilder uri = new StringBuilder("get_cases/" + projectId);
        String separator = "?";

        if (suiteId != null) {
            uri.append(separator).append("suite_id=").append(suiteId);
            separator = "&";
        }
        if (sectionId != null) {
            uri.append(separator).append("section_id=").append(sectionId);
            separator = "&";
        }
        if (limit != null) {
            uri.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uri.append(separator).append("offset=").append(offset);
        }

        JsonNode response = get(uri.toString(), JsonNode.class);
        return extractList(response, "cases", TestCase.class);
    }

    /**
     * Adds a new test case.
     *
     * @param sectionId the section ID
     * @param data the test case data
     * @return the created test case
     */
    public TestCase addCase(Integer sectionId, Map<String, Object> data) {
        log.info("Adding test case to section: {}", sectionId);
        return post("add_case/" + sectionId, data, TestCase.class);
    }

    /**
     * Updates an existing test case.
     *
     * @param caseId the test case ID
     * @param data the update data
     * @return the updated test case
     */
    public TestCase updateCase(Integer caseId, Map<String, Object> data) {
        log.info("Updating test case: {}", caseId);
        return post("update_case/" + caseId, data, TestCase.class);
    }

    /**
     * Deletes a test case.
     *
     * @param caseId the test case ID
     */
    public void deleteCase(Integer caseId) {
        log.warn("Deleting test case: {}", caseId);
        post("delete_case/" + caseId, null, Void.class);
    }

    // ==================== Projects API ====================

    /**
     * Gets a project by ID.
     *
     * @param projectId the project ID
     * @return the project
     */
    public Project getProject(Integer projectId) {
        log.debug("Getting project: {}", projectId);
        return get("get_project/" + projectId, Project.class);
    }

    /**
     * Gets all projects.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        log.debug("Getting all projects");
        JsonNode response = get("get_projects", JsonNode.class);
        return extractList(response, "projects", Project.class);
    }

    /**
     * Adds a new project.
     *
     * @param data the project data
     * @return the created project
     */
    public Project addProject(Map<String, Object> data) {
        log.info("Adding project: {}", data.get("name"));
        return post("add_project", data, Project.class);
    }

    /**
     * Updates an existing project.
     *
     * @param projectId the project ID
     * @param data the update data
     * @return the updated project
     */
    public Project updateProject(Integer projectId, Map<String, Object> data) {
        log.info("Updating project: {}", projectId);
        return post("update_project/" + projectId, data, Project.class);
    }

    /**
     * Deletes a project.
     *
     * @param projectId the project ID
     */
    public void deleteProject(Integer projectId) {
        log.warn("Deleting project: {}", projectId);
        post("delete_project/" + projectId, null, Void.class);
    }

    // ==================== Runs API ====================

    /**
     * Gets a test run by ID.
     *
     * @param runId the run ID
     * @return the test run
     */
    public TestRun getRun(Integer runId) {
        log.debug("Getting run: {}", runId);
        return get("get_run/" + runId, TestRun.class);
    }

    /**
     * Gets test runs for a project.
     *
     * @param projectId the project ID
     * @param limit maximum results
     * @param offset pagination offset
     * @return list of test runs
     */
    public List<TestRun> getRuns(Integer projectId, Integer limit, Integer offset) {
        log.debug("Getting runs for project: {}", projectId);

        StringBuilder uri = new StringBuilder("get_runs/" + projectId);
        String separator = "?";

        if (limit != null) {
            uri.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uri.append(separator).append("offset=").append(offset);
        }

        JsonNode response = get(uri.toString(), JsonNode.class);
        return extractList(response, "runs", TestRun.class);
    }

    /**
     * Adds a new test run.
     *
     * @param projectId the project ID
     * @param data the run data
     * @return the created test run
     */
    public TestRun addRun(Integer projectId, Map<String, Object> data) {
        log.info("Adding run to project: {}", projectId);
        return post("add_run/" + projectId, data, TestRun.class);
    }

    /**
     * Updates an existing test run.
     *
     * @param runId the run ID
     * @param data the update data
     * @return the updated test run
     */
    public TestRun updateRun(Integer runId, Map<String, Object> data) {
        log.info("Updating run: {}", runId);
        return post("update_run/" + runId, data, TestRun.class);
    }

    /**
     * Closes a test run.
     *
     * @param runId the run ID
     * @return the closed test run
     */
    public TestRun closeRun(Integer runId) {
        log.info("Closing run: {}", runId);
        return post("close_run/" + runId, null, TestRun.class);
    }

    /**
     * Deletes a test run.
     *
     * @param runId the run ID
     */
    public void deleteRun(Integer runId) {
        log.warn("Deleting run: {}", runId);
        post("delete_run/" + runId, null, Void.class);
    }

    // ==================== Results API ====================

    /**
     * Gets results for a test.
     *
     * @param testId the test ID
     * @param limit maximum results
     * @param offset pagination offset
     * @return list of results
     */
    public List<TestResult> getResults(Integer testId, Integer limit, Integer offset) {
        log.debug("Getting results for test: {}", testId);

        StringBuilder uri = new StringBuilder("get_results/" + testId);
        String separator = "?";

        if (limit != null) {
            uri.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uri.append(separator).append("offset=").append(offset);
        }

        JsonNode response = get(uri.toString(), JsonNode.class);
        return extractList(response, "results", TestResult.class);
    }

    /**
     * Gets all results for a run.
     *
     * @param runId the run ID
     * @param limit maximum results
     * @param offset pagination offset
     * @return list of results
     */
    public List<TestResult> getResultsForRun(Integer runId, Integer limit, Integer offset) {
        log.debug("Getting results for run: {}", runId);

        StringBuilder uri = new StringBuilder("get_results_for_run/" + runId);
        String separator = "?";

        if (limit != null) {
            uri.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uri.append(separator).append("offset=").append(offset);
        }

        JsonNode response = get(uri.toString(), JsonNode.class);
        return extractList(response, "results", TestResult.class);
    }

    /**
     * Adds a result for a test.
     *
     * @param testId the test ID
     * @param data the result data
     * @return the created result
     */
    public TestResult addResult(Integer testId, Map<String, Object> data) {
        log.info("Adding result for test: {}", testId);
        return post("add_result/" + testId, data, TestResult.class);
    }

    /**
     * Adds multiple results for a run.
     *
     * @param runId the run ID
     * @param results the results data
     * @return list of created results
     */
    public List<TestResult> addResults(Integer runId, List<Map<String, Object>> results) {
        log.info("Adding {} results for run: {}", results.size(), runId);
        Map<String, Object> data = new HashMap<>();
        data.put("results", results);
        JsonNode response = post("add_results/" + runId, data, JsonNode.class);
        return extractListDirect(response, TestResult.class);
    }

    /**
     * Adds results for cases in a run.
     *
     * @param runId the run ID
     * @param results the results data with case_id fields
     * @return list of created results
     */
    public List<TestResult> addResultsForCases(Integer runId, List<Map<String, Object>> results) {
        log.info("Adding {} results for cases in run: {}", results.size(), runId);
        Map<String, Object> data = new HashMap<>();
        data.put("results", results);
        JsonNode response = post("add_results_for_cases/" + runId, data, JsonNode.class);
        return extractListDirect(response, TestResult.class);
    }

    // ==================== Sections API ====================

    /**
     * Gets a section by ID.
     *
     * @param sectionId the section ID
     * @return the section
     */
    public Section getSection(Integer sectionId) {
        log.debug("Getting section: {}", sectionId);
        return get("get_section/" + sectionId, Section.class);
    }

    /**
     * Gets sections for a project.
     *
     * @param projectId the project ID
     * @param suiteId optional suite ID
     * @param limit maximum results
     * @param offset pagination offset
     * @return list of sections
     */
    public List<Section> getSections(Integer projectId, Integer suiteId, Integer limit, Integer offset) {
        log.debug("Getting sections for project: {}", projectId);

        StringBuilder uri = new StringBuilder("get_sections/" + projectId);
        String separator = "?";

        if (suiteId != null) {
            uri.append(separator).append("suite_id=").append(suiteId);
            separator = "&";
        }
        if (limit != null) {
            uri.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uri.append(separator).append("offset=").append(offset);
        }

        JsonNode response = get(uri.toString(), JsonNode.class);
        return extractList(response, "sections", Section.class);
    }

    /**
     * Adds a new section.
     *
     * @param projectId the project ID
     * @param data the section data
     * @return the created section
     */
    public Section addSection(Integer projectId, Map<String, Object> data) {
        log.info("Adding section to project: {}", projectId);
        return post("add_section/" + projectId, data, Section.class);
    }

    /**
     * Updates an existing section.
     *
     * @param sectionId the section ID
     * @param data the update data
     * @return the updated section
     */
    public Section updateSection(Integer sectionId, Map<String, Object> data) {
        log.info("Updating section: {}", sectionId);
        return post("update_section/" + sectionId, data, Section.class);
    }

    /**
     * Deletes a section.
     *
     * @param sectionId the section ID
     * @param soft whether to perform a soft delete
     */
    public void deleteSection(Integer sectionId, boolean soft) {
        log.warn("Deleting section: {} (soft={})", sectionId, soft);
        String uri = "delete_section/" + sectionId;
        if (soft) {
            uri += "?soft=1";
        }
        post(uri, null, Void.class);
    }

    /**
     * Moves a section to a new location.
     *
     * @param sectionId the section ID
     * @param data the move data (parent_id, after_id)
     * @return the moved section
     */
    public Section moveSection(Integer sectionId, Map<String, Object> data) {
        log.info("Moving section: {}", sectionId);
        return post("move_section/" + sectionId, data, Section.class);
    }

    // ==================== Helper Methods ====================

    private <T> T get(String uri, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new TestrailApiException(
                                            "TestRail API error: " + body,
                                            response.statusCode().value(),
                                            body))))
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new TestrailApiException(
                    "TestRail API returned HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    e);
        } catch (TestrailApiException e) {
            throw e;
        } catch (Exception e) {
            throw new TestrailApiException("Failed to call TestRail API: " + e.getMessage(), e);
        }
    }

    private <T> T post(String uri, Object data, Class<T> responseType) {
        try {
            var requestSpec = webClient.post().uri(uri);

            if (data != null) {
                requestSpec = requestSpec.bodyValue(data);
            }

            if (responseType == Void.class) {
                requestSpec.retrieve()
                        .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(new TestrailApiException(
                                                "TestRail API error: " + body,
                                                response.statusCode().value(),
                                                body))))
                        .toBodilessEntity()
                        .block();
                return null;
            }

            return requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new TestrailApiException(
                                            "TestRail API error: " + body,
                                            response.statusCode().value(),
                                            body))))
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new TestrailApiException(
                    "TestRail API returned HTTP " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    e);
        } catch (TestrailApiException e) {
            throw e;
        } catch (Exception e) {
            throw new TestrailApiException("Failed to call TestRail API: " + e.getMessage(), e);
        }
    }

    private <T> List<T> extractList(JsonNode response, String fieldName, Class<T> elementType) {
        if (response == null) {
            return List.of();
        }

        JsonNode arrayNode = response.has(fieldName) ? response.get(fieldName) : response;

        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readerForListOf(elementType).readValue(arrayNode);
        } catch (Exception e) {
            log.error("Failed to parse list response", e);
            return List.of();
        }
    }

    private <T> List<T> extractListDirect(JsonNode response, Class<T> elementType) {
        if (response == null || !response.isArray()) {
            return List.of();
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readerForListOf(elementType).readValue(response);
        } catch (Exception e) {
            log.error("Failed to parse list response", e);
            return List.of();
        }
    }
}
