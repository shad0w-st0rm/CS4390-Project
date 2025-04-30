import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles communication with a single client connection on a separate thread.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ConcurrentMap<String, ClientInfo> activeClients;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName = null; // gets set after successful JOIN
    private ClientInfo clientInfo = null;
    private final Logger logger = new Logger("Server");


    public ClientHandler(Socket socket, ConcurrentMap<String, ClientInfo> activeClients) {
        this.socket = socket;
        this.activeClients = activeClients;
        this.logger.setContext("Handler-" + socket.getPort()); // context for logs
    }

    @Override
    public void run() {
        Instant connectionTime = Instant.now(); // Record time when handler starts
        String clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle JOIN
            String joinMessage = in.readLine();
            if (joinMessage == null || !joinMessage.startsWith("JOIN:")) {
                logger.logError("Invalid JOIN message from " + clientAddress + ": " + joinMessage);
                safeSend("ERROR:Protocol error - Expected JOIN:<Name>");
                return; // Exit thread
            }

            clientName = joinMessage.substring("JOIN:".length()).trim();
            if (clientName.isEmpty() || clientName.contains(":")) { // Basic validation
                logger.logError("Invalid client name from " + clientAddress + ": '" + clientName + "'");
                safeSend("ACK:JOIN:ERROR:Invalid name");
                return; // Exit thread
            }

            // check and add the client
            clientInfo = new ClientInfo(clientName, socket, connectionTime, clientAddress);
            ClientInfo existing = activeClients.putIfAbsent(clientName, clientInfo);

            if (existing != null) {
                logger.logWarning("Client name '" + clientName + "' already taken by " + existing.address());
                safeSend("ACK:JOIN:ERROR:Name already in use");
                return; // Exit thread
            }

            // Successfully joined
            logger.setContext(clientName); // Update logger context to use client name
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
                    handleCalculation(message.substring("CALC:".length()));
                } else if ("EXIT".equalsIgnoreCase(message)) {
                    break; // Exit loop
                } else {
                    logger.logWarning("Received unknown message format: " + message);
                    safeSend("ERROR:Unknown command");
                }
            }

        } catch (SocketException e) {
            logger.logInfo("Socket closed unexpectedly for " + (clientName != null ? clientName : clientAddress) + ". Assuming disconnection.");
            Server.logToServerConsole(String.format("[%s] Connection lost for %s.",
                    Logger.getTimestamp(), (clientName != null ? clientName : clientAddress)));
        }
        catch (IOException e) {
            logger.logError("I/O Error handling client " + (clientName != null ? clientName : clientAddress) + ": " + e.getMessage());
            Server.logToServerConsole(String.format("[%s] I/O Error for %s: %s",
                    Logger.getTimestamp(), (clientName != null ? clientName : clientAddress), e.getMessage()));

        } finally {
            cleanup();
        }
    }

    private void handleCalculation(String equation) {
        if (equation == null || equation.trim().isEmpty()) {
            logger.logWarning("Received empty CALC request.");
            safeSend("ERROR:Empty equation provided.");
            return;
        }
        MathSolver.MathResult result = MathSolver.solveEquation(equation.trim());
        String response;
        if (result.getStatusCode() == MathSolver.MathResult.SUCCESS) {
            response = "RESULT:" + result.getValue();
            logger.logInfo("Calculation success: " + equation + " = " + result.getValue());
        } else {
            response = "ERROR:" + result.getStatusMessage();
            logger.logWarning("Calculation error: " + equation + " -> " + result.getStatusMessage());
        }
        safeSend(response);
        Server.logToServerConsole(String.format("[%s] Sent to %s: %s",
                Logger.getTimestamp(), clientName, response));
    }


    private void safeSend(String message) {
        if (out != null && !out.checkError()) { // Check error flag before writing
            out.println(message);
        } else if (out == null) {
            logger.logError("Cannot send message - PrintWriter is null.");
        } else {
            logger.logError("Cannot send message - PrintWriter encountered an error previously.");
        }
    }


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


