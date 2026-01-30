package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Label;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tools for managing TestRail labels for test case categorization.
 * Labels provide flexible tagging for test cases (e.g., "Release 2.0", "Critical", "Regression").
 * Unlike case types or priorities, labels are custom and can be applied to multiple test cases.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LabelsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = """
            Retrieves a single label by ID including its title and metadata.
            Labels are custom tags for categorizing and organizing test cases.
            Returns label ID, title (max 20 chars), creator, and creation timestamp.
            
            **When to use:** Use this tool when you need to view a specific label's details,
            verify label existence before applying to test cases, audit label metadata,
            or check label properties.
            
            **Might lead to:** update_label (to modify title), get_cases (to see which cases use this label).
            
            **Example prompts:**
            - "Show me label 1"
            - "Get details for label 'Release 2.0'"
            - "What's the title of label 5?"
            """)
    public Label getLabel(int labelId) {
        log.info("Getting label {}", labelId);
        return apiClient.getLabel(labelId);
    }

    @Tool(description = """
            Retrieves all labels for a project with optional pagination.
            Labels provide flexible tagging for test cases beyond built-in categorization (types, priorities).
            Common uses: release tags ("Release 2.0"), feature tags ("Payment"), criticality tags ("Must-Test").
            Supports pagination (limit/offset) - returns up to 250 labels per request.
            
            **When to use:** Use this tool when you need to browse available labels in a project,
            discover existing tags before creating test cases, audit label inventory,
            or prepare label-based filtering/reporting workflows.
            
            **Might lead to:** get_label (to view specific label details), update_label (to modify labels),
            get_cases (to filter cases by label).
            
            **Example prompts:**
            - "List all labels in project 1"
            - "Show me available tags for test cases"
            - "What labels exist for categorizing tests?"
            """)
    public List<Label> getLabels(int projectId, Integer limit, Integer offset) {
        log.info("Getting labels for project {}", projectId);
        return apiClient.getLabels(projectId, limit, offset);
    }

    @Tool(description = """
            Updates an existing label's title.
            Changes affect all test cases using this label - the label reference is updated everywhere.
            Maximum 20 characters allowed for title.
            
            Updatable fields:
            - **title** (string): New label title (max 20 characters)
            
            **When to use:** Use this tool when you need to rename labels for clarity,
            fix typos in label names, standardize label naming conventions,
            or update labels to reflect project changes. Changes propagate to all test cases using this label.
            
            **Might lead to:** get_label (to verify update), get_cases (to see affected test cases).
            
            **Example prompts:**
            - "Rename label 1 to 'Release 3.0'"
            - "Update label 'Critcal' to 'Critical'"
            - "Change label 5 title to 'High Priority'"
            """)
    public Label updateLabel(int labelId, Map<String, Object> label) {
        log.info("Updating label {}", labelId);
        return apiClient.updateLabel(labelId, label);
    }
}
