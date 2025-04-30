import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

/**
 * Simple record to hold information about a connected client.
 */
public record ClientInfo(
        String name,
        Socket socket,
        Instant connectionTime,
        String address // Store IP and port as String for logging
) {
    /**
     * Calculates the duration the client was connected.
     * @return Duration object representing connection time.
     */
    public Duration getConnectionDuration() {
        return Duration.between(connectionTime, Instant.now());
    }

    /**
     * Formats duration into HH:MM:SS.sss format.
     * @param duration The duration to format.
     * @return Formatted string.
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        long nano = Math.abs(duration.getNano());

        String positive = String.format(
                "%02d:%02d:%02d.%03d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60,
                nano / 1_000_000); // Milliseconds
        return seconds < 0 ? "-" + positive : positive;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", connectionTime=" + connectionTime +
                '}';
    }
}