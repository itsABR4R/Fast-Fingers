package com.typinggame.io;

import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Manages saving and loading user statistics and match history.
 * Uses ObjectOutputStream/ObjectInputStream for binary serialization
 * and FileWriter/FileReader for human-readable text files.
 */
@Component
public class ScoreManager {

    private static final String STATS_FILE = "scores.dat";
    private static final String MATCH_HISTORY_FILE = "match_history.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Save UserStats to binary file using ObjectOutputStream.
     */
    public void saveStats(UserStats stats) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(STATS_FILE))) {
            oos.writeObject(stats);
            System.out.println("[ScoreManager] Stats saved to " + STATS_FILE);
        }
    }

    /**
     * Load UserStats from binary file using ObjectInputStream.
     */
    public UserStats loadStats() throws IOException, ClassNotFoundException {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            System.out.println("[ScoreManager] No stats file found, creating new stats");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(STATS_FILE))) {
            UserStats stats = (UserStats) ois.readObject();
            System.out.println("[ScoreManager] Stats loaded from " + STATS_FILE);
            return stats;
        }
    }

    /**
     * Export the last game summary to human-readable text file using FileWriter.
     */
    public void exportLastGame(UserStats stats) throws IOException {
        if (stats == null || stats.getLastGame() == null) {
            System.out.println("[ScoreManager] No game to export");
            return;
        }

        UserStats.GameRecord lastGame = stats.getLastGame();

        try (FileWriter writer = new FileWriter(MATCH_HISTORY_FILE, true)) {
            // Append mode to keep history
            writer.write("=".repeat(60) + "\n");
            writer.write("Match Summary - " + DATE_FORMAT.format(new Date(lastGame.getTimestamp())) + "\n");
            writer.write("=".repeat(60) + "\n");
            writer.write(String.format("Player: %s\n", stats.getUsername()));
            writer.write(String.format("Game Mode: %s\n", lastGame.getGameMode()));
            writer.write(String.format("WPM: %.2f\n", lastGame.getWpm()));
            writer.write(String.format("Accuracy: %.2f%%\n", lastGame.getAccuracy()));
            writer.write(String.format("Words Typed: %d\n", lastGame.getWordsTyped()));
            writer.write(String.format("Time: %.2f seconds\n", lastGame.getDuration() / 1000.0));
            writer.write("\n");

            System.out.println("[ScoreManager] Last game exported to " + MATCH_HISTORY_FILE);
        }
    }

    /**
     * Export complete match history to text file using FileWriter.
     */
    public void exportFullHistory(UserStats stats) throws IOException {
        if (stats == null) {
            System.out.println("[ScoreManager] No stats to export");
            return;
        }

        try (FileWriter writer = new FileWriter(MATCH_HISTORY_FILE, false)) {
            // Overwrite mode for full history
            writer.write("╔" + "═".repeat(58) + "╗\n");
            writer.write("║" + centerText("TYPING GAME MATCH HISTORY", 58) + "║\n");
            writer.write("╠" + "═".repeat(58) + "╣\n");
            writer.write("║ Player: " + String.format("%-47s", stats.getUsername()) + "║\n");
            writer.write("║ Total Games: " + String.format("%-43d", stats.getTotalGamesPlayed()) + "║\n");
            writer.write("║ Best WPM: " + String.format("%-46.2f", stats.getBestWPM()) + "║\n");
            writer.write("║ Average WPM: " + String.format("%-43.2f", stats.getAverageWPM()) + "║\n");
            writer.write("║ Best Accuracy: " + String.format("%-40.2f%%", stats.getBestAccuracy()) + "║\n");
            writer.write("║ Average Accuracy: " + String.format("%-37.2f%%", stats.getAverageAccuracy()) + "║\n");
            writer.write("╚" + "═".repeat(58) + "╝\n\n");

            List<UserStats.GameRecord> history = stats.getGameHistory();
            for (int i = 0; i < history.size(); i++) {
                UserStats.GameRecord game = history.get(i);
                writer.write(String.format("Game #%d - %s\n", i + 1,
                        DATE_FORMAT.format(new Date(game.getTimestamp()))));
                writer.write("-".repeat(60) + "\n");
                writer.write(String.format("  Mode: %s\n", game.getGameMode()));
                writer.write(String.format("  WPM: %.2f | Accuracy: %.2f%% | Words: %d | Time: %.2fs\n",
                        game.getWpm(), game.getAccuracy(), game.getWordsTyped(),
                        game.getDuration() / 1000.0));
                writer.write("\n");
            }

            System.out.println("[ScoreManager] Full history exported to " + MATCH_HISTORY_FILE);
        }
    }

    /**
     * Read match history from text file using FileReader.
     */
    public String readMatchHistory() throws IOException {
        File file = new File(MATCH_HISTORY_FILE);
        if (!file.exists()) {
            return "No match history found.";
        }

        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(MATCH_HISTORY_FILE);
                BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    /**
     * Helper method to center text.
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    /**
     * Delete stats file.
     */
    public boolean deleteStats() {
        File file = new File(STATS_FILE);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("[ScoreManager] Stats file deleted");
            }
            return deleted;
        }
        return false;
    }

    /**
     * Delete match history file.
     */
    public boolean deleteMatchHistory() {
        File file = new File(MATCH_HISTORY_FILE);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("[ScoreManager] Match history file deleted");
            }
            return deleted;
        }
        return false;
    }

    /**
     * Check if stats file exists.
     */
    public boolean statsFileExists() {
        return new File(STATS_FILE).exists();
    }

    /**
     * Check if match history file exists.
     */
    public boolean matchHistoryExists() {
        return new File(MATCH_HISTORY_FILE).exists();
    }

    /**
     * Get stats file path.
     */
    public String getStatsFilePath() {
        return new File(STATS_FILE).getAbsolutePath();
    }

    /**
     * Get match history file path.
     */
    public String getMatchHistoryFilePath() {
        return new File(MATCH_HISTORY_FILE).getAbsolutePath();
    }
}
