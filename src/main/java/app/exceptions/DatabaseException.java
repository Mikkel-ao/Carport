package app.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String userMessage) {
        super(userMessage);
        System.out.println("userMessage: " + userMessage);
    }

    public DatabaseException(String userMessage, String systemMessage) {
        super(userMessage);
        System.out.println("userMessage: " + userMessage);
        System.out.println("errorMessage: " + systemMessage);
    }

    public DatabaseException(String userMessage, Throwable cause) {
        super(userMessage, cause);  // Original exception + stacktrace
        System.out.println("userMessage: " + userMessage);
        System.out.println("Exception cause: " + cause.getMessage());
    }
}
