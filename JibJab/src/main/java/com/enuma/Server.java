package com.enuma;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    // All constants and variables for the server is defined
    private static final int SERVER_PORT = 3057; // Port number on which the server listens to  all client
    private static ServerSocket serverSocket; // Server socket for accepting client connections through port
    private static volatile boolean isRunning = true; // A flag to control the server's running state
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // Array list to manage connected
    // clients

    // The main method where the server starts and listens for client connections
    public static void main(String[] args) {
        try {
            // Create a server socket bound to the specified port
            serverSocket = new ServerSocket(SERVER_PORT);

            // Print a message to indicate that the server has connected and is waiting for clients
            System.out.println("Server connected. Waiting for client...");

            while (isRunning) {
                // Accept a client connection and create a socket for communication with other client
                Socket socket = serverSocket.accept();

                // Create a ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(socket);

                // Add the client handler to the list of client handlers
                clientHandlers.add(clientHandler);

                // Start a new thread to handle communication with the client
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();

            // Handle any IO exceptions by closing the server
            closeServer();
        }
    }

    // This method removes a client handler from the list of connected clients
    public static synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    // This method broadcasts a message to all connected clients
    public static synchronized void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    // This method broadcasts a given message to all connected clients except the sender
    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != sender) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // This method closes the server and all client connections
    public static synchronized void closeServer() {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.closeClient();
        }
        clientHandlers.clear();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method handles the shutdown of a specific client
    public static synchronized void shutdownClient(ClientHandler clientHandler) {
        if (clientHandler.isShuttingDown()) {
            removeClient(clientHandler);
        }
    }
}
