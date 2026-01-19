package com.typinggame.bot;

import com.typinggame.domain.Word;

import java.util.List;
import java.util.Random;

/**
 * Bot player that implements Runnable to simulate typing in a separate thread.
 * Simulates typing at a constant WPM (e.g., 60 WPM).
 */
public class BotPlayer implements Runnable {

    private final String botName;
    private final List<Word> wordList;
    private final GameStatus gameStatus;
    private final double targetWPM;
    private final Random random;

    private volatile boolean running = true;

    /**
     * Create a bot player.
     * 
     * @param botName    Name of the bot
     * @param wordList   Words to type
     * @param gameStatus Shared game status
     * @param targetWPM  Target WPM for the bot (e.g., 60)
     */
    public BotPlayer(String botName, List<Word> wordList, GameStatus gameStatus, double targetWPM) {
        this.botName = botName;
        this.wordList = wordList;
        this.gameStatus = gameStatus;
        this.targetWPM = targetWPM;
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("[BotPlayer] " + botName + " started (Target WPM: " + targetWPM + ")");

        // Wait for game to start
        while (!gameStatus.isGameStarted() && running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        int wordsCompleted = 0;
        long startTime = System.currentTimeMillis();

        // Type each word
        for (Word word : wordList) {
            if (!running || gameStatus.isGameEnded()) {
                break;
            }

            // Calculate time to type this word at target WPM
            long timeToTypeWord = calculateTimeToTypeWord(word, targetWPM);

            // Add slight randomness (±10%) to make it more realistic
            long actualTime = (long) (timeToTypeWord * (0.9 + random.nextDouble() * 0.2));

            try {
                Thread.sleep(actualTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Update progress
            wordsCompleted++;
            long elapsed = System.currentTimeMillis() - startTime;
            double currentWPM = calculateCurrentWPM(wordsCompleted, elapsed);

            gameStatus.updateBotProgress(wordsCompleted, currentWPM);

            System.out.println("[BotPlayer] " + botName + " typed: \"" + word.getText() +
                    "\" (" + wordsCompleted + "/" + wordList.size() +
                    ", WPM: " + String.format("%.1f", currentWPM) + ")");
        }

        System.out.println("[BotPlayer] " + botName + " finished! Final WPM: " +
                String.format("%.1f", gameStatus.getBotWPM()));
    }

    /**
     * Calculate time needed to type a word at target WPM.
     * Formula: (characters / 5) / (WPM / 60) * 1000 = milliseconds
     */
    private long calculateTimeToTypeWord(Word word, double wpm) {
        // Standard: 5 characters = 1 word
        double words = word.getLength() / 5.0;
        double minutes = words / wpm;
        return (long) (minutes * 60 * 1000);
    }

    /**
     * Calculate current WPM based on words completed and time elapsed.
     */
    private double calculateCurrentWPM(int wordsCompleted, long elapsedMillis) {
        if (elapsedMillis <= 0) {
            return 0.0;
        }
        double minutes = elapsedMillis / 60000.0;
        return wordsCompleted / minutes;
    }

    /**
     * Stop the bot.
     */
    public void stop() {
        running = false;
    }

    public String getBotName() {
        return botName;
    }

    public double getTargetWPM() {
        return targetWPM;
    }
}
