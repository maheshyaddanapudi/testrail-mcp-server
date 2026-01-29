package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Attachment;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools for managing TestRail attachments.
 */
@Component
public class AttachmentsTools {

    private final TestrailApiClient apiClient;
    private final ObjectMapper objectMapper;

    public AttachmentsTools(TestrailApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Get all attachments for a test case. Supports optional limit and offset for pagination.")
    public String getAttachmentsForCase(int caseId, Integer limit, Integer offset) throws JsonProcessingException {
        List<Attachment> attachments = apiClient.getAttachmentsForCase(caseId, limit, offset);
        return objectMapper.writeValueAsString(attachments);
    }

    @Tool(description = "Get all attachments for a test plan. Supports optional limit and offset for pagination.")
    public String getAttachmentsForPlan(int planId, Integer limit, Integer offset) throws JsonProcessingException {
        List<Attachment> attachments = apiClient.getAttachmentsForPlan(planId, limit, offset);
        return objectMapper.writeValueAsString(attachments);
    }

    @Tool(description = "Get all attachments for a test plan entry.")
    public String getAttachmentsForPlanEntry(int planId, String entryId) throws JsonProcessingException {
        List<Attachment> attachments = apiClient.getAttachmentsForPlanEntry(planId, entryId);
        return objectMapper.writeValueAsString(attachments);
    }

    @Tool(description = "Get all attachments for a test run. Supports optional limit and offset for pagination.")
    public String getAttachmentsForRun(int runId, Integer limit, Integer offset) throws JsonProcessingException {
        List<Attachment> attachments = apiClient.getAttachmentsForRun(runId, limit, offset);
        return objectMapper.writeValueAsString(attachments);
    }

    @Tool(description = "Get all attachments for a test.")
    public String getAttachmentsForTest(int testId) throws JsonProcessingException {
        List<Attachment> attachments = apiClient.getAttachmentsForTest(testId);
        return objectMapper.writeValueAsString(attachments);
    }

    @Tool(description = "Get a single attachment by ID. The attachment ID can be an integer (legacy) or UUID string (TestRail 7.1+).")
    public String getAttachment(String attachmentId) throws JsonProcessingException {
        Attachment attachment = apiClient.getAttachment(attachmentId);
        return objectMapper.writeValueAsString(attachment);
    }

    @Tool(description = "Delete an attachment by ID. The attachment ID can be an integer (legacy) or UUID string (TestRail 7.1+).")
    public String deleteAttachment(String attachmentId) {
        apiClient.deleteAttachment(attachmentId);
        return "{\"success\": true, \"message\": \"Attachment deleted successfully\"}";
    }
}
