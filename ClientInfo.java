import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

/**
 * Simple record to hold information about a connected client.
 * Stores the client's name, socket, connection time, and address.
 */
public record ClientInfo(
        String name, // The name of the client
        Socket socket, // The socket associated with the client
        Instant connectionTime, // The time the client connected
        String address // The client's IP address and port as a string
) {
    /**
     * Calculates the duration the client has been connected.
     * @return A Duration object representing the connection time.
     */
    public Duration getConnectionDuration() {
        return Duration.between(connectionTime, Instant.now());
    }

    /**
     * Formats a duration into a human-readable HH:MM:SS.sss format.
     * @param duration The duration to format.
     * @return A formatted string representing the duration.
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        long nano = Math.abs(duration.getNano());

        String positive = String.format(
                "%02d:%02d:%02d.%03d",
                absSeconds / 3600, // Hours
                (absSeconds % 3600) / 60, // Minutes
                absSeconds % 60, // Seconds
                nano / 1_000_000 // Milliseconds
        );
        return seconds < 0 ? "-" + positive : positive; // Add a negative sign if the duration is negative
    }

    /**
     * Provides a string representation of the ClientInfo object.
     * @return A string containing the client's name, address, and connection time.
     */
    @Override
    public String toString() {
        return "ClientInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", connectionTime=" + connectionTime +
                '}';
    }
}