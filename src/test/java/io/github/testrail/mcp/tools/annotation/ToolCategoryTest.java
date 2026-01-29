package io.github.testrail.mcp.tools.annotation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ToolCategory enum.
 */
class ToolCategoryTest {

    @Test
    void cases_shouldHaveCorrectDisplayName() {
        assertThat(ToolCategory.CASES.getDisplayName()).isEqualTo("Test Cases");
    }

    @Test
    void cases_shouldHaveCorrectDescription() {
        assertThat(ToolCategory.CASES.getDescription()).isEqualTo("Operations related to test case management");
    }

    @Test
    void projects_shouldHaveCorrectDisplayName() {
        assertThat(ToolCategory.PROJECTS.getDisplayName()).isEqualTo("Projects");
    }

    @Test
    void projects_shouldHaveCorrectDescription() {
        assertThat(ToolCategory.PROJECTS.getDescription()).isEqualTo("Operations related to project management");
    }

    @Test
    void runs_shouldHaveCorrectDisplayName() {
        assertThat(ToolCategory.RUNS.getDisplayName()).isEqualTo("Test Runs");
    }

    @Test
    void runs_shouldHaveCorrectDescription() {
        assertThat(ToolCategory.RUNS.getDescription()).isEqualTo("Operations related to test run management");
    }

    @Test
    void results_shouldHaveCorrectDisplayName() {
        assertThat(ToolCategory.RESULTS.getDisplayName()).isEqualTo("Test Results");
    }

    @Test
    void results_shouldHaveCorrectDescription() {
        assertThat(ToolCategory.RESULTS.getDescription()).isEqualTo("Operations related to test results");
    }

    @Test
    void sections_shouldHaveCorrectDisplayName() {
        assertThat(ToolCategory.SECTIONS.getDisplayName()).isEqualTo("Sections");
    }

    @Test
    void sections_shouldHaveCorrectDescription() {
        assertThat(ToolCategory.SECTIONS.getDescription()).isEqualTo("Operations related to test case sections");
    }

    @Test
    void allCategories_shouldHaveNonNullDisplayNames() {
        for (ToolCategory category : ToolCategory.values()) {
            assertThat(category.getDisplayName()).isNotNull();
            assertThat(category.getDisplayName()).isNotEmpty();
        }
    }

    @Test
    void allCategories_shouldHaveNonNullDescriptions() {
        for (ToolCategory category : ToolCategory.values()) {
            assertThat(category.getDescription()).isNotNull();
            assertThat(category.getDescription()).isNotEmpty();
        }
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        assertThat(ToolCategory.valueOf("CASES")).isEqualTo(ToolCategory.CASES);
        assertThat(ToolCategory.valueOf("PROJECTS")).isEqualTo(ToolCategory.PROJECTS);
        assertThat(ToolCategory.valueOf("RUNS")).isEqualTo(ToolCategory.RUNS);
        assertThat(ToolCategory.valueOf("RESULTS")).isEqualTo(ToolCategory.RESULTS);
        assertThat(ToolCategory.valueOf("SECTIONS")).isEqualTo(ToolCategory.SECTIONS);
    }

    @Test
    void values_shouldReturnAllCategories() {
        ToolCategory[] categories = ToolCategory.values();
        assertThat(categories).hasSize(5);
        assertThat(categories).contains(
            ToolCategory.CASES,
            ToolCategory.PROJECTS,
            ToolCategory.RUNS,
            ToolCategory.RESULTS,
            ToolCategory.SECTIONS
        );
    }
}
