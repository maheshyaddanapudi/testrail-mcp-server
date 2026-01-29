package io.github.testrail.mcp.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TestrailApiException.
 */
class TestrailApiExceptionTest {

    @Test
    void constructor_withMessage_shouldCreateException() {
        String message = "Test error message";
        TestrailApiException exception = new TestrailApiException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(0);
        assertThat(exception.getResponseBody()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_shouldCreateException() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        TestrailApiException exception = new TestrailApiException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStatusCode()).isEqualTo(0);
        assertThat(exception.getResponseBody()).isNull();
    }

    @Test
    void constructor_withMessageAndStatusCode_shouldCreateException() {
        String message = "Test error message";
        int statusCode = 400;
        String responseBody = "{\"error\": \"Bad request\"}";
        TestrailApiException exception = new TestrailApiException(message, statusCode, responseBody);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
        assertThat(exception.getResponseBody()).isEqualTo(responseBody);
    }

    @Test
    void constructor_withAllParameters_shouldCreateException() {
        String message = "Test error message";
        int statusCode = 500;
        String responseBody = "{\"error\": \"Internal server error\"}";
        Throwable cause = new RuntimeException("Root cause");
        TestrailApiException exception = new TestrailApiException(message, statusCode, responseBody, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(statusCode);
        assertThat(exception.getResponseBody()).isEqualTo(responseBody);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void getStatusCode_shouldReturnStatusCode() {
        TestrailApiException exception = new TestrailApiException("Error", 404, "Not found");
        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    void getResponseBody_shouldReturnResponseBody() {
        String responseBody = "{\"error\": \"Not found\"}";
        TestrailApiException exception = new TestrailApiException("Error", 404, responseBody);
        assertThat(exception.getResponseBody()).isEqualTo(responseBody);
    }

    @Test
    void exception_shouldBeThrowable() {
        TestrailApiException exception = new TestrailApiException("Test");
        assertThatThrownBy(() -> {
            throw exception;
        }).isInstanceOf(TestrailApiException.class)
          .hasMessage("Test");
    }

    @Test
    void exception_withNullResponseBody_shouldHandleGracefully() {
        TestrailApiException exception = new TestrailApiException("Error", 400, null);
        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getResponseBody()).isNull();
    }

    @Test
    void isNotFound_shouldReturnTrueFor404() {
        TestrailApiException exception = new TestrailApiException("Not found", 404, null);
        assertThat(exception.isNotFound()).isTrue();
    }

    @Test
    void isNotFound_shouldReturnTrueFor400WithNotFoundMessage() {
        TestrailApiException exception = new TestrailApiException("Error", 400, "{\"error\": \"Resource not found\"}");
        assertThat(exception.isNotFound()).isTrue();
    }

    @Test
    void isNotFound_shouldReturnFalseForOtherErrors() {
        TestrailApiException exception = new TestrailApiException("Error", 500, null);
        assertThat(exception.isNotFound()).isFalse();
    }

    @Test
    void isAuthenticationError_shouldReturnTrueFor401() {
        TestrailApiException exception = new TestrailApiException("Unauthorized", 401, null);
        assertThat(exception.isAuthenticationError()).isTrue();
    }

    @Test
    void isAuthenticationError_shouldReturnTrueFor403() {
        TestrailApiException exception = new TestrailApiException("Forbidden", 403, null);
        assertThat(exception.isAuthenticationError()).isTrue();
    }

    @Test
    void isAuthenticationError_shouldReturnFalseForOtherErrors() {
        TestrailApiException exception = new TestrailApiException("Error", 500, null);
        assertThat(exception.isAuthenticationError()).isFalse();
    }

    @Test
    void toString_shouldIncludeMessageAndStatusCode() {
        TestrailApiException exception = new TestrailApiException("Test error", 500, "body");
        String result = exception.toString();
        assertThat(result).contains("Test error");
        assertThat(result).contains("500");
    }
}
