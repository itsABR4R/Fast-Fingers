package com.typinggame.network;

import com.typinggame.domain.GameMode;
import com.typinggame.domain.Word;
import com.typinggame.engine.TypingEngine;
import com.typinggame.network.GameMessage.PlayerProgress;
import com.typinggame.mode.PlayerScore;
import com.typinggame.mode.PlayerScore.WinnerResult;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread managing a match between two players.
 * Broadcasts real-time progress updates and handles Elimination Mode attacks.
 */
public class GameSession extends Thread {

    private static final int BROADCAST_INTERVAL_MS = 500; // 500ms updates
    private static final double ELIMINATION_ATTACK_THRESHOLD = 60.0; // 60 WPM
    private static final int ATTACK_WORD_COUNT = 3;

    private final String sessionId;
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final GameMode gameMode;
    private final TypingEngine typingEngine;

    private final ConcurrentHashMap<String, PlayerProgress> playerProgress;
    private final ConcurrentHashMap<String, Boolean> playerReady;
    private final ConcurrentHashMap<String, Boolean> hasAttacked;
    private final ConcurrentHashMap<String, PlayerHealth> playerHealthMap; // Health tracking

    private volatile boolean running;
    private volatile boolean gameStarted;
    private List<String> gameWords;
    private long gameStartTime;

    public GameSession(String sessionId, ClientHandler player1, ClientHandler player2, GameMode gameMode) {
        this.sessionId = sessionId;
        this.player1 = player1;
        this.player2 = player2;
        this.gameMode = gameMode;
        this.typingEngine = new TypingEngine();

        this.playerProgress = new ConcurrentHashMap<>();
        this.playerReady = new ConcurrentHashMap<>();
        this.hasAttacked = new ConcurrentHashMap<>();
        this.playerHealthMap = new ConcurrentHashMap<>();

        this.running = true;
        this.gameStarted = false;

        // Initialize tracking
        hasAttacked.put(player1.getPlayerId(), false);
        hasAttacked.put(player2.getPlayerId(), false);

        // Initialize health if mode has health system
        if (gameMode.hasHealthSystem()) {
            playerHealthMap.put(player1.getPlayerId(), new PlayerHealth(player1.getPlayerId()));
            playerHealthMap.put(player2.getPlayerId(), new PlayerHealth(player2.getPlayerId()));
            System.out.println("[GameSession] Health system enabled for " + gameMode.getDisplayName());
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("[GameSession] Session " + sessionId + " started");

            // Generate word list for the game
            generateWordList();

            // Send game start message to both players
            GameMessage startMsg = GameMessage.gameStart(gameWords);
            player1.sendMessage(startMsg);
            player2.sendMessage(startMsg);

            System.out.println("[GameSession] Waiting for players to be ready...");

            // Wait for both players to be ready
            waitForPlayersReady();

            if (!running)
                return;

            gameStarted = true;
            gameStartTime = System.currentTimeMillis();

            System.out.println("[GameSession] Game started! Broadcasting progress updates...");

            // Main game loop - broadcast progress updates
            while (running && gameStarted) {
                broadcastProgress();

                // Check for game end conditions
                if (checkGameEnd()) {
                    break;
                }

                Thread.sleep(BROADCAST_INTERVAL_MS);
            }

        } catch (InterruptedException e) {
            System.out.println("[GameSession] Session interrupted: " + sessionId);
        } catch (Exception e) {
            System.err.println("[GameSession] Error in session " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            endGame();
        }
    }

    /**
     * Generate word list for the game.
     */
    private void generateWordList() {
        List<Word> words = typingEngine.getRandomWords(50);
        gameWords = words.stream()
                .map(Word::getText)
                .collect(Collectors.toList());

        System.out.println("[GameSession] Generated " + gameWords.size() + " words for the game");
    }

    /**
     * Wait for both players to send READY message.
     */
    private void waitForPlayersReady() throws InterruptedException {
        while (running && (!isPlayerReady(player1.getPlayerId()) || !isPlayerReady(player2.getPlayerId()))) {
            Thread.sleep(100);
        }
    }

    /**
     * Check if a player is ready.
     */
    private boolean isPlayerReady(String playerId) {
        return playerReady.getOrDefault(playerId, false);
    }

    /**
     * Called when a player sends READY message.
     */
    public void onPlayerReady(String playerId) {
        playerReady.put(playerId, true);
        System.out.println("[GameSession] Player " + playerId + " is ready");
    }

    /**
     * Called when a player sends progress update.
     */
    public void onProgressUpdate(String playerId, PlayerProgress progress) {
        playerProgress.put(playerId, progress);

        // Check for Elimination Mode attacks
        if (gameMode == GameMode.ELIMINATION && progress.getWpm() >= ELIMINATION_ATTACK_THRESHOLD) {
            if (!hasAttacked.get(playerId)) {
                performAttack(playerId);
            }
        }
    }

    /**
     * Handle word validation for health-based modes.
     * Only active if mode has health system.
     */
    public void onWordValidation(String playerId, String typedWord, String expectedWord) {
        // Only validate if mode has health system
        if (!gameMode.hasHealthSystem()) {
            return;
        }

        PlayerHealth health = playerHealthMap.get(playerId);
        if (health == null || health.isEliminated()) {
            return;
        }

        boolean correct = typedWord.trim().equalsIgnoreCase(expectedWord.trim());

        if (!correct) {
            // Wrong word - decrement health
            boolean eliminated = health.decrementHealth();

            // Send health update to both players
            ClientHandler player = playerId.equals(player1.getPlayerId()) ? player1 : player2;
            ClientHandler opponent = playerId.equals(player1.getPlayerId()) ? player2 : player1;

            GameMessage healthMsg = GameMessage.healthUpdate(
                    playerId,
                    health.getCurrentHealth(),
                    health.getMaxHealth(),
                    false);
            player.sendMessage(healthMsg);
            opponent.sendMessage(healthMsg); // Notify opponent too

            if (eliminated) {
                // Player eliminated - trigger game over
                handlePlayerElimination(playerId);
            }
        } else {
            // Correct word - send confirmation
            ClientHandler player = playerId.equals(player1.getPlayerId()) ? player1 : player2;
            GameMessage healthMsg = GameMessage.healthUpdate(
                    playerId,
                    health.getCurrentHealth(),
                    health.getMaxHealth(),
                    true);
            player.sendMessage(healthMsg);
        }
    }

    /**
     * Handle player elimination when health reaches 0.
     */
    private void handlePlayerElimination(String eliminatedPlayerId) {
        System.out.println("[GameSession] Player " + eliminatedPlayerId + " ELIMINATED (health = 0)");

        // Determine winner and loser
        ClientHandler eliminated = eliminatedPlayerId.equals(player1.getPlayerId()) ? player1 : player2;
        ClientHandler winner = eliminatedPlayerId.equals(player1.getPlayerId()) ? player2 : player1;

        // Send GAME_OVER to eliminated player
        GameMessage gameOverMsg = GameMessage.gameOver(
                eliminatedPlayerId,
                "You have been eliminated! No health remaining.");
        eliminated.sendMessage(gameOverMsg);

        // Send GAME_END to winner
        GameMessage winMsg = GameMessage.gameEnd(
                winner.getPlayerId(),
                winner.getPlayerName() + " wins! Opponent eliminated.");
        winner.sendMessage(winMsg);

        // End the game
        running = false;
        gameStarted = false;
    }

    /**
     * Broadcast current progress to both players.
     */
    private void broadcastProgress() {
        PlayerProgress p1Progress = playerProgress.get(player1.getPlayerId());
        PlayerProgress p2Progress = playerProgress.get(player2.getPlayerId());

        // Send opponent progress to each player
        if (p1Progress != null) {
            GameMessage msg = GameMessage.opponentProgress(player1.getPlayerId(), p1Progress);
            player2.sendMessage(msg);
        }

        if (p2Progress != null) {
            GameMessage msg = GameMessage.opponentProgress(player2.getPlayerId(), p2Progress);
            player1.sendMessage(msg);
        }
    }

    /**
     * Perform Elimination Mode attack.
     */
    private void performAttack(String attackerId) {
        hasAttacked.put(attackerId, true);

        // Get hard difficulty words for attack
        List<Word> hardWords = typingEngine.getWordsByDifficulty(Word.DifficultyLevel.HARD);
        List<String> attackWords = hardWords.stream()
                .limit(ATTACK_WORD_COUNT)
                .map(Word::getText)
                .collect(Collectors.toList());

        // Determine target player
        ClientHandler target = attackerId.equals(player1.getPlayerId()) ? player2 : player1;

        // Send attack message
        GameMessage attackMsg = GameMessage.attack(attackerId, attackWords);
        target.sendMessage(attackMsg);

        System.out.println("[GameSession] Player " + attackerId + " attacked with "
                + ATTACK_WORD_COUNT + " hard words!");
        System.out.println("  Attack words: " + attackWords);
    }

    /**
     * Check if game should end.
     */
    private boolean checkGameEnd() {
        PlayerProgress p1Progress = playerProgress.get(player1.getPlayerId());
        PlayerProgress p2Progress = playerProgress.get(player2.getPlayerId());

        if (p1Progress == null || p2Progress == null) {
            return false;
        }

        // Check if either player completed all words
        int totalWords = gameWords.size();
        if (p1Progress.getWordsCompleted() >= totalWords ||
                p2Progress.getWordsCompleted() >= totalWords) {
            return true;
        }

        // Check for timeout (5 minutes)
        long elapsed = System.currentTimeMillis() - gameStartTime;
        if (elapsed > 300000) { // 5 minutes
            System.out.println("[GameSession] Game timeout reached");
            return true;
        }

        return false;
    }

    /**
     * End the game and determine winner.
     * Uses PlayerScore Comparable for Vs Friend mode.
     */
    private void endGame() {
        gameStarted = false;

        PlayerProgress p1Progress = playerProgress.get(player1.getPlayerId());
        PlayerProgress p2Progress = playerProgress.get(player2.getPlayerId());

        String winnerId = null;
        String message = "Game ended";

        if (p1Progress != null && p2Progress != null) {
            // For Vs Friend mode, use Comparable PlayerScore
            if (gameMode == GameMode.VS_FRIEND) {
                PlayerScore score1 = new PlayerScore(
                        player1.getPlayerId(),
                        player1.getPlayerName(),
                        p1Progress.getWordsCompleted(),
                        p1Progress.getWpm(),
                        p1Progress.getAccuracy(),
                        System.currentTimeMillis() - gameStartTime);

                PlayerScore score2 = new PlayerScore(
                        player2.getPlayerId(),
                        player2.getPlayerName(),
                        p2Progress.getWordsCompleted(),
                        p2Progress.getWpm(),
                        p2Progress.getAccuracy(),
                        System.currentTimeMillis() - gameStartTime);

                // Use Comparable to determine winner
                WinnerResult result = PlayerScore.getWinnerWithReason(score1, score2);
                winnerId = result.getWinnerId();
                message = result.toString();

                System.out.println("[GameSession] Winner determined using Comparable: " + message);
            } else {
                // For other modes, use standard logic
                if (p1Progress.getWordsCompleted() > p2Progress.getWordsCompleted()) {
                    winnerId = player1.getPlayerId();
                    message = player1.getPlayerName() + " wins!";
                } else if (p2Progress.getWordsCompleted() > p1Progress.getWordsCompleted()) {
                    winnerId = player2.getPlayerId();
                    message = player2.getPlayerName() + " wins!";
                } else {
                    // Tie on words, check WPM
                    if (p1Progress.getWpm() > p2Progress.getWpm()) {
                        winnerId = player1.getPlayerId();
                        message = player1.getPlayerName() + " wins by WPM!";
                    } else if (p2Progress.getWpm() > p1Progress.getWpm()) {
                        winnerId = player2.getPlayerId();
                        message = player2.getPlayerName() + " wins by WPM!";
                    } else {
                        message = "It's a tie!";
                    }
                }
            }
        }

        // Send game end message to both players
        GameMessage endMsg = GameMessage.gameEnd(winnerId, message);
        player1.sendMessage(endMsg);
        player2.sendMessage(endMsg);

        System.out.println("[GameSession] " + message);
        System.out.println("  Player 1 (" + player1.getPlayerName() + "): "
                + (p1Progress != null ? p1Progress.getWordsCompleted() + " words, "
                        + String.format("%.1f", p1Progress.getWpm()) + " WPM" : "N/A"));
        System.out.println("  Player 2 (" + player2.getPlayerName() + "): "
                + (p2Progress != null ? p2Progress.getWordsCompleted() + " words, "
                        + String.format("%.1f", p2Progress.getWpm()) + " WPM" : "N/A"));

        // Cleanup
        shutdown();
    }

    /**
     * Called when a player disconnects.
     */
    public void onPlayerDisconnected(String playerId) {
        if (!gameStarted)
            return;

        System.out.println("[GameSession] Player " + playerId + " disconnected");

        // Determine other player
        ClientHandler otherPlayer = playerId.equals(player1.getPlayerId()) ? player2 : player1;

        // Notify other player and end game
        GameMessage msg = GameMessage.gameEnd(otherPlayer.getPlayerId(),
                "Opponent disconnected. You win!");
        otherPlayer.sendMessage(msg);

        running = false;
    }

    /**
     * Shutdown the game session.
     */
    public void shutdown() {
        running = false;
        gameStarted = false;
        System.out.println("[GameSession] Session " + sessionId + " ended");
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
