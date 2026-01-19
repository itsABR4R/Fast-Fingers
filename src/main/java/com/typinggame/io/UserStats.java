package com.typinggame.io;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable class to store user statistics.
 * Can be saved/loaded using ObjectOutputStream/ObjectInputStream.
 */
public class UserStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private int totalGamesPlayed;
    private int totalWordsTyped;
    private double bestWPM;
    private double averageWPM;
    private double bestAccuracy;
    private double averageAccuracy;
    private long totalTimeSpent; // in milliseconds
    private List<GameRecord> gameHistory;

    public UserStats(String username) {
        this.username = username;
        this.totalGamesPlayed = 0;
        this.totalWordsTyped = 0;
        this.bestWPM = 0.0;
        this.averageWPM = 0.0;
        this.bestAccuracy = 0.0;
        this.averageAccuracy = 0.0;
        this.totalTimeSpent = 0;
        this.gameHistory = new ArrayList<>();
    }

    /**
     * Record for a single game session.
     */
    public static class GameRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        private final double wpm;
        private final double accuracy;
        private final int wordsTyped;
        private final long duration; // in milliseconds
        private final long timestamp;
        private final String gameMode;

        public GameRecord(double wpm, double accuracy, int wordsTyped,
                long duration, String gameMode) {
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.wordsTyped = wordsTyped;
            this.duration = duration;
            this.timestamp = System.currentTimeMillis();
            this.gameMode = gameMode;
        }

        // Getters
        public double getWpm() {
            return wpm;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public int getWordsTyped() {
            return wordsTyped;
        }

        public long getDuration() {
            return duration;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getGameMode() {
            return gameMode;
        }

        @Override
        public String toString() {
            return String.format("WPM: %.1f | Accuracy: %.1f%% | Words: %d | Time: %.1fs | Mode: %s",
                    wpm, accuracy, wordsTyped, duration / 1000.0, gameMode);
        }
    }

    /**
     * Add a game record and update statistics.
     */
    public void addGameRecord(double wpm, double accuracy, int wordsTyped,
            long duration, String gameMode) {
        GameRecord record = new GameRecord(wpm, accuracy, wordsTyped, duration, gameMode);
        gameHistory.add(record);

        totalGamesPlayed++;
        totalWordsTyped += wordsTyped;
        totalTimeSpent += duration;

        // Update best scores
        if (wpm > bestWPM) {
            bestWPM = wpm;
        }
        if (accuracy > bestAccuracy) {
            bestAccuracy = accuracy;
        }

        // Recalculate averages
        double totalWPM = 0;
        double totalAccuracy = 0;
        for (GameRecord gr : gameHistory) {
            totalWPM += gr.getWpm();
            totalAccuracy += gr.getAccuracy();
        }
        averageWPM = totalWPM / totalGamesPlayed;
        averageAccuracy = totalAccuracy / totalGamesPlayed;
    }

    /**
     * Get the most recent game record.
     */
    public GameRecord getLastGame() {
        if (gameHistory.isEmpty()) {
            return null;
        }
        return gameHistory.get(gameHistory.size() - 1);
    }

    /**
     * Get all game records.
     */
    public List<GameRecord> getGameHistory() {
        return new ArrayList<>(gameHistory);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public int getTotalWordsTyped() {
        return totalWordsTyped;
    }

    public double getBestWPM() {
        return bestWPM;
    }

    public double getAverageWPM() {
        return averageWPM;
    }

    public double getBestAccuracy() {
        return bestAccuracy;
    }

    public double getAverageAccuracy() {
        return averageAccuracy;
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }

    @Override
    public String toString() {
        return String.format(
                "UserStats{username='%s', games=%d, bestWPM=%.1f, avgWPM=%.1f, bestAcc=%.1f%%, avgAcc=%.1f%%}",
                username, totalGamesPlayed, bestWPM, averageWPM, bestAccuracy, averageAccuracy);
    }
}
