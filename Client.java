// Worked on by Tanmaye and modified by Amir

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client {

    // Server address and port configuration
    private static final String SERVER_ADDRESS = "127.0.0.1"; // Localhost address
    private static final int SERVER_PORT = 12345; // Port number the server is listening on
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS"); // Timestamp format for logging
    private static volatile boolean running = true; // Flag to signal the reader thread to stop

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in); // Scanner for user input

        // Prompt the user to enter their name
        System.out.print("Enter your name: ");
        String clientName = userInput.nextLine().trim(); // Read and trim the input

        // Validate the client name
        if (clientName.isEmpty() || clientName.contains(":")) {
            System.err.println("Invalid name. Cannot be empty or contain ':'.");
            userInput.close();
            return; // Exit if the name is invalid
        }

        // Try to establish a connection to the server
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // Create a socket to connect to the server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Output stream to send data to the server
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { // Input stream to receive data from the server

            log("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Send a JOIN message to the server with the client's name
            log("Attempting to join as '" + clientName + "'...");
            out.println("JOIN:" + clientName);

            // Wait for the server's response to the JOIN request
            String joinResponse = in.readLine();
            log("Server response: " + joinResponse);

            // Check if the JOIN request was successful
            if (joinResponse == null || !joinResponse.equals("ACK:JOIN:OK")) {
                System.err.println("Failed to join server: " + (joinResponse != null ? joinResponse : "Server disconnected"));
                userInput.close();
                return; // Exit if the JOIN request failed
            }

            log("Successfully joined the server.");

            // Start a separate thread to listen for messages from the server
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        log("Server: " + serverMessage); // Log messages received from the server
                    }
                } catch (SocketException e) {
                    if (running) { // Log error if the connection was abruptly closed
                        log("Connection closed by server or network error: " + e.getMessage());
                    }
                } catch (IOException e) {
                    if (running) { // Log error if there was an issue reading from the server
                        log("Error reading from server: " + e.getMessage());
                    }
                } finally {
                    log("Reader thread finished."); // Log when the reader thread finishes
                    running = false; // Ensure the main loop also stops if the reader fails/finishes
                }
            });
            readerThread.start(); // Start the reader thread

            // Main loop for sending messages (user input + random calculations)
            log("You can now enter math equations (e.g., 5*8+2) or type 'EXIT' to quit.");
            Random random = new Random(); // Random generator for random calculations

            while (running) {
                // Prompt the user for input
                System.out.print("Enter command (equation (or RANDOM) or EXIT): ");
                String input = userInput.nextLine(); // Read user input

                if (!running) break; // Check if the reader thread has stopped

                if ("EXIT".equalsIgnoreCase(input)) {
                    // Handle the EXIT command
                    log("Sending EXIT command...");
                    out.println("EXIT"); // Send the EXIT command to the server
                    running = false; // Signal the reader thread and break the loop
                    break;
                } else if (input.equalsIgnoreCase("RANDOM")) {
                    // Handle the RANDOM command to send a random calculation
                    sendRandomCalculation(out, random);
                } else if (input != null && !input.trim().isEmpty()) {
                    // Assume the input is a calculation and send it to the server
                    log("Sending CALC: " + input);
                    out.println("CALC:" + input.trim());
                }
            }

            // Wait briefly for the reader thread to finish processing the last messages
            readerThread.join(1000); // Wait up to 1 second

        } catch (UnknownHostException e) {
            // Handle the case where the server address is invalid
            System.err.println("Error: Server not found at " + SERVER_ADDRESS + ":" + SERVER_PORT);
        } catch (IOException e) {
            // Handle I/O errors during communication with the server
            System.err.println("Error: Could not connect to server or I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            // Handle interruptions during thread operations
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("Error: Client interrupted: " + e.getMessage());
        } finally {
            // Ensure resources are cleaned up and the application terminates gracefully
            running = false; // Ensure the running flag is set to false
            userInput.close(); // Close the user input scanner
            log("Client application terminated.");
        }
    }

    // Method to send a random calculation to the server
    private static void sendRandomCalculation(PrintWriter out, Random random) {
        if (!running) return; // Don't send if the client is shutting down
        int num1 = random.nextInt(100) + 1; // Generate a random number between 1 and 100
        int num2 = random.nextInt(100) + 1; // Generate another random number between 1 and 100
        char[] operators = {'+', '-', '*', '/'}; // Array of supported operators
        char op = operators[random.nextInt(operators.length)]; // Randomly select an operator

        // Avoid division by zero for the random calculation
        if (op == '/' && num2 == 0) {
            num2 = 1; // Replace zero with one to avoid division by zero
        }

        // Sometimes add parentheses to make the equation more complex
        String equation;
        if (random.nextBoolean()) {
            int num3 = random.nextInt(50) + 1; // Generate a third random number
            char op2 = operators[random.nextInt(2)]; // Randomly select an operator (+ or -) for the parentheses
            equation = String.format("(%d %c %d) %c %d", num1, op2, num3, op, num2); // Format the equation with parentheses
        } else {
            equation = String.format("%d %c %d", num1, op, num2); // Format a simple equation
        }

        log("Sending random CALC: " + equation); // Log the random calculation
        out.println("CALC:" + equation); // Send the random calculation to the server
    }

    // Method to log messages with a timestamp
    private static void log(String message) {
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " + message); // Print the message with a timestamp
    }
}