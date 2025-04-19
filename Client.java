import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Replace with the server's IP address if needed
        int serverPort = 12345; // Replace with the server's port number

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);

            String input;
            while (true) {
                System.out.print("Enter a message to send to the server (or 'exit' to quit): ");
                input = userInput.readLine();

                // Send the message to the server
                out.println(input);

                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("Closing connection...");
                    break;
                }

                // Receive and print the server's response
                String response = in.readLine();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}