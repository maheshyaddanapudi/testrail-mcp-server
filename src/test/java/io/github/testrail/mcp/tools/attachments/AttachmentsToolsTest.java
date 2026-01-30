package io.github.testrail.mcp.tools.attachments;

import io.github.testrail.mcp.tools.attachments.*;

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

    @BeforeEach
    void setUp() {
        attachmentsTools = new AttachmentsTools(apiClient);
    }

    @Test
    void getAttachmentsForCase_shouldReturnAttachments() {
        Attachment attachment = new Attachment();
        attachment.setId(1);
        attachment.setName("test.jpg");

        when(apiClient.getAttachmentsForCase(123, null, null)).thenReturn(List.of(attachment));

        List<Attachment> result = attachmentsTools.getAttachmentsForCase(123, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test.jpg");
        verify(apiClient).getAttachmentsForCase(123, null, null);
    }

    @Test
    void getAttachmentsForPlan_shouldReturnAttachments() {
        Attachment attachment = new Attachment();
        attachment.setId(2);
        attachment.setName("plan.pdf");

        when(apiClient.getAttachmentsForPlan(456, 10, 0)).thenReturn(List.of(attachment));

        List<Attachment> result = attachmentsTools.getAttachmentsForPlan(456, 10, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("plan.pdf");
        verify(apiClient).getAttachmentsForPlan(456, 10, 0);
    }

    @Test
    void getAttachmentsForPlanEntry_shouldReturnAttachments() {
        Attachment attachment = new Attachment();
        attachment.setId(3);

        when(apiClient.getAttachmentsForPlanEntry(789, "abc-123")).thenReturn(List.of(attachment));

        List<Attachment> result = attachmentsTools.getAttachmentsForPlanEntry(789, "abc-123");

        assertThat(result).hasSize(1);
        verify(apiClient).getAttachmentsForPlanEntry(789, "abc-123");
    }

    @Test
    void getAttachmentsForRun_shouldReturnAttachments() {
        Attachment attachment = new Attachment();
        attachment.setId(4);

        when(apiClient.getAttachmentsForRun(111, null, null)).thenReturn(List.of(attachment));

        List<Attachment> result = attachmentsTools.getAttachmentsForRun(111, null, null);

        assertThat(result).hasSize(1);
        verify(apiClient).getAttachmentsForRun(111, null, null);
    }

    @Test
    void getAttachmentsForTest_shouldReturnAttachments() {
        Attachment attachment = new Attachment();
        attachment.setId(5);

        when(apiClient.getAttachmentsForTest(222)).thenReturn(List.of(attachment));

        List<Attachment> result = attachmentsTools.getAttachmentsForTest(222);

        assertThat(result).hasSize(1);
        verify(apiClient).getAttachmentsForTest(222);
    }

    @Test
    void getAttachment_shouldReturnSingleAttachment() {
        Attachment attachment = new Attachment();
        attachment.setId("uuid-123");
        attachment.setName("doc.pdf");

        when(apiClient.getAttachment("uuid-123")).thenReturn(attachment);

        Attachment result = attachmentsTools.getAttachment("uuid-123");

        assertThat(result.getName()).isEqualTo("doc.pdf");
        verify(apiClient).getAttachment("uuid-123");
    }

    @Test
    void deleteAttachment_shouldDeleteSuccessfully() {
        doNothing().when(apiClient).deleteAttachment("uuid-456");

        attachmentsTools.deleteAttachment("uuid-456");

        verify(apiClient).deleteAttachment("uuid-456");
    }
}
