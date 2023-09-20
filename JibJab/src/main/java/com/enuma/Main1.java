package com.enuma;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // User enters their name for the client
        System.out.println("Enter your name for the client:");
        String name = scanner.nextLine();

        try {
            // Create a socket to connect to the server on localhost and port 3057
            Socket socket = new Socket("localhost", 3057);

            // Create a PrintWriter for sending data to the server
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            // Send the client's name to the server
            writer.println(name);
            writer.flush();

            // Start a separate thread to listen for messages from the server
            Thread serverListener = new Thread(() -> {
                try {
                    // Create a Scanner to read messages from the server
                    Scanner serverInput = new Scanner(socket.getInputStream());

                    // Continuously listen for messages from the server
                    while (serverInput.hasNextLine()) {
                        String message = serverInput.nextLine();

                        // Check if the server sent a "/shutdown" message
                        if ("shutdown".equalsIgnoreCase(message)) {
                            System.out.println("Server has closed the connection.");
                            break;
                        }

                        // Print the received message from the server
                        System.out.println(message);
                    }

                    // Close the socket when the server connection is closed
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Start the server listener thread
            serverListener.start();

            while (true) {
                // Read a message from the user and send it to the server
                String message = scanner.nextLine();
                writer.println(message);
                writer.flush();

                // Check if the user wants to shut down the client
                if ("shutdown".equalsIgnoreCase(message)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
