import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class for the Math Server application.
 * Listens for incoming client connections, manages active clients, and delegates client handling to separate threads.
 */
public class Server {

    private static final int PORT = 12345; // Port number the server listens on
    // Thread-safe map to store connected clients: Key=clientName, Value=ClientInfo
    private static final ConcurrentMap<String, ClientInfo> activeClients = new ConcurrentHashMap<>();
    private static final Logger serverLogger = new Logger("Server"); // Logger for server events
    private static final ExecutorService clientExecutor = Executors.newCachedThreadPool(); // Thread pool for handling clients

    /**
     * Main entry point for the server application.
     * Initializes the server socket, listens for client connections, and delegates client handling to threads.
     */
    public static void main(String[] args) {
        logToServerConsole("Starting Math Server...");

        // Try to start the server socket and listen for incoming connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logToServerConsole("Server started on port " + PORT + ". Waiting for clients...");

            // Infinite loop to accept client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Blocking call, waits for a client
                    // Hand off the new client connection to a handler thread from the pool
                    clientExecutor.submit(new ClientHandler(clientSocket, activeClients));
                } catch (IOException e) {
                    // Log errors that occur while accepting client connections
                    serverLogger.logError("Error accepting client connection: " + e.getMessage());
                    logToServerConsole("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Log fatal errors that prevent the server from starting
            serverLogger.logError("Could not start server on port " + PORT + ": " + e.getMessage());
            logToServerConsole("FATAL: Could not start server on port " + PORT + ": " + e.getMessage());
        } finally {
            // Shut down the thread pool when the server exits
            clientExecutor.shutdown();
            logToServerConsole("Server shutting down.");
        }
    }

    /**
     * Logs a message to the server's main console.
     * @param message The message to log.
     */
    public static void logToServerConsole(String message) {
        System.out.println(message); // Simple console log for main server events
    }

    /**
     * Displays a list of currently connected clients in the server console.
     * Iterates through the activeClients map and logs each client's details.
     */
    public static void showConnectedClients() {
        logToServerConsole("---- Active Clients ----");
        if (activeClients.isEmpty()) {
            // No clients are currently connected
            logToServerConsole("  (None)");
        } else {
            // Iterate through the active clients and log their details
            activeClients.forEach((name, info) -> {
                logToServerConsole(String.format("  - %s (Connected: %s, From: %s)",
                        name,
                        info.connectionTime().toString(), // Using Instant's default toString
                        info.address()));
            });
        }
        logToServerConsole("----------------------");
    }
}