package com.typinggame.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Thread to handle individual client connection.
 * Reads messages from client and forwards to GameSession.
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private final String playerId;
    private final GameServer server;
    private GameSession gameSession;

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running;
    private String playerName;

    public ClientHandler(Socket socket, String playerId, GameServer server) {
        this.socket = socket;
        this.playerId = playerId;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Setup I/O streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("[ClientHandler] Player " + playerId + " connected from "
                    + socket.getInetAddress());

            // Read messages from client
            String messageJson;
            while (running && (messageJson = in.readLine()) != null) {
                try {
                    GameMessage message = GameMessage.fromJson(messageJson);
                    handleMessage(message);
                } catch (Exception e) {
                    System.err.println("[ClientHandler] Error parsing message: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[ClientHandler] Connection error for player " + playerId
                        + ": " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Handle incoming message from client.
     */
    private void handleMessage(GameMessage message) {
        System.out.println("[ClientHandler] Received from " + playerId + ": " + message.getType());

        switch (message.getType()) {
            case CONNECT:
                this.playerName = message.getPlayerName();
                server.onPlayerConnected(this, message.getGameMode());
                break;

            case READY:
                if (gameSession != null) {
                    gameSession.onPlayerReady(playerId);
                }
                break;

            case PROGRESS_UPDATE:
                if (gameSession != null) {
                    gameSession.onProgressUpdate(playerId, message.getProgress());
                }
                break;

            case DISCONNECT:
                running = false;
                break;

            default:
                System.err.println("[ClientHandler] Unhandled message type: " + message.getType());
        }
    }

    /**
     * Send message to client.
     */
    public void sendMessage(GameMessage message) {
        if (out != null && !socket.isClosed()) {
            try {
                out.println(message.toJson());
            } catch (Exception e) {
                System.err.println("[ClientHandler] Error sending message to " + playerId
                        + ": " + e.getMessage());
            }
        }
    }

    /**
     * Set the game session for this client.
     */
    public void setGameSession(GameSession session) {
        this.gameSession = session;
    }

    /**
     * Cleanup resources and close connection.
     */
    public void cleanup() {
        running = false;

        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error during cleanup: " + e.getMessage());
        }

        // Notify server of disconnection
        if (gameSession != null) {
            gameSession.onPlayerDisconnected(playerId);
        }

        System.out.println("[ClientHandler] Player " + playerId + " disconnected");
    }

    /**
     * Stop this client handler.
     */
    public void shutdown() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error shutting down: " + e.getMessage());
        }
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName != null ? playerName : "Player " + playerId;
    }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed();
    }
}
