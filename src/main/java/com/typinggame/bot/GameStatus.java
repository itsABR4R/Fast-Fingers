package com.typinggame.bot;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe shared game status for Vs Bot mode.
 * Uses ReentrantLock for synchronization.
 */
public class GameStatus {

    private final ReentrantLock lock = new ReentrantLock();

    private volatile boolean gameStarted = false;
    private volatile boolean gameEnded = false;
    private volatile String winner = null;

    // User stats
    private int userWordsCompleted = 0;
    private double userWPM = 0.0;
    private double userAccuracy = 0.0;
    private long userStartTime = 0;
    private long userEndTime = 0;

    // Bot stats
    private int botWordsCompleted = 0;
    private double botWPM = 0.0;
    private double botAccuracy = 100.0; // Bot is perfect
    private long botStartTime = 0;
    private long botEndTime = 0;

    private final int totalWords;

    public GameStatus(int totalWords) {
        this.totalWords = totalWords;
    }

    /**
     * Start the game for both players.
     */
    public void startGame() {
        lock.lock();
        try {
            gameStarted = true;
            long now = System.currentTimeMillis();
            userStartTime = now;
            botStartTime = now;
            System.out.println("[GameStatus] Game started!");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Update user progress (thread-safe).
     */
    public void updateUserProgress(int wordsCompleted, double wpm, double accuracy) {
        lock.lock();
        try {
            this.userWordsCompleted = wordsCompleted;
            this.userWPM = wpm;
            this.userAccuracy = accuracy;

            if (wordsCompleted >= totalWords && userEndTime == 0) {
                userEndTime = System.currentTimeMillis();
                checkGameEnd();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Update bot progress (thread-safe).
     */
    public void updateBotProgress(int wordsCompleted, double wpm) {
        lock.lock();
        try {
            this.botWordsCompleted = wordsCompleted;
            this.botWPM = wpm;

            if (wordsCompleted >= totalWords && botEndTime == 0) {
                botEndTime = System.currentTimeMillis();
                checkGameEnd();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if game should end (both completed or one finished).
     */
    private void checkGameEnd() {
        if (!gameEnded && (userWordsCompleted >= totalWords || botWordsCompleted >= totalWords)) {
            gameEnded = true;
            determineWinner();
        }
    }

    /**
     * Determine the winner based on completion and WPM.
     */
    private void determineWinner() {
        if (userWordsCompleted >= totalWords && botWordsCompleted >= totalWords) {
            // Both completed - compare WPM
            winner = userWPM > botWPM ? "USER" : (userWPM < botWPM ? "BOT" : "TIE");
        } else if (userWordsCompleted >= totalWords) {
            winner = "USER";
        } else if (botWordsCompleted >= totalWords) {
            winner = "BOT";
        }

        System.out.println("[GameStatus] Game ended! Winner: " + winner);
    }

    /**
     * Force end the game.
     */
    public void endGame() {
        lock.lock();
        try {
            if (!gameEnded) {
                gameEnded = true;
                if (userEndTime == 0)
                    userEndTime = System.currentTimeMillis();
                if (botEndTime == 0)
                    botEndTime = System.currentTimeMillis();
                determineWinner();
            }
        } finally {
            lock.unlock();
        }
    }

    // Thread-safe getters
    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public String getWinner() {
        lock.lock();
        try {
            return winner;
        } finally {
            lock.unlock();
        }
    }

    public int getUserWordsCompleted() {
        lock.lock();
        try {
            return userWordsCompleted;
        } finally {
            lock.unlock();
        }
    }

    public double getUserWPM() {
        lock.lock();
        try {
            return userWPM;
        } finally {
            lock.unlock();
        }
    }

    public double getUserAccuracy() {
        lock.lock();
        try {
            return userAccuracy;
        } finally {
            lock.unlock();
        }
    }

    public int getBotWordsCompleted() {
        lock.lock();
        try {
            return botWordsCompleted;
        } finally {
            lock.unlock();
        }
    }

    public double getBotWPM() {
        lock.lock();
        try {
            return botWPM;
        } finally {
            lock.unlock();
        }
    }

    public int getTotalWords() {
        return totalWords;
    }

    public long getUserDuration() {
        lock.lock();
        try {
            return userEndTime > 0 ? userEndTime - userStartTime : System.currentTimeMillis() - userStartTime;
        } finally {
            lock.unlock();
        }
    }

    public long getBotDuration() {
        lock.lock();
        try {
            return botEndTime > 0 ? botEndTime - botStartTime : System.currentTimeMillis() - botStartTime;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return String.format("GameStatus{user: %d/%d words, %.1f WPM | bot: %d/%d words, %.1f WPM | winner: %s}",
                    userWordsCompleted, totalWords, userWPM,
                    botWordsCompleted, totalWords, botWPM,
                    winner != null ? winner : "IN_PROGRESS");
        } finally {
            lock.unlock();
        }
    }
}
