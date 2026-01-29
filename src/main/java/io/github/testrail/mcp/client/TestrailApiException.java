package io.github.testrail.mcp.client;

/**
 * Exception thrown when a TestRail API call fails.
 */
public class TestrailApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public TestrailApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public TestrailApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public TestrailApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public TestrailApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Returns whether this is a "not found" error (HTTP 400 with specific message or 404).
     *
     * @return true if the resource was not found
     */
    public boolean isNotFound() {
        return statusCode == 404 ||
                (statusCode == 400 && responseBody != null &&
                        (responseBody.contains("not found") || responseBody.contains("does not exist")));
    }

    /**
     * Returns whether this is an authentication error.
     *
     * @return true if authentication failed
     */
    public boolean isAuthenticationError() {
        return statusCode == 401 || statusCode == 403;
    }

    @Override
    public String toString() {
        return "TestrailApiException{" +
                "message='" + getMessage() + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
