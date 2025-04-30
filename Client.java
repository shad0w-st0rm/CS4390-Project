import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static volatile boolean running = true; // Flag to signal reader thread to stop


    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);


        System.out.print("Enter your name: ");
        String clientName = userInput.nextLine().trim();


        if (clientName.isEmpty() || clientName.contains(":")) {
            System.err.println("Invalid name. Cannot be empty or contain ':'.");
            userInput.close();
            return;
        }


        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {


            log("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);


            // Send JOIN message
            log("Attempting to join as '" + clientName + "'...");
            out.println("JOIN:" + clientName);


            // Wait for JOIN Acknowledgment
            String joinResponse = in.readLine();
            log("Server response: " + joinResponse);


            if (joinResponse == null || !joinResponse.equals("ACK:JOIN:OK")) {
                System.err.println("Failed to join server: " + (joinResponse != null ? joinResponse : "Server disconnected"));
                userInput.close();
                return;
            }


            log("Successfully joined the server.");


            // Start a separate thread to listen for server messages
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = in.readLine()) != null) {
                        log("Server: " + serverMessage); // Displays messages like RESULT, ERROR
                    }
                } catch (SocketException e) {
                    if (running) { // log error if connection abruptly stopped
                        log("Connection closed by server or network error: " + e.getMessage());
                    }
                }
                catch (IOException e) {
                    if (running) {
                        log("Error reading from server: " + e.getMessage());
                    }
                } finally {
                    log("Reader thread finished.");
                    running = false; // Ensure main loop also stops if reader fails/finishes
                }
            });
            readerThread.start();


            // 4. Main loop for sending messages (user input + random calculations)
            log("You can now enter math equations (e.g., 5*8+2) or type 'EXIT' to quit.");
            Random random = new Random();
            long nextRandomCalcTime = System.currentTimeMillis() + (random.nextInt(5) + 3) * 1000; // 3-7 seconds first


            while (running) {
                // Check if it's time for a random calculation
                if (System.currentTimeMillis() >= nextRandomCalcTime) {
                    sendRandomCalculation(out, random);
                    nextRandomCalcTime = System.currentTimeMillis() + (random.nextInt(8) + 5) * 1000; // 5-12 seconds next
                }


                // integrate random calcs between inputs
                System.out.print("Enter command (equation or EXIT): ");
                String input = userInput.nextLine();


                if (!running) break; // Check if reader thread stopped


                if ("EXIT".equalsIgnoreCase(input)) {
                    log("Sending EXIT command...");
                    out.println("EXIT");
                    running = false; // Signal reader thread and break loop
                    break;
                } else if (input != null && !input.trim().isEmpty()) {
                    // Assume it's a calculation
                    log("Sending CALC: " + input);
                    out.println("CALC:" + input.trim());
                }



                // Inject a random calc sometimes after user input
                if (random.nextInt(10) < 2) {
                    sendRandomCalculation(out, random);
                    nextRandomCalcTime = System.currentTimeMillis() + (random.nextInt(8) + 5) * 1000; // Reset timer
                }


            }


            // Wait briefly for the reader thread to  finish processing last messages
            readerThread.join(1000); // Wait up to 1 second


        } catch (UnknownHostException e) {
            System.err.println("Error: Server not found at " + SERVER_ADDRESS + ":" + SERVER_PORT);
        } catch (IOException e) {
            System.err.println("Error: Could not connect to server or I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("Error: Client interrupted: " + e.getMessage());
        } finally {
            running = false; // Ensure flag is set
            userInput.close();
            log("Client application terminated.");
        }
    }


    private static void sendRandomCalculation(PrintWriter out, Random random) {
        if (!running) return; // Don't send if shutting down
        int num1 = random.nextInt(100) + 1;
        int num2 = random.nextInt(100) + 1;
        char[] operators = {'+', '-', '*', '/'};
        char op = operators[random.nextInt(operators.length)];


        // Avoid division by zero for the random calc
        if (op == '/' && num2 == 0) {
            num2 = 1;
        }


        // sometimes  add parentheses to switch things up
        String equation;
        if (random.nextBoolean()) {
            int num3 = random.nextInt(50)+1;
            char op2 = operators[random.nextInt(2)]; // Only + or - inside parentheses
            equation = String.format("(%d %c %d) %c %d", num1, op2, num3, op, num2);
        } else {
            equation = String.format("%d %c %d", num1, op, num2);
        }


        log("Sending random CALC: " + equation);
        out.println("CALC:" + equation);
    }


    private static void log(String message) {
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " + message);
    }


}