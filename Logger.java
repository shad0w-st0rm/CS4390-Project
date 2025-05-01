//  Worked on by Amir

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple utility class for timestamped console logging.
 * Provides methods to log messages with different severity levels (INFO, WARN, ERROR).
 */
public class Logger {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"); // Timestamp format
    private String context; // Context for the logger (e.g., "Server", "ClientName", "Handler-12345")

    /**
     * Constructor to initialize the logger with a specific context.
     * @param initialContext The initial context for the logger.
     */
    public Logger(String initialContext) {
        this.context = initialContext;
    }

    /**
     * Updates the context of the logger.
     * @param context The new context to set.
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Generates a timestamp for log entries.
     * @return The current timestamp as a formatted string.
     */
    public static String getTimestamp() {
        return dtf.format(LocalDateTime.now());
    }

    /**
     * Logs a message with a specific severity level.
     * @param level The severity level (e.g., INFO, WARN, ERROR).
     * @param message The message to log.
     */
    private void log(String level, String message) {
        String logEntry = String.format("[%s] [%s] [%s] %s", getTimestamp(), level, context, message);
        System.out.println(logEntry); // Print the log entry to the console
    }

    /**
     * Logs an informational message.
     * @param message The message to log.
     */
    public void logInfo(String message) {
        log("INFO", message);
    }

    /**
     * Logs a warning message.
     * @param message The message to log.
     */
    public void logWarning(String message) {
        log("WARN", message);
    }

    /**
     * Logs an error message.
     * @param message The message to log.
     */
    public void logError(String message) {
        log("ERROR", message);
    }
}