package io.github.testrail.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.testrail.mcp.client.TestrailApiClient;
import io.github.testrail.mcp.model.Attachment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentsToolsTest {

    @Mock
    private TestrailApiClient apiClient;

    private AttachmentsTools attachmentsTools;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        attachmentsTools = new AttachmentsTools(apiClient, objectMapper);
    }

    @Test
    void getAttachmentsForCase_shouldReturnAttachments() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(1);
        attachment.setName("test.jpg");

        when(apiClient.getAttachmentsForCase(123, null, null)).thenReturn(List.of(attachment));

        String result = attachmentsTools.getAttachmentsForCase(123, null, null);

        assertThat(result).contains("test.jpg");
        verify(apiClient).getAttachmentsForCase(123, null, null);
    }

    @Test
    void getAttachmentsForPlan_shouldReturnAttachments() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(2);
        attachment.setName("plan.pdf");

        when(apiClient.getAttachmentsForPlan(456, 10, 0)).thenReturn(List.of(attachment));

        String result = attachmentsTools.getAttachmentsForPlan(456, 10, 0);

        assertThat(result).contains("plan.pdf");
        verify(apiClient).getAttachmentsForPlan(456, 10, 0);
    }

    @Test
    void getAttachmentsForPlanEntry_shouldReturnAttachments() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(3);

        when(apiClient.getAttachmentsForPlanEntry(789, "abc-123")).thenReturn(List.of(attachment));

        String result = attachmentsTools.getAttachmentsForPlanEntry(789, "abc-123");

        assertThat(result).isNotEmpty();
        verify(apiClient).getAttachmentsForPlanEntry(789, "abc-123");
    }

    @Test
    void getAttachmentsForRun_shouldReturnAttachments() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(4);

        when(apiClient.getAttachmentsForRun(111, null, null)).thenReturn(List.of(attachment));

        String result = attachmentsTools.getAttachmentsForRun(111, null, null);

        assertThat(result).isNotEmpty();
        verify(apiClient).getAttachmentsForRun(111, null, null);
    }

    @Test
    void getAttachmentsForTest_shouldReturnAttachments() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(5);

        when(apiClient.getAttachmentsForTest(222)).thenReturn(List.of(attachment));

        String result = attachmentsTools.getAttachmentsForTest(222);

        assertThat(result).isNotEmpty();
        verify(apiClient).getAttachmentsForTest(222);
    }

    @Test
    void getAttachment_shouldReturnSingleAttachment() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId("uuid-123");
        attachment.setName("doc.pdf");

        when(apiClient.getAttachment("uuid-123")).thenReturn(attachment);

        String result = attachmentsTools.getAttachment("uuid-123");

        assertThat(result).contains("doc.pdf");
        verify(apiClient).getAttachment("uuid-123");
    }

    @Test
    void deleteAttachment_shouldDeleteSuccessfully() {
        doNothing().when(apiClient).deleteAttachment("uuid-456");

        String result = attachmentsTools.deleteAttachment("uuid-456");

        assertThat(result).contains("success");
        verify(apiClient).deleteAttachment("uuid-456");
    }
}
