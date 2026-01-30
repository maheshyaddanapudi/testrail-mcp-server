package io.github.testrail.mcp.tools.attachments;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Attachment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools for managing TestRail attachments.
 * Attachments are files (screenshots, logs, documents) associated with test cases, plans, runs, and results.
 * Supports both legacy integer IDs and UUID-based IDs (TestRail 7.1+).
 */
@Component
public class AttachmentsTools {

    private final TestrailApiClient apiClient;

    public AttachmentsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_attachments_for_case",
            description = """
                    Retrieves all attachments associated with a test case.
                    Returns attachment metadata including ID, filename, size, upload date, and user.
                    Supports pagination via limit and offset parameters (TestRail 6.7+).
                    Requires TestRail 5.7 or later.
                    
                    **When to use:** Use this tool when you need to view all files attached to a test case,
                    download case documentation or reference materials, audit attachment history,
                    verify uploaded files, or prepare for attachment migration/cleanup.
                    
                    **Might lead to:** get_attachment (to retrieve specific attachment details), delete_attachment (to remove files).
                    
                    **Example prompts:**
                    - "Show me all attachments for test case 123"
                    - "List files attached to case 456"
                    - "What attachments are on test case 789?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachments_for_case', {caseId: 123})",
                    "execute_tool('get_attachments_for_case', {caseId: 456, limit: 10})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "attachments", "files", "case", "documents"}
    )
    public List<Attachment> getAttachmentsForCase(
            @InternalToolParam(description = "The ID of the test case")
            int caseId,
            @InternalToolParam(description = "Maximum number of attachments to return", required = false)
            Integer limit,
            @InternalToolParam(description = "Number of attachments to skip for pagination", required = false)
            Integer offset
    ) {
        List<Attachment> attachments = apiClient.getAttachmentsForCase(caseId, limit, offset);
        return attachments;
    }

    @InternalTool(
            name = "get_attachments_for_plan",
            description = """
                    Retrieves all attachments associated with a test plan.
                    Returns attachment metadata including ID, filename, size, upload date, and user.
                    Supports pagination via limit and offset parameters (TestRail 6.7+).
                    Requires TestRail 6.3 or later.
                    
                    **When to use:** Use this tool when you need to view all files attached to a test plan,
                    access plan-level documentation, audit plan attachments, verify uploaded files,
                    or prepare for attachment migration/cleanup.
                    
                    **Might lead to:** get_attachment (to retrieve specific attachment details), delete_attachment (to remove files).
                    
                    **Example prompts:**
                    - "Show me all attachments for test plan 10"
                    - "List files attached to plan 25"
                    - "What documents are attached to test plan 50?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachments_for_plan', {planId: 10})",
                    "execute_tool('get_attachments_for_plan', {planId: 25, limit: 20})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "attachments", "files", "plan", "documents"}
    )
    public List<Attachment> getAttachmentsForPlan(
            @InternalToolParam(description = "The ID of the test plan")
            int planId,
            @InternalToolParam(description = "Maximum number of attachments to return", required = false)
            Integer limit,
            @InternalToolParam(description = "Number of attachments to skip for pagination", required = false)
            Integer offset
    ) {
        List<Attachment> attachments = apiClient.getAttachmentsForPlan(planId, limit, offset);
        return attachments;
    }

    @InternalTool(
            name = "get_attachments_for_plan_entry",
            description = """
                    Retrieves all attachments associated with a specific test plan entry.
                    A plan entry represents a test run configuration within a test plan.
                    Returns attachment metadata including ID, filename, size, upload date, and user.
                    Requires TestRail 6.3 or later.
                    
                    **When to use:** Use this tool when you need to view files attached to a specific plan entry/configuration,
                    access entry-specific documentation, audit entry attachments, or verify uploaded files.
                    
                    **Might lead to:** get_attachment (to retrieve specific attachment details), delete_attachment (to remove files).
                    
                    **Example prompts:**
                    - "Show me attachments for plan 10 entry 'abc123'"
                    - "List files attached to plan entry 5 in plan 20"
                    - "What attachments are on the Chrome configuration entry?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachments_for_plan_entry', {planId: 10, entryId: 'abc123'})",
                    "execute_tool('get_attachments_for_plan_entry', {planId: 20, entryId: '5'})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "attachments", "files", "plan", "entry", "configuration"}
    )
    public List<Attachment> getAttachmentsForPlanEntry(
            @InternalToolParam(description = "The ID of the test plan")
            int planId,
            @InternalToolParam(description = "The ID of the plan entry")
            String entryId
    ) {
        List<Attachment> attachments = apiClient.getAttachmentsForPlanEntry(planId, entryId);
        return attachments;
    }

    @InternalTool(
            name = "get_attachments_for_run",
            description = """
                    Retrieves all attachments associated with a test run.
                    Returns attachment metadata including ID, filename, size, upload date, and user.
                    Supports pagination via limit and offset parameters (TestRail 6.7+).
                    Requires TestRail 6.3 or later.
                    
                    **When to use:** Use this tool when you need to view all files attached to a test run,
                    access run-level documentation or logs, audit run attachments, verify uploaded files,
                    or prepare for attachment migration/cleanup.
                    
                    **Might lead to:** get_attachment (to retrieve specific attachment details), delete_attachment (to remove files).
                    
                    **Example prompts:**
                    - "Show me all attachments for test run 100"
                    - "List files attached to run 250"
                    - "What logs are attached to test run 500?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachments_for_run', {runId: 100})",
                    "execute_tool('get_attachments_for_run', {runId: 250, limit: 50})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "attachments", "files", "run", "logs"}
    )
    public List<Attachment> getAttachmentsForRun(
            @InternalToolParam(description = "The ID of the test run")
            int runId,
            @InternalToolParam(description = "Maximum number of attachments to return", required = false)
            Integer limit,
            @InternalToolParam(description = "Number of attachments to skip for pagination", required = false)
            Integer offset
    ) {
        List<Attachment> attachments = apiClient.getAttachmentsForRun(runId, limit, offset);
        return attachments;
    }

    @InternalTool(
            name = "get_attachments_for_test",
            description = """
                    Retrieves all attachments associated with a specific test (test instance in a run).
                    Returns attachment metadata including ID, filename, size, upload date, and user.
                    Requires TestRail 5.7 or later.
                    
                    **When to use:** Use this tool when you need to view files attached to a specific test execution,
                    access test-specific screenshots or logs, audit test attachments, or verify uploaded files.
                    
                    **Might lead to:** get_attachment (to retrieve specific attachment details), delete_attachment (to remove files).
                    
                    **Example prompts:**
                    - "Show me all attachments for test 1000"
                    - "List files attached to test 2500"
                    - "What screenshots are on test 5000?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachments_for_test', {testId: 1000})",
                    "execute_tool('get_attachments_for_test', {testId: 2500})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "attachments", "files", "test", "screenshots"}
    )
    public List<Attachment> getAttachmentsForTest(
            @InternalToolParam(description = "The ID of the test")
            int testId
    ) {
        List<Attachment> attachments = apiClient.getAttachmentsForTest(testId);
        return attachments;
    }

    @InternalTool(
            name = "get_attachment",
            description = """
                    Retrieves detailed metadata for a single attachment by its ID.
                    Supports both legacy integer IDs and UUID-based IDs (TestRail 7.1+).
                    Returns attachment details including filename, size, upload date, user, and associated entity.
                    
                    **When to use:** Use this tool when you need to get details about a specific attachment,
                    verify attachment properties, check who uploaded a file, or prepare for download/deletion.
                    
                    **Might lead to:** delete_attachment (to remove the file).
                    
                    **Example prompts:**
                    - "Show me details for attachment 443"
                    - "Get information about attachment '2ec27be4-812f-4806-9a5d-d39130d1691a'"
                    - "What's the filename for attachment 1000?"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('get_attachment', {attachmentId: '443'})",
                    "execute_tool('get_attachment', {attachmentId: '2ec27be4-812f-4806-9a5d-d39130d1691a'})"
            },
            keywords = {"get", "retrieve", "fetch", "show", "view", "attachment", "details", "metadata"}
    )
    public Attachment getAttachment(
            @InternalToolParam(description = "The ID of the attachment (integer or UUID)")
            String attachmentId
    ) {
        Attachment attachment = apiClient.getAttachment(attachmentId);
        return attachment;
    }

    @InternalTool(
            name = "delete_attachment",
            description = """
                    Permanently deletes an attachment by its ID.
                    Supports both legacy integer IDs and UUID-based IDs (TestRail 7.1+).
                    **WARNING: This action cannot be undone.** The file will be permanently removed.
                    
                    **When to use:** Use this tool ONLY when you need to remove obsolete or incorrect attachments,
                    clean up duplicate files, free up storage space, or remove sensitive information.
                    Always verify the attachment ID before deletion.
                    
                    **Might lead to:** get_attachments_for_* (to verify deletion).
                    
                    **Example prompts:**
                    - "Delete attachment 443"
                    - "Remove attachment '2ec27be4-812f-4806-9a5d-d39130d1691a'"
                    - "Permanently delete attachment 1000"
                    """,
            category = "attachments",
            examples = {
                    "execute_tool('delete_attachment', {attachmentId: '443'})",
                    "execute_tool('delete_attachment', {attachmentId: '2ec27be4-812f-4806-9a5d-d39130d1691a'})"
            },
            keywords = {"delete", "remove", "erase", "destroy", "purge", "attachment", "file", "cleanup"}
    )
    public String deleteAttachment(
            @InternalToolParam(description = "The ID of the attachment to delete (integer or UUID)")
            String attachmentId
    ) {
        apiClient.deleteAttachment(attachmentId);
        return "{\"success\": true, \"message\": \"Attachment deleted successfully\"}";
    }
}
