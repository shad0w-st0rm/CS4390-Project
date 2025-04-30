import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 12345;
    // Thread-safe map to store connected clients: Key=clientName, Value=ClientInfo
    private static final ConcurrentMap<String, ClientInfo> activeClients = new ConcurrentHashMap<>();
    private static final Logger serverLogger = new Logger("Server");
    private static final ExecutorService clientExecutor = Executors.newCachedThreadPool();


    public static void main(String[] args) {
        logToServerConsole("Starting Math Server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logToServerConsole("Server started on port " + PORT + ". Waiting for clients...");

            while (true) { // Keep accepting clients indefinitely
                try {
                    Socket clientSocket = serverSocket.accept(); // Blocking call, waits for a client
                    // Hand off the new client connection to a handler thread from the pool
                    clientExecutor.submit(new ClientHandler(clientSocket, activeClients));
                } catch (IOException e) {
                    serverLogger.logError("Error accepting client connection: " + e.getMessage());
                    logToServerConsole("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            serverLogger.logError("Could not start server on port " + PORT + ": " + e.getMessage());
            logToServerConsole("FATAL: Could not start server on port " + PORT + ": " + e.getMessage());
        } finally {
            clientExecutor.shutdown(); // Shut down the thread pool when server exits
            logToServerConsole("Server shutting down.");
        }
    }

    // Centralized logging to the server's main console
    public static void logToServerConsole(String message) {
        System.out.println(message); // Simple console log for main server events
    }


    // Example method to show connected clients
    public static void showConnectedClients() {
        logToServerConsole("---- Active Clients ----");
        if (activeClients.isEmpty()) {
            logToServerConsole("  (None)");
        } else {
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