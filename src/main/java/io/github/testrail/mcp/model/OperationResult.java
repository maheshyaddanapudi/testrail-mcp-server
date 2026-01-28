package io.github.testrail.mcp.model;

/**
 * Represents the result of an operation that doesn't return a specific entity.
 */
public class OperationResult {

    private boolean success;
    private String message;

    private OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful operation result.
     *
     * @param message the success message
     * @return the operation result
     */
    public static OperationResult success(String message) {
        return new OperationResult(true, message);
    }

    /**
     * Creates a failed operation result.
     *
     * @param message the error message
     * @return the operation result
     */
    public static OperationResult failure(String message) {
        return new OperationResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
