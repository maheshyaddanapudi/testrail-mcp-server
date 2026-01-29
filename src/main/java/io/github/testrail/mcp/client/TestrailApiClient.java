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
     * Gets all projects with optional filters.
     *
     * @param isCompleted filter by completion status (true for completed, false for active, null for all)
     * @param limit maximum results (1-250)
     * @param offset pagination offset
     * @return list of projects
     */
    public List<Project> getProjects(Boolean isCompleted, Integer limit, Integer offset) {
        log.debug("Getting all projects");

        StringBuilder uri = new StringBuilder("get_projects");
        String separator = "?";

        if (isCompleted != null) {
            uri.append(separator).append("is_completed=").append(isCompleted ? 1 : 0);
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
     * Gets test runs for a project with optional filters.
     *
     * @param projectId the project ID
     * @param isCompleted filter by completion status (true for completed, false for active, null for all)
     * @param createdAfter only return runs created after this timestamp
     * @param createdBefore only return runs created before this timestamp
     * @param createdBy comma-separated list of creator user IDs
     * @param milestoneId comma-separated list of milestone IDs
     * @param suiteId comma-separated list of suite IDs
     * @param limit maximum results (1-250)
     * @param offset pagination offset
     * @return list of test runs
     */
    public List<TestRun> getRuns(Integer projectId, Boolean isCompleted, Long createdAfter,
                                  Long createdBefore, String createdBy, String milestoneId,
                                  String suiteId, Integer limit, Integer offset) {
        log.debug("Getting runs for project: {}", projectId);

        StringBuilder uri = new StringBuilder("get_runs/" + projectId);
        String separator = "?";

        if (isCompleted != null) {
            uri.append(separator).append("is_completed=").append(isCompleted ? 1 : 0);
            separator = "&";
        }
        if (createdAfter != null) {
            uri.append(separator).append("created_after=").append(createdAfter);
            separator = "&";
        }
        if (createdBefore != null) {
            uri.append(separator).append("created_before=").append(createdBefore);
            separator = "&";
        }
        if (createdBy != null) {
            uri.append(separator).append("created_by=").append(createdBy);
            separator = "&";
        }
        if (milestoneId != null) {
            uri.append(separator).append("milestone_id=").append(milestoneId);
            separator = "&";
        }
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
     * Gets results for a test with optional filters.
     *
     * @param testId the test ID
     * @param defectsFilter single Defect ID (e.g. TR-1, 4291, etc.)
     * @param statusId comma-separated list of status IDs to filter by
     * @param limit maximum results (1-250)
     * @param offset pagination offset
     * @return list of results
     */
    public List<TestResult> getResults(Integer testId, String defectsFilter, String statusId,
                                        Integer limit, Integer offset) {
        log.debug("Getting results for test: {}", testId);

        StringBuilder uri = new StringBuilder("get_results/" + testId);
        String separator = "?";

        if (defectsFilter != null) {
            uri.append(separator).append("defects_filter=").append(defectsFilter);
            separator = "&";
        }
        if (statusId != null) {
            uri.append(separator).append("status_id=").append(statusId);
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
        return extractList(response, "results", TestResult.class);
    }

    /**
     * Gets all results for a run with optional filters.
     *
     * @param runId the run ID
     * @param createdAfter only return results created after this timestamp
     * @param createdBefore only return results created before this timestamp
     * @param createdBy comma-separated list of creator user IDs
     * @param defectsFilter single Defect ID (e.g. TR-1, 4291, etc.)
     * @param statusId comma-separated list of status IDs to filter by
     * @param limit maximum results (1-250)
     * @param offset pagination offset
     * @return list of results
     */
    public List<TestResult> getResultsForRun(Integer runId, Long createdAfter, Long createdBefore,
                                              String createdBy, String defectsFilter, String statusId,
                                              Integer limit, Integer offset) {
        log.debug("Getting results for run: {}", runId);

        StringBuilder uri = new StringBuilder("get_results_for_run/" + runId);
        String separator = "?";

        if (createdAfter != null) {
            uri.append(separator).append("created_after=").append(createdAfter);
            separator = "&";
        }
        if (createdBefore != null) {
            uri.append(separator).append("created_before=").append(createdBefore);
            separator = "&";
        }
        if (createdBy != null) {
            uri.append(separator).append("created_by=").append(createdBy);
            separator = "&";
        }
        if (defectsFilter != null) {
            uri.append(separator).append("defects_filter=").append(defectsFilter);
            separator = "&";
        }
        if (statusId != null) {
            uri.append(separator).append("status_id=").append(statusId);
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

    // ==================== Plans API ====================

    /**
     * Gets a test plan by ID.
     *
     * @param planId the plan ID
     * @return the test plan
     */
    public TestPlan getPlan(Integer planId) {
        log.debug("Getting test plan: {}", planId);
        return get("get_plan/" + planId, TestPlan.class);
    }

    /**
     * Gets test plans for a project with optional filters.
     *
     * @param projectId the project ID
     * @param createdAfter optional timestamp - only return plans created after this date
     * @param createdBefore optional timestamp - only return plans created before this date
     * @param createdBy optional comma-separated list of creator user IDs
     * @param isCompleted optional - 1 for completed only, 0 for active only
     * @param milestoneId optional comma-separated list of milestone IDs
     * @param limit maximum results (default 250)
     * @param offset pagination offset
     * @return list of test plans
     */
    public List<TestPlan> getPlans(Integer projectId, Long createdAfter, Long createdBefore,
                                    String createdBy, Integer isCompleted, String milestoneId,
                                    Integer limit, Integer offset) {
        log.debug("Getting test plans for project: {}", projectId);

        StringBuilder uri = new StringBuilder("get_plans/" + projectId);
        String separator = "?";

        if (createdAfter != null) {
            uri.append(separator).append("created_after=").append(createdAfter);
            separator = "&";
        }
        if (createdBefore != null) {
            uri.append(separator).append("created_before=").append(createdBefore);
            separator = "&";
        }
        if (createdBy != null && !createdBy.isEmpty()) {
            uri.append(separator).append("created_by=").append(createdBy);
            separator = "&";
        }
        if (isCompleted != null) {
            uri.append(separator).append("is_completed=").append(isCompleted);
            separator = "&";
        }
        if (milestoneId != null && !milestoneId.isEmpty()) {
            uri.append(separator).append("milestone_id=").append(milestoneId);
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
        return extractList(response, "plans", TestPlan.class);
    }

    /**
     * Adds a new test plan.
     *
     * @param projectId the project ID
     * @param data the plan data
     * @return the created test plan
     */
    public TestPlan addPlan(Integer projectId, Map<String, Object> data) {
        log.info("Adding test plan to project: {}", projectId);
        return post("add_plan/" + projectId, data, TestPlan.class);
    }

    /**
     * Updates an existing test plan.
     *
     * @param planId the plan ID
     * @param data the update data
     * @return the updated test plan
     */
    public TestPlan updatePlan(Integer planId, Map<String, Object> data) {
        log.info("Updating test plan: {}", planId);
        return post("update_plan/" + planId, data, TestPlan.class);
    }

    /**
     * Closes a test plan.
     *
     * @param planId the plan ID
     * @return the closed test plan
     */
    public TestPlan closePlan(Integer planId) {
        log.info("Closing test plan: {}", planId);
        return post("close_plan/" + planId, null, TestPlan.class);
    }

    /**
     * Deletes a test plan.
     *
     * @param planId the plan ID
     */
    public void deletePlan(Integer planId) {
        log.info("Deleting test plan: {}", planId);
        post("delete_plan/" + planId, null, Void.class);
    }

    // ==================== Users API ====================

    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the user
     */
    public User getUser(Integer userId) {
        log.debug("Getting user: {}", userId);
        return get("get_user/" + userId, User.class);
    }

    /**
     * Gets the current authenticated user.
     *
     * @return the current user
     */
    public User getCurrentUser() {
        log.debug("Getting current user");
        return get("get_current_user", User.class);
    }

    /**
     * Gets a user by email address.
     *
     * @param email the email address
     * @return the user
     */
    public User getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return get("get_user_by_email&email=" + email, User.class);
    }

    /**
     * Gets all users or users for a specific project.
     *
     * @param projectId optional project ID (required for non-admins)
     * @return list of users
     */
    public Object[] getUsers(Integer projectId) {
        log.debug("Getting users for project: {}", projectId);
        String uri = projectId != null ? "get_users/" + projectId : "get_users";
        // get_users returns a direct array, not wrapped in a field
        JsonNode response = get(uri, JsonNode.class);
        return extractListDirect(response, User.class).toArray();
    }

    // ==================== Suites API ====================

    /**
     * Gets a suite by ID.
     *
     * @param suiteId the suite ID
     * @return the suite
     */
    public Suite getSuite(Integer suiteId) {
        log.debug("Getting suite: {}", suiteId);
        return get("get_suite/" + suiteId, Suite.class);
    }

    /**
     * Gets suites for a project.
     *
     * @param projectId the project ID
     * @return list of suites
     */
    public Object[] getSuites(Integer projectId) {
        log.debug("Getting suites for project: {}", projectId);
        JsonNode response = get("get_suites/" + projectId, JsonNode.class);
        return extractList(response, "suites", Suite.class).toArray();
    }

    /**
     * Adds a new suite to a project.
     *
     * @param projectId the project ID
     * @param suite the suite data
     * @return the created suite
     */
    public Suite addSuite(Integer projectId, Map<String, Object> suite) {
        log.info("Adding suite to project: {}", projectId);
        return post("add_suite/" + projectId, suite, Suite.class);
    }

    /**
     * Updates a suite.
     *
     * @param suiteId the suite ID
     * @param suite the suite data
     * @return the updated suite
     */
    public Suite updateSuite(Integer suiteId, Map<String, Object> suite) {
        log.info("Updating suite: {}", suiteId);
        return post("update_suite/" + suiteId, suite, Suite.class);
    }

    /**
     * Deletes a suite.
     *
     * @param suiteId the suite ID
     * @param soft optional soft delete parameter (1 to preview, 0 or null to delete)
     */
    public void deleteSuite(Integer suiteId, Integer soft) {
        log.info("Deleting suite: {}", suiteId);
        String uri = "delete_suite/" + suiteId;
        if (soft != null) {
            uri += "?soft=" + soft;
        }
        post(uri, null, Void.class);
    }

    // ==================== Milestones API ====================

    /**
     * Gets a milestone by ID.
     *
     * @param milestoneId the milestone ID
     * @return the milestone
     */
    public Milestone getMilestone(Integer milestoneId) {
        log.debug("Getting milestone: {}", milestoneId);
        return get("get_milestone/" + milestoneId, Milestone.class);
    }

    /**
     * Gets milestones for a project with optional filters.
     *
     * @param projectId the project ID
     * @param isCompleted optional filter for completion status
     * @param isStarted optional filter for started status
     * @param limit maximum results (default 250)
     * @param offset pagination offset
     * @return list of milestones
     */
    public Object[] getMilestones(Integer projectId, Boolean isCompleted, Boolean isStarted, Integer limit, Integer offset) {
        log.debug("Getting milestones for project: {}", projectId);
        
        StringBuilder uri = new StringBuilder("get_milestones/" + projectId);
        String separator = "?";
        
        if (isCompleted != null) {
            uri.append(separator).append("is_completed=").append(isCompleted ? 1 : 0);
            separator = "&";
        }
        if (isStarted != null) {
            uri.append(separator).append("is_started=").append(isStarted ? 1 : 0);
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
        return extractList(response, "milestones", Milestone.class).toArray();
    }

    /**
     * Adds a new milestone to a project.
     *
     * @param projectId the project ID
     * @param milestone the milestone data
     * @return the created milestone
     */
    public Milestone addMilestone(Integer projectId, Map<String, Object> milestone) {
        log.info("Adding milestone to project: {}", projectId);
        return post("add_milestone/" + projectId, milestone, Milestone.class);
    }

    /**
     * Updates a milestone.
     *
     * @param milestoneId the milestone ID
     * @param milestone the milestone data
     * @return the updated milestone
     */
    public Milestone updateMilestone(Integer milestoneId, Map<String, Object> milestone) {
        log.info("Updating milestone: {}", milestoneId);
        return post("update_milestone/" + milestoneId, milestone, Milestone.class);
    }

    /**
     * Deletes a milestone.
     *
     * @param milestoneId the milestone ID
     */
    public void deleteMilestone(Integer milestoneId) {
        log.info("Deleting milestone: {}", milestoneId);
        post("delete_milestone/" + milestoneId, null, Void.class);
    }

    // ==================== Tests API ====================

    /**
     * Gets a test by ID.
     *
     * @param testId the test ID
     * @param withData optional parameter to get data
     * @return the test
     */
    public Test getTest(Integer testId, String withData) {
        log.debug("Getting test: {}", testId);
        
        StringBuilder uri = new StringBuilder("get_test/" + testId);
        if (withData != null && !withData.isEmpty()) {
            uri.append("?with_data=").append(withData);
        }
        
        return get(uri.toString(), Test.class);
    }

    /**
     * Gets tests for a test run with optional filters.
     *
     * @param runId the test run ID
     * @param statusId optional comma-separated list of status IDs
     * @param labelId optional comma-separated list of label IDs
     * @param limit maximum results (default 250)
     * @param offset pagination offset
     * @return list of tests
     */
    public List<Test> getTests(Integer runId, String statusId, String labelId,
                                Integer limit, Integer offset) {
        log.debug("Getting tests for run: {}", runId);

        StringBuilder uri = new StringBuilder("get_tests/" + runId);
        String separator = "?";

        if (statusId != null && !statusId.isEmpty()) {
            uri.append(separator).append("status_id=").append(statusId);
            separator = "&";
        }
        if (labelId != null && !labelId.isEmpty()) {
            uri.append(separator).append("label_id=").append(labelId);
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
        return extractList(response, "tests", Test.class);
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
            WebClient.RequestBodySpec requestBodySpec = webClient.post().uri(uri);
            WebClient.RequestHeadersSpec<?> requestSpec = data != null
                    ? requestBodySpec.bodyValue(data)
                    : requestBodySpec;

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
