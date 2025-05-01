import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles communication with a single client connection on a separate thread.
 * Manages client requests, processes calculations, and handles disconnections.
 */
public class ClientHandler implements Runnable {

    private final Socket socket; // The socket for communication with the client
    private final ConcurrentMap<String, ClientInfo> activeClients; // Shared map of active clients
    private PrintWriter out; // Output stream to send messages to the client
    private BufferedReader in; // Input stream to receive messages from the client
    private String clientName = null; // The name of the client (set after successful JOIN)
    private ClientInfo clientInfo = null; // Information about the connected client
    private final Logger logger = new Logger("Server"); // Logger for this handler

    /**
     * Constructor to initialize the handler with the client socket and active clients map.
     * @param socket The socket for communication with the client.
     * @param activeClients The shared map of active clients.
     */
    public ClientHandler(Socket socket, ConcurrentMap<String, ClientInfo> activeClients) {
        this.socket = socket;
        this.activeClients = activeClients;
        this.logger.setContext("Handler-" + socket.getPort()); // Set context for logging
    }

    /**
     * The main logic for handling client communication.
     * Processes JOIN requests, CALC requests, and EXIT commands.
     */
    @Override
    public void run() {
        Instant connectionTime = Instant.now(); // Record the time when the client connected
        String clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort(); // Client's address

        try {
            // Initialize input and output streams for communication
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle the JOIN request
            String joinMessage = in.readLine();
            if (joinMessage == null || !joinMessage.startsWith("JOIN:")) {
                logger.logError("Invalid JOIN message from " + clientAddress + ": " + joinMessage);
                safeSend("ERROR:Protocol error - Expected JOIN:<Name>");
                return; // Exit the thread
            }

            // Extract and validate the client name
            clientName = joinMessage.substring("JOIN:".length()).trim();
            if (clientName.isEmpty() || clientName.contains(":")) { // Basic validation
                logger.logError("Invalid client name from " + clientAddress + ": '" + clientName + "'");
                safeSend("ACK:JOIN:ERROR:Invalid name");
                return; // Exit the thread
            }

            // Add the client to the active clients map
            clientInfo = new ClientInfo(clientName, socket, connectionTime, clientAddress);
            ClientInfo existing = activeClients.putIfAbsent(clientName, clientInfo);

            if (existing != null) {
                logger.logWarning("Client name '" + clientName + "' already taken by " + existing.address());
                safeSend("ACK:JOIN:ERROR:Name already in use");
                return; // Exit the thread
            }

            // Successfully joined
            logger.setContext(clientName); // Update logger context to use the client name
            logger.logInfo("Client connected from " + clientAddress);
            Server.logToServerConsole(String.format("[%s] Client '%s' connected from %s",
                    Logger.getTimestamp(), clientName, clientAddress));
            safeSend("ACK:JOIN:OK");

            // Handle subsequent requests (CALC, EXIT)
            String message;
            while ((message = in.readLine()) != null) {
                logger.logInfo("Received request: " + message);
                Server.logToServerConsole(String.format("[%s] Received from %s: %s",
                        Logger.getTimestamp(), clientName, message));

                if (message.startsWith("CALC:")) {
                    // Handle a calculation request
                    handleCalculation(message.substring("CALC:".length()));
                } else if ("EXIT".equalsIgnoreCase(message)) {
                    // Handle the EXIT command
                    break; // Exit the loop
                } else {
                    // Handle unknown commands
                    logger.logWarning("Received unknown message format: " + message);
                    safeSend("ERROR:Unknown command");
                }
            }

        } catch (SocketException e) {
            // Handle unexpected socket closure
            logger.logInfo("Socket closed unexpectedly for " + (clientName != null ? clientName : clientAddress) + ". Assuming disconnection.");
            Server.logToServerConsole(String.format("[%s] Connection lost for %s.",
                    Logger.getTimestamp(), (clientName != null ? clientName : clientAddress)));
        } catch (IOException e) {
            // Handle I/O errors
            logger.logError("I/O Error handling client " + (clientName != null ? clientName : clientAddress) + ": " + e.getMessage());
            Server.logToServerConsole(String.format("[%s] I/O Error for %s: %s",
                    Logger.getTimestamp(), (clientName != null ? clientName : clientAddress), e.getMessage()));
        } finally {
            // Cleanup resources and remove the client from the active list
            cleanup();
        }
    }

    /**
     * Handles a calculation request from the client.
     * @param equation The equation to calculate.
     */
    private void handleCalculation(String equation) {
        if (equation == null || equation.trim().isEmpty()) {
            logger.logWarning("Received empty CALC request.");
            safeSend("ERROR:Empty equation provided.");
            return;
        }
        // Solve the equation using MathSolver
        MathSolver.MathResult result = MathSolver.solveEquation(equation.trim());
        String response;
        if (result.getStatusCode() == MathSolver.MathResult.SUCCESS) {
            response = "RESULT:" + result.getValue();
            logger.logInfo("Calculation success: " + equation + " = " + result.getValue());
        } else {
            response = "ERROR:" + result.getStatusMessage();
            logger.logWarning("Calculation error: " + equation + " -> " + result.getStatusMessage());
        }
        // Send the response back to the client
        safeSend(response);
        Server.logToServerConsole(String.format("[%s] Sent to %s: %s",
                Logger.getTimestamp(), clientName, response));
    }

    /**
     * Safely sends a message to the client.
     * Ensures the PrintWriter is valid before sending.
     * @param message The message to send.
     */
    private void safeSend(String message) {
        if (out != null && !out.checkError()) { // Check error flag before writing
            out.println(message);
        } else if (out == null) {
            logger.logError("Cannot send message - PrintWriter is null.");
        } else {
            logger.logError("Cannot send message - PrintWriter encountered an error previously.");
        }
    }

    /**
     * Cleans up resources and removes the client from the active clients map.
     * Logs the disconnection and closes all resources.
     */
    private void cleanup() {
        logger.logInfo("Cleaning up connection.");

        // Remove client from the active list if they were successfully added
        if (clientName != null && clientInfo != null) {
            activeClients.remove(clientName, clientInfo);
            Duration duration = clientInfo.getConnectionDuration();
            String formattedDuration = ClientInfo.formatDuration(duration);
            logger.logInfo("Client disconnected. Duration: " + formattedDuration);
            Server.logToServerConsole(String.format("[%s] Client '%s' disconnected. Duration: %s",
                    Logger.getTimestamp(), clientName, formattedDuration));
        } else {
            // Client never fully joined or info wasn't set
            String address = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            logger.logInfo("Client disconnected (never fully joined or name not set): " + address);
            Server.logToServerConsole(String.format("[%s] Client disconnected (never fully joined): %s",
                    Logger.getTimestamp(), address));
        }

        // Close resources in reverse order of creation
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            logger.logError("Error closing PrintWriter: " + e.getMessage());
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            logger.logError("Error closing BufferedReader: " + e.getMessage());
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.logError("Error closing socket: " + e.getMessage());
        }

        logger.logInfo("Cleanup complete.");
    }
}


