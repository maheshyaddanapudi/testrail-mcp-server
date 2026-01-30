package io.github.testrail.mcp.tools.reports;

import io.github.testrail.mcp.annotation.InternalTool;
import io.github.testrail.mcp.annotation.InternalToolParam;
import io.github.testrail.mcp.client.TestrailApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.testrail.mcp.model.Report;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tools for running and managing TestRail reports.
 * Reports provide insights into test activities, coverage, progress, and results.
 * Only reports marked as "On demand via the API" are accessible via these endpoints.
 */
@Component
public class ReportsTools {

    private final TestrailApiClient apiClient;

    public ReportsTools(TestrailApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @InternalTool(
            name = "get_reports",
            description = """
                    Retrieves all API-accessible reports for a single project.
                    Returns report templates that have been marked as "On demand via the API" during creation.
                    Note: Reports must be created with the "On-demand via the API" checkbox enabled to appear here.
                    This setting cannot be changed after report creation.
                    
                    Report types include: Activity Summary, Test Results Summary, Defects Summary, Progress Reports, etc.
                    
                    **When to use:** Use this tool when you need to discover available report templates in a project,
                    get report template IDs for execution, audit API-accessible reports, or prepare for report automation.
                    
                    **Might lead to:** run_report (to execute a specific report template).
                    
                    **Example prompts:**
                    - "List all API reports for project 1"
                    - "What reports are available via API in project 5?"
                    - "Show me report templates I can run for project 10"
                    """,
            category = "reports",
            examples = {
                    "execute_tool('get_reports', {projectId: 1})",
                    "execute_tool('get_reports', {projectId: 5})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "reports", "templates", "api", "available"}
    )
    public List<Report> getReports(
            @InternalToolParam(description = "The ID of the project to retrieve reports for")
            int projectId
    ) {
        List<Report> reports = apiClient.getReports(projectId);
        return reports;
    }

    @InternalTool(
            name = "run_report",
            description = """
                    Executes a single-project report and returns URLs for accessing the results.
                    Returns report_url (web view), report_html (HTML format), and report_pdf (PDF format).
                    The report may not be available immediately - processing time varies by report complexity.
                    Requires TestRail 5.7 or later.
                    
                    **When to use:** Use this tool when you need to generate on-demand reports,
                    automate report distribution, integrate report data into external systems,
                    or provide stakeholders with current test metrics.
                    
                    **Might lead to:** get_reports (to find available report templates).
                    
                    **Example prompts:**
                    - "Run report template 1"
                    - "Execute the Activity Summary report"
                    - "Generate report 5 and give me the PDF link"
                    """,
            category = "reports",
            examples = {
                    "execute_tool('run_report', {reportTemplateId: 1})",
                    "execute_tool('run_report', {reportTemplateId: 5})"
            },
            keywords = {"run", "execute", "generate", "create", "report", "template", "pdf", "html"}
    )
    public JsonNode runReport(
            @InternalToolParam(description = "The ID of the report template to execute")
            int reportTemplateId
    ) {
        JsonNode reportData = apiClient.runReport(reportTemplateId);
        return reportData;
    }

    @InternalTool(
            name = "get_cross_project_reports",
            description = """
                    Retrieves all API-accessible cross-project reports.
                    Cross-project reports aggregate data across multiple projects for enterprise-wide insights.
                    Returns report templates that have been marked as "On demand via the API".
                    Requires TestRail Enterprise license.
                    
                    **When to use:** Use this tool when you need to discover available cross-project report templates,
                    get report template IDs for enterprise-wide reporting, audit cross-project reports,
                    or prepare for multi-project report automation.
                    
                    **Might lead to:** run_cross_project_report (to execute a cross-project report).
                    
                    **Example prompts:**
                    - "List all cross-project reports"
                    - "What enterprise reports are available via API?"
                    - "Show me cross-project report templates"
                    """,
            category = "reports",
            examples = {
                    "execute_tool('get_cross_project_reports', {})"
            },
            keywords = {"get", "list", "retrieve", "fetch", "show", "browse", "cross", "project", "reports", "enterprise"}
    )
    public List<Report> getCrossProjectReports() {
        List<Report> reports = apiClient.getCrossProjectReports();
        return reports;
    }

    @InternalTool(
            name = "run_cross_project_report",
            description = """
                    Executes a cross-project report and returns URLs for accessing the results.
                    Cross-project reports provide enterprise-wide insights across multiple projects.
                    Returns report_url (web view), report_html (HTML format), and report_pdf (PDF format).
                    The report may not be available immediately - processing time varies by report complexity.
                    Requires TestRail Enterprise license.
                    
                    **When to use:** Use this tool when you need to generate enterprise-wide reports,
                    automate cross-project report distribution, provide executive dashboards,
                    or integrate multi-project metrics into external systems.
                    
                    **Might lead to:** get_cross_project_reports (to find available templates).
                    
                    **Example prompts:**
                    - "Run cross-project report template 1"
                    - "Execute the enterprise Activity Summary report"
                    - "Generate cross-project report 5 and give me the PDF"
                    """,
            category = "reports",
            examples = {
                    "execute_tool('run_cross_project_report', {reportTemplateId: 1})",
                    "execute_tool('run_cross_project_report', {reportTemplateId: 5})"
            },
            keywords = {"run", "execute", "generate", "create", "cross", "project", "report", "enterprise", "pdf"}
    )
    public JsonNode runCrossProjectReport(
            @InternalToolParam(description = "The ID of the cross-project report template to execute")
            int reportTemplateId
    ) {
        JsonNode reportData = apiClient.runCrossProjectReport(reportTemplateId);
        return reportData;
    }
}
