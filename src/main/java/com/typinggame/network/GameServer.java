package com.typinggame.network;

import com.typinggame.domain.GameMode;
import com.typinggame.bot.BotPlayer;
import com.typinggame.bot.GameStatus;
import com.typinggame.bot.BotDifficulty;
import com.typinggame.domain.Word;
import com.typinggame.engine.TypingEngine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main game server that listens for client connections.
 * Handles dynamic game modes with HashMap session tracking.
 */
public class GameServer {

    private static final int PORT = 9090;
    private static final int MAX_PLAYERS_PER_SESSION = 2;

    private ServerSocket serverSocket;
    private volatile boolean running;
    private final AtomicInteger playerIdCounter;

    // HashMap to track waiting players by mode
    private final HashMap<GameMode, ConcurrentHashMap<String, ClientHandler>> waitingPlayersByMode;

    // HashMap to track active sessions
    private final HashMap<String, GameSession> activeSessions;

    // HashMap to track bot sessions
    private final HashMap<String, BotPlayer> botSessions;

    public GameServer() {
        this.playerIdCounter = new AtomicInteger(1);
        this.waitingPlayersByMode = new HashMap<>();
        this.activeSessions = new HashMap<>();
        this.botSessions = new HashMap<>();

        // Initialize waiting queues for each mode
        for (GameMode mode : GameMode.values()) {
            waitingPlayersByMode.put(mode, new ConcurrentHashMap<>());
        }
    }

    /**
     * Start the server and listen for connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;

            System.out.println("===========================================");
            System.out.println("Game Server started on port " + PORT);
            System.out.println("Supported modes: PRACTICE, VS_BOT, VS_FRIEND, ELIMINATION");
            System.out.println("Waiting for players to connect...");
            System.out.println("===========================================\n");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[GameServer] Error accepting connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[GameServer] Failed to start server: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Handle new client connection.
     */
    private void handleNewConnection(Socket clientSocket) {
        String playerId = "P" + playerIdCounter.getAndIncrement();
        ClientHandler clientHandler = new ClientHandler(clientSocket, playerId, this);
        clientHandler.start();

        System.out.println("[GameServer] New connection: " + playerId);
    }

    /**
     * Called when a player connects and sends CONNECT message with mode.
     * This is the connection handshake.
     */
    public synchronized void onPlayerConnected(ClientHandler clientHandler, String gameModeStr) {
        String playerId = clientHandler.getPlayerId();

        // Parse game mode from handshake
        GameMode gameMode = GameMode.fromString(gameModeStr);

        System.out.println("[GameServer] Player " + playerId + " (" + clientHandler.getPlayerName()
                + ") connected. Mode: " + gameMode.getDisplayName());

        // Handle based on mode
        switch (gameMode) {
            case PRACTICE:
                handlePracticeMode(clientHandler);
                break;

            case VS_BOT:
                handleVsBotMode(clientHandler);
                break;

            case VS_FRIEND:
            case ELIMINATION:
                handleMultiplayerMode(clientHandler, gameMode);
                break;
        }
    }

    /**
     * Handle PRACTICE mode - solo play, no opponent.
     */
    private void handlePracticeMode(ClientHandler clientHandler) {
        System.out.println("[GameServer] Starting PRACTICE mode for " + clientHandler.getPlayerId());

        // Send ready message
        GameMessage readyMsg = GameMessage.gameStart(List.of());
        clientHandler.sendMessage(readyMsg);

        // Practice mode doesn't need a session, client handles everything
    }

    /**
     * Handle VS_BOT mode - instantiate BotPlayer thread.
     * Uses difficulty enum with Comparator for speed selection.
     */
    private void handleVsBotMode(ClientHandler clientHandler) {
        String playerId = clientHandler.getPlayerId();

        // Determine difficulty (can be sent by client, default to MEDIUM)
        BotDifficulty difficulty = BotDifficulty.MEDIUM;

        System.out.println("[GameServer] Starting VS_BOT mode for " + playerId
                + " - Difficulty: " + difficulty.getDisplayName()
                + " (" + difficulty.getTargetWPM() + " WPM)");

        // Create word list
        TypingEngine engine = new TypingEngine();
        List<Word> words = engine.getRandomWords(50);

        // Create GameStatus for bot
        GameStatus gameStatus = new GameStatus(words.size());

        // Create and start bot player thread with difficulty-based speed
        BotPlayer bot = new BotPlayer(
                difficulty.getDisplayName(),
                words,
                gameStatus,
                difficulty.getTargetWPM() // Speed determined by difficulty
        );

        Thread botThread = new Thread(bot);
        botThread.setName("BotPlayer-" + playerId + "-" + difficulty);
        botThread.start();

        // Store bot session
        botSessions.put(playerId, bot);

        // Send game start with words
        List<String> wordStrings = words.stream().map(Word::getText).toList();
        GameMessage startMsg = GameMessage.gameStart(wordStrings);
        clientHandler.sendMessage(startMsg);

        System.out.println("[GameServer] Bot thread started for player " + playerId);
    }

    /**
     * Handle multiplayer modes (VS_FRIEND, ELIMINATION).
     */
    private void handleMultiplayerMode(ClientHandler clientHandler, GameMode gameMode) {
        String playerId = clientHandler.getPlayerId();
        ConcurrentHashMap<String, ClientHandler> waitingPlayers = waitingPlayersByMode.get(gameMode);

        // Add to waiting players for this mode
        waitingPlayers.put(playerId, clientHandler);

        System.out.println("[GameServer] Player " + playerId + " waiting for " + gameMode + " match. "
                + "Waiting players: " + waitingPlayers.size());

        // Check if we have enough players to start a session
        if (waitingPlayers.size() >= MAX_PLAYERS_PER_SESSION) {
            createGameSession(gameMode);
        } else {
            // Notify player they're waiting for opponent
            GameMessage waitingMsg = GameMessage.error("Waiting for opponent to connect...");
            clientHandler.sendMessage(waitingMsg);
        }
    }

    /**
     * Create a game session with two waiting players.
     */
    private void createGameSession(GameMode gameMode) {
        ConcurrentHashMap<String, ClientHandler> waitingPlayers = waitingPlayersByMode.get(gameMode);

        if (waitingPlayers.size() < MAX_PLAYERS_PER_SESSION) {
            return;
        }

        // Get first two waiting players
        ClientHandler[] players = waitingPlayers.values().toArray(new ClientHandler[0]);
        ClientHandler player1 = players[0];
        ClientHandler player2 = players[1];

        // Remove from waiting list
        waitingPlayers.remove(player1.getPlayerId());
        waitingPlayers.remove(player2.getPlayerId());

        // Create and start game session
        String sessionId = "SESSION-" + gameMode + "-" + System.currentTimeMillis();
        GameSession session = new GameSession(sessionId, player1, player2, gameMode);

        player1.setGameSession(session);
        player2.setGameSession(session);

        activeSessions.put(sessionId, session);
        session.start();

        System.out.println("[GameServer] Created " + gameMode.getDisplayName() + " session: " + sessionId);
        System.out.println("  Player 1: " + player1.getPlayerName() + " (" + player1.getPlayerId() + ")");
        System.out.println("  Player 2: " + player2.getPlayerName() + " (" + player2.getPlayerId() + ")");
    }

    /**
     * Called when a game session ends.
     */
    public void onSessionEnded(String sessionId) {
        activeSessions.remove(sessionId);
        System.out.println("[GameServer] Session ended: " + sessionId);
    }

    /**
     * Shutdown the server.
     */
    public void shutdown() {
        running = false;

        // Close all active sessions
        for (GameSession session : activeSessions.values()) {
            session.shutdown();
        }
        activeSessions.clear();

        // Stop all bot sessions
        for (BotPlayer bot : botSessions.values()) {
            bot.stop();
        }
        botSessions.clear();

        // Close all waiting player connections
        for (ConcurrentHashMap<String, ClientHandler> waitingPlayers : waitingPlayersByMode.values()) {
            for (ClientHandler handler : waitingPlayers.values()) {
                handler.shutdown();
            }
            waitingPlayers.clear();
        }

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[GameServer] Error closing server socket: " + e.getMessage());
        }

        System.out.println("[GameServer] Server shutdown complete");
    }

    /**
     * Main method to run the server.
     */
    public static void main(String[] args) {
        GameServer server = new GameServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[GameServer] Shutting down...");
            server.shutdown();
        }));

        server.start();
    }
}
