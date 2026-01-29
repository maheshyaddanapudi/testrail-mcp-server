package io.github.testrail.mcp.tools;

import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.TestCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BDDsTools {

    private final TestrailApiClient apiClient;

    @Tool(description = "Export a BDD scenario from a test case as a .feature file")
    public String getBdd(int caseId) {
        log.info("Getting BDD for case {}", caseId);
        return apiClient.getBdd(caseId);
    }

    @Tool(description = "Import/upload a BDD scenario to a section from a .feature file content")
    public TestCase addBdd(int sectionId, String featureFileContent) {
        log.info("Adding BDD to section {}", sectionId);
        return apiClient.addBdd(sectionId, featureFileContent);
    }
}
