package com.typinggame.io;

import com.typinggame.engine.TypingEngine;

import java.io.IOException;

/**
 * Demo application to test File I/O operations.
 * Demonstrates ObjectOutputStream/ObjectInputStream and FileWriter/FileReader.
 */
public class FileIODemo {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("File I/O Demo - Typing Game");
        System.out.println("===========================================\n");

        ScoreManager scoreManager = new ScoreManager();

        // Test 1: Create and save user stats
        System.out.println("--- Test 1: ObjectOutputStream (Serialization) ---");
        UserStats stats = new UserStats("Alice");

        // Add some game records
        stats.addGameRecord(45.5, 96.2, 25, 33000, "PRACTICE");
        stats.addGameRecord(52.3, 94.8, 30, 34500, "VS_FRIEND");
        stats.addGameRecord(68.7, 97.5, 35, 30500, "ELIMINATION");
        stats.addGameRecord(55.0, 95.0, 28, 30600, "PRACTICE");

        try {
            scoreManager.saveStats(stats);
            System.out.println("✓ Stats saved successfully");
            System.out.println("  File: " + scoreManager.getStatsFilePath());
            System.out.println("  " + stats);
        } catch (IOException e) {
            System.err.println("✗ Failed to save stats: " + e.getMessage());
        }

        System.out.println();

        // Test 2: Load user stats
        System.out.println("--- Test 2: ObjectInputStream (Deserialization) ---");
        try {
            UserStats loadedStats = scoreManager.loadStats();
            if (loadedStats != null) {
                System.out.println("✓ Stats loaded successfully");
                System.out.println("  " + loadedStats);
                System.out.println("  Games played: " + loadedStats.getTotalGamesPlayed());
                System.out.println("  Best WPM: " + loadedStats.getBestWPM());
                System.out.println("  Average WPM: " + String.format("%.2f", loadedStats.getAverageWPM()));
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("✗ Failed to load stats: " + e.getMessage());
        }

        System.out.println();

        // Test 3: Export last game to text file
        System.out.println("--- Test 3: FileWriter (Export Last Game) ---");
        try {
            scoreManager.exportLastGame(stats);
            System.out.println("✓ Last game exported successfully");
            System.out.println("  File: " + scoreManager.getMatchHistoryFilePath());
        } catch (IOException e) {
            System.err.println("✗ Failed to export last game: " + e.getMessage());
        }

        System.out.println();

        // Test 4: Export full history
        System.out.println("--- Test 4: FileWriter (Export Full History) ---");
        try {
            scoreManager.exportFullHistory(stats);
            System.out.println("✓ Full history exported successfully");
        } catch (IOException e) {
            System.err.println("✗ Failed to export full history: " + e.getMessage());
        }

        System.out.println();

        // Test 5: Read match history
        System.out.println("--- Test 5: FileReader (Read Match History) ---");
        try {
            String history = scoreManager.readMatchHistory();
            System.out.println("✓ Match history read successfully");
            System.out.println("\n" + history);
        } catch (IOException e) {
            System.err.println("✗ Failed to read match history: " + e.getMessage());
        }

        // Test 6: Load custom word list
        System.out.println("--- Test 6: FileInputStream (Load Custom Words) ---");
        TypingEngine engine = new TypingEngine();
        System.out.println("Initial word bank size: " + engine.getWordBankSize());

        try {
            int wordsAdded = engine.loadCustomWordList("custom_words.txt");
            System.out.println("✓ Custom word list loaded successfully");
            System.out.println("  Words added: " + wordsAdded);
            System.out.println("  Total words: " + engine.getWordBankSize());
        } catch (IOException e) {
            System.err.println("✗ Failed to load custom word list: " + e.getMessage());
            System.err.println("  Make sure 'custom_words.txt' exists in the project directory");
        }

        System.out.println();
        System.out.println("===========================================");
        System.out.println("File I/O Demo Complete!");
        System.out.println("===========================================");

        // Show file locations
        System.out.println("\nGenerated Files:");
        System.out.println("  1. " + scoreManager.getStatsFilePath());
        System.out.println("  2. " + scoreManager.getMatchHistoryFilePath());
    }
}
