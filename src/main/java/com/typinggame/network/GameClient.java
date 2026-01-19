package com.typinggame.network;

import com.typinggame.domain.GameMode;
import com.typinggame.network.GameMessage.PlayerProgress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

/**
 * Client to connect to GameServer for multiplayer mode.
 * Sends progress updates and receives opponent progress and attacks.
 */
public class GameClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running;

    private String playerId;
    private String playerName;
    private GameMode gameMode;

    private ServerListenerThread listenerThread;

    public GameClient(String playerName, GameMode gameMode) {
        this.playerName = playerName;
        this.gameMode = gameMode;
        this.playerId = UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Connect to the game server.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            running = true;

            System.out.println("[GameClient] Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);

            // Send connection message
            GameMessage connectMsg = GameMessage.connect(playerId, playerName, gameMode.name());
            sendMessage(connectMsg);

            // Start listener thread
            listenerThread = new ServerListenerThread();
            listenerThread.start();

            return true;

        } catch (IOException e) {
            System.err.println("[GameClient] Failed to connect: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send message to server.
     */
    public void sendMessage(GameMessage message) {
        if (out != null && running) {
            out.println(message.toJson());
        }
    }

    /**
     * Send ready signal to server.
     */
    public void sendReady() {
        GameMessage readyMsg = GameMessage.ready(playerId);
        sendMessage(readyMsg);
        System.out.println("[GameClient] Sent READY signal");
    }

    /**
     * Send progress update to server.
     */
    public void sendProgressUpdate(double wpm, double accuracy, int wordsCompleted,
            int totalWords, String currentWord, long elapsedTime) {
        PlayerProgress progress = new PlayerProgress(wpm, accuracy, wordsCompleted,
                totalWords, currentWord, elapsedTime);
        GameMessage msg = GameMessage.progressUpdate(playerId, progress);
        sendMessage(msg);
    }

    /**
     * Disconnect from server.
     */
    public void disconnect() {
        running = false;

        if (out != null) {
            GameMessage disconnectMsg = new GameMessage(GameMessage.MessageType.DISCONNECT);
            sendMessage(disconnectMsg);
        }

        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("[GameClient] Error during disconnect: " + e.getMessage());
        }

        System.out.println("[GameClient] Disconnected from server");
    }

    /**
     * Thread to listen for messages from server.
     */
    private class ServerListenerThread extends Thread {
        @Override
        public void run() {
            try {
                String messageJson;
                while (running && (messageJson = in.readLine()) != null) {
                    try {
                        GameMessage message = GameMessage.fromJson(messageJson);
                        handleServerMessage(message);
                    } catch (Exception e) {
                        System.err.println("[GameClient] Error parsing message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[GameClient] Connection lost: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handle message from server.
     */
    private void handleServerMessage(GameMessage message) {
        switch (message.getType()) {
            case GAME_START:
                System.out.println("\n[GameClient] ===== GAME STARTED =====");
                System.out.println("Words to type: " + message.getAttackWords().size());
                System.out.println("First 5 words: " + message.getAttackWords().subList(0,
                        Math.min(5, message.getAttackWords().size())));
                System.out.println("=====================================\n");
                break;

            case OPPONENT_PROGRESS:
                PlayerProgress oppProgress = message.getProgress();
                System.out.println("[Opponent] WPM: " + String.format("%.1f", oppProgress.getWpm())
                        + " | Accuracy: " + String.format("%.1f", oppProgress.getAccuracy()) + "%"
                        + " | Words: " + oppProgress.getWordsCompleted() + "/"
                        + oppProgress.getTotalWords());
                break;

            case ATTACK:
                System.out.println("\n*** ATTACKED! ***");
                System.out.println("Opponent sent you " + message.getAttackWords().size()
                        + " difficult words:");
                System.out.println(message.getAttackWords());
                System.out.println("*****************\n");
                break;

            case GAME_END:
                System.out.println("\n[GameClient] ===== GAME ENDED =====");
                System.out.println(message.getMessage());
                if (message.getWinnerId() != null) {
                    if (message.getWinnerId().equals(playerId)) {
                        System.out.println("🎉 YOU WON! 🎉");
                    } else {
                        System.out.println("You lost. Better luck next time!");
                    }
                }
                System.out.println("===================================\n");
                running = false;
                break;

            case ERROR:
                System.out.println("[Server] " + message.getMessage());
                break;

            default:
                System.out.println("[GameClient] Received: " + message.getType());
        }
    }

    /**
     * Demo main method to test the client.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Select mode (1=VS_FRIEND, 2=ELIMINATION): ");
        int modeChoice = scanner.nextInt();
        GameMode mode = modeChoice == 2 ? GameMode.ELIMINATION : GameMode.VS_FRIEND;

        GameClient client = new GameClient(name, mode);

        if (client.connect()) {
            System.out.println("Connected! Waiting for opponent...");

            // Wait a bit for game to start
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send ready signal
            client.sendReady();

            // Simulate typing progress (for testing)
            System.out.println("\nSimulating typing progress...");
            for (int i = 1; i <= 10 && client.running; i++) {
                try {
                    Thread.sleep(2000);

                    double wpm = 30 + (i * 5); // Gradually increase WPM
                    double accuracy = 95.0 + (Math.random() * 5);

                    client.sendProgressUpdate(wpm, accuracy, i, 50, "word" + i, i * 2000);
                    System.out.println("Sent progress: " + i + " words, " + wpm + " WPM");

                } catch (InterruptedException e) {
                    break;
                }
            }

            // Wait for game to end
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.disconnect();
        }

        scanner.close();
    }
}
