import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple utility class for timestamped console logging.
 */
public class Logger {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private String context; // such as "Server", "ClientName", "Handler-12345"

    public Logger(String initialContext) {
        this.context = initialContext;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public static String getTimestamp() {
        return dtf.format(LocalDateTime.now());
    }

    private void log(String level, String message) {

        String logEntry = String.format("[%s] [%s] [%s] %s", getTimestamp(), level, context, message);
    }

    public void logInfo(String message) {
        log("INFO", message);
    }

    public void logWarning(String message) {
        log("WARN", message);
    }

    public void logError(String message) {
        log("ERROR", message);
    }
}