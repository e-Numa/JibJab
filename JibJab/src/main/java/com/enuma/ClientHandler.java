package com.enuma;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable { //allows multiple clients to run or interact concurrently without
    // being blocked or without obstructing each other in the chat application.
    private Socket socket; // Socket for communication with the clients
    private BufferedReader buffReader; // Reader method enables the servers ability to read input from the clients
    private BufferedWriter buffWriter; // Writer method enables the server ability for sending output to the clients
    private String name; // Server's reception for the name of the clients
    private boolean isShuttingDown = false; // A flag to indicate if the clients handler is shutting down - works for
    // respective main. If the shutdown function is implemented, this method ensures that the client handler receives
    // the command and shuts down.

    public ClientHandler (Socket socket) {
        try {
            this.socket = socket; // Initialize the socket with the provided socket
            this.buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // Created a
            // writer for sending data to the clients
            this.buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Created a reader
            // for reading data from the clients
            this.name = buffReader.readLine(); // Read the client's name from the input stream
            // Broadcast a message to all clients that this client has entered the room
            Server.broadcastMessage("SERVER: " + name + " has entered the room", this);
        } catch (IOException e) {
            closeClient(); // Handle any IO exceptions by closing the client handler
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (!isShuttingDown && socket.isConnected()) {//while, keeps client thread running while two conditions are
            // met
            try {
                messageFromClient = buffReader.readLine(); // Read a line of input from the client
                if (messageFromClient != null) {
                    if ("shutdown".equalsIgnoreCase(messageFromClient)) {
                        Server.shutdownClient(this); // Notify the server to remove this client from server
                        break;
                    }
                    if ("leave chat".equalsIgnoreCase(messageFromClient)) {
                        Server.broadcastMessage(name + " left the chat", this); // Broadcast a message that this client left the chat
                        closeClient(); // Close the client handler for exiting client
                        break;
                    }
                    Server.broadcastMessage(name + ": " + messageFromClient, this); // Broadcast the message from one
                    // client another or to all clients
                }
            } catch (IOException e) {
                closeClient(); // Handle any IO exceptions by closing the client handler
                break;
            }
        }
    }

    public void sendMessage(String message) {
        try {
            buffWriter.write(message); // Write a message to the client's output stream
            buffWriter.newLine(); // Add a new line to the message
            buffWriter.flush(); // Flush the writer memory to send the message immediately
        } catch (IOException e) {
            closeClient(); // Handle any IO exceptions by closing the client handler
        }
    }

    public void closeClient() {
        Server.removeClient(this); // Remove this client from the server's list of clients
        try {
            if (buffReader != null) {
                buffReader.close(); // Close the reader
            }
            if (buffWriter != null) {
                buffWriter.close(); // Close the writer
            }
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Close the socket if it's not already closed
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IO exceptions by printing the stack trace
        }
    }

    public boolean isShuttingDown() {
        return isShuttingDown; // Getter method for the shutting down flag
    }

    public void setShuttingDown(boolean shuttingDown) {
        isShuttingDown = shuttingDown; // Setter method for the shutting down flag
    }
}
