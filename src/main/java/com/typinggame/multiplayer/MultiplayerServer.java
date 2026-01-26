package com.typinggame.multiplayer;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Requirement (4 & 5): Raw TCP server running inside Spring Boot.
 * Listens on port 9090 and spins a new Thread per player.
 */
@Component
public class MultiplayerServer implements Runnable {

    private static final int PORT = 9090;

    private final MultiplayerRoomService roomService;

    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public MultiplayerServer(MultiplayerRoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[MultiplayerServer] Listening on port " + PORT);

            while (running) {
                Socket socket = serverSocket.accept();
                PlayerThread playerThread = new PlayerThread(socket, roomService);
                playerThread.start(); // new Thread per connection
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[MultiplayerServer] Server error: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
