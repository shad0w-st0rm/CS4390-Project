# Multi-Client Math Server (Java Sockets)

## Overview

This project implements a simple network application demonstrating client-server communication using Java Sockets. It consists of a centralized Math Server that provides basic math calculation services to multiple Clients simultaneously. 
The server tracks connected users and logs their activity, while clients can connect, send calculation requests, and disconnect.

## Features

### Server (`Server.java`, `ClientHandler.java`)

*   Listens for incoming client connections on a specified port (default: 12345).
*   Handles multiple client connections concurrently using a thread pool (`ExecutorService` and `ClientHandler` runnable).
*   Tracks connected clients:
    *   Stores client name and connection time (`ClientInfo` record).
    *   Logs who connects, when they connect, and their address.
    *   Logs who disconnects and their connection duration.
*   Logs received requests, showing which client sent what.
*   Processes basic math calculation requests (`CALC:<equation>`) using the `MathSolver` class.
*   Handles client joining (`JOIN:<name>`) and exiting (`EXIT`).
*   Provides feedback to clients (acknowledgments, results, errors).
*   Logs server status messages to the console.

### Client (`Client.java`)

*   Connects to the Math Server at a specified address and port.
*   Prompts the user for a name upon startup.
*   Sends a `JOIN` request to the server and waits for acknowledgment (`ACK:JOIN:OK`).
*   Allows the user to manually enter basic math equations to be sent to the server (prefixed with `CALC:`).
*   **Automatically sends basic math calculation requests at random intervals** to demonstrate server handling of asynchronous requests.
*   Receives and displays calculation results (`RESULT:<value>`) or error messages (`ERROR:<message>`) from the server.
*   Sends an `EXIT` command to the server when the user types "EXIT".
*   Handles basic connection errors and provides feedback to the user.
*   Logs client-side actions and server responses to the console.

### Math Solver (`MathSolver.java`)

*   Provides static methods to parse and evaluate infix mathematical expressions (strings).
*   Supports basic arithmetic operations (+, -, \*, /) and parentheses.
*   Includes basic error detection (mismatched parentheses, division by zero, format errors).

## Requirements

*   **Java Development Kit (JDK):** Version 8 or higher. Tested with OpenJDK 21.
*   **IDE (Optional but Recommended):** IntelliJ IDEA, Eclipse, VS Code with Java extensions, etc.
*   **Operating System:** Windows, macOS, or Linux.

## Setup and Compilation

1.  **Clone the repository or download the source code.**
2.  **Place all `.java` files** (`Server.java`, `Client.java`, `ClientHandler.java`, `ClientInfo.java`, `Logger.java`, `MathSolver.java`) in the same source directory.
3.  **Compile the code:**
    *   Open a terminal or command prompt in the directory containing the `.java` files.
    *   Run the Java compiler:
        ```bash
        javac *.java
        ```
    *   This will generate `.class` files for all the source files.

## How to Run

### Using Command Line

1.  **Start the Server:** Open a terminal/command prompt, navigate to the directory with the compiled `.class` files, and run:
    ```bash
    java Server
    ```
    *(The server will output: "Server started on port 12345. Waiting for clients...")*

2.  **Start the First Client:** Open a **new, separate** terminal/command prompt, navigate to the same directory, and run:
    ```bash
    java Client
    ```
    *(It will prompt you to "Enter your name:". Type a name and press Enter.)*

3.  **Start Additional Clients:** Open **more new, separate** terminals/command prompts, navigate to the directory, and run `java Client` in each. Enter a **different unique name** for each client.

4.  **Interact:**
    *   In any client terminal, type a math equation (e.g., `(5+3)*10`, `100/5`) and press Enter.
    *   Observe the results sent back from the server.
    *   Notice random calculations being sent automatically by clients.
    *   Type `EXIT` in a client terminal to disconnect that client.
    *   Observe the logs in the Server terminal window.

### Using IntelliJ IDEA

1.  **Open the project** in IntelliJ.
2.  **Run the Server:**
    *   Locate `Server.java`.
    *   Right-click inside the editor or on the file in the Project view and select **Run 'Server.main()'**.
    *   Observe the output in the "Run" tool window. Leave it running.
3.  **Run the First Client:**
    *   Locate `Client.java`.
    *   Right-click and select **Run 'Client.main()'**.
    *   A new "Run" tab will open. Interact with it (enter name, etc.).
4.  **Allow Multiple Client Instances:**
    *   Go to the Run/Debug configurations dropdown (top right).
    *   Select "Edit Configurations...".
    *   Select the "Client" configuration.
    *   Check the box **"Allow multiple instances"** (or "Allow parallel run").
    *   Click Apply/OK.
5.  **Run Additional Clients:**
    *   Right-click `Client.java` and select **Run 'Client.main()'** again. Repeat for each additional client needed.
    *   Each will open in a new "Run" tab. Enter a unique name for each.
6.  **Interact:** Use the different client tabs to send requests and the server tab to monitor activity.

## Communication Protocol

The client and server communicate using simple text-based messages over TCP sockets. Messages are terminated by a newline character.

*   **Client -> Server (Join):** `JOIN:<ClientName>`
    *   Example: `JOIN:Alice`
*   **Server -> Client (Join Acknowledgment):**
    *   Success: `ACK:JOIN:OK`
    *   Failure: `ACK:JOIN:ERROR:<Reason>` (e.g., `ACK:JOIN:ERROR:Name already in use`)
*   **Client -> Server (Calculation):** `CALC:<Equation>`
    *   Example: `CALC:5 * (10 + 2)`
*   **Server -> Client (Calculation Result/Error):**
    *   Success: `RESULT:<Value>` (e.g., `RESULT:60.0`)
    *   Error: `ERROR:<ErrorMessage>` (e.g., `ERROR:Error: Division by zero`)
*   **Client -> Server (Exit):** `EXIT`

## Code Structure

*   `Server.java`: Main server class, listens for connections, manages client handlers via `ExecutorService`, holds the shared client list (`activeClients`).
*   `ClientHandler.java`: Runnable class handling all communication and logic for a single connected client thread on the server. Manages protocol states for one client.
*   `ClientInfo.java`: A Java `record` used to store tracked information about a connected client (name, socket, connection time, address).
*   `Client.java`: Main client application class, handles user interaction, connection logic, sending requests (user & random), and displaying server responses.
*   `MathSolver.java`: Utility class containing the logic to parse and evaluate mathematical expressions from strings.
*   `Logger.java`: A simple utility class used by server components for timestamped logging messages (currently just conceptually prepares logs).

## Team Members

*   Tanmaye Goel
*   Amir Akilimali
*   Ethan 

---
