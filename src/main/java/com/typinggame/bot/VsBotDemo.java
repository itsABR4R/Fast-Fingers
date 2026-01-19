package com.typinggame.bot;

import com.typinggame.domain.Word;
import com.typinggame.engine.TypingEngine;
import com.typinggame.bot.WPMComparator.PlayerResult;

import java.util.List;
import java.util.Scanner;

/**
 * Demo application for Vs Bot mode.
 * Demonstrates thread-based bot player with thread-safe GameStatus.
 */
public class VsBotDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===========================================");
        System.out.println("Vs Bot Mode Demo");
        System.out.println("===========================================\n");

        Scanner scanner = new Scanner(System.in);

        // Setup
        TypingEngine engine = new TypingEngine();
        int wordCount = 20;
        double botWPM = 60.0;

        System.out.println("Configuration:");
        System.out.println("  Words to type: " + wordCount);
        System.out.println("  Bot WPM: " + botWPM);
        System.out.println();

        // Get word list
        List<Word> wordList = engine.getRandomWords(wordCount);
        System.out.println("Word list generated: " + wordList.size() + " words");
        System.out.println("First 5 words: " + wordList.subList(0, Math.min(5, wordList.size())));
        System.out.println();

        // Create shared game status
        GameStatus gameStatus = new GameStatus(wordCount);

        // Create bot player
        BotPlayer bot = new BotPlayer("SpeedBot", wordList, gameStatus, botWPM);

        // Start bot thread
        Thread botThread = new Thread(bot);
        botThread.setName("BotPlayer-Thread");

        System.out.println("Press ENTER to start the race...");
        scanner.nextLine();

        // Start game
        gameStatus.startGame();
        botThread.start();

        System.out.println("\n=== GAME STARTED ===\n");
        System.out.println("Bot is typing...");
        System.out.println("(In a real game, user would type here)\n");

        // Simulate user typing (for demo purposes)
        simulateUserTyping(gameStatus, wordList);

        // Wait for bot to finish
        botThread.join();

        // End game
        gameStatus.endGame();

        // Display results
        displayResults(gameStatus);

        scanner.close();
    }

    /**
     * Simulate user typing for demo purposes.
     */
    private static void simulateUserTyping(GameStatus gameStatus, List<Word> wordList)
            throws InterruptedException {
        System.out.println("[Simulating user typing at 50 WPM...]\n");

        long startTime = System.currentTimeMillis();
        int wordsCompleted = 0;

        for (Word word : wordList) {
            if (gameStatus.isGameEnded()) {
                break;
            }

            // Simulate typing time (slower than bot)
            long timePerWord = (long) ((word.getLength() / 5.0) / 50.0 * 60 * 1000);
            Thread.sleep(timePerWord);

            wordsCompleted++;
            long elapsed = System.currentTimeMillis() - startTime;
            double wpm = (wordsCompleted / (elapsed / 60000.0));
            double accuracy = 95.0 + Math.random() * 5; // 95-100%

            gameStatus.updateUserProgress(wordsCompleted, wpm, accuracy);

            System.out.println("[User] Typed: \"" + word.getText() + "\" (" +
                    wordsCompleted + "/" + wordList.size() +
                    ", WPM: " + String.format("%.1f", wpm) + ")");
        }
    }

    /**
     * Display final results and determine winner.
     */
    private static void displayResults(GameStatus gameStatus) {
        System.out.println("\n===========================================");
        System.out.println("GAME OVER - Results");
        System.out.println("===========================================\n");

        // Create player results
        PlayerResult userResult = new PlayerResult(
                "User",
                gameStatus.getUserWordsCompleted(),
                gameStatus.getUserWPM(),
                gameStatus.getUserAccuracy(),
                gameStatus.getUserDuration());

        PlayerResult botResult = new PlayerResult(
                "Bot",
                gameStatus.getBotWordsCompleted(),
                gameStatus.getBotWPM(),
                100.0, // Bot is perfect
                gameStatus.getBotDuration());

        System.out.println("User Stats:");
        System.out.println("  " + userResult);
        System.out.println();

        System.out.println("Bot Stats:");
        System.out.println("  " + botResult);
        System.out.println();

        // Use Comparator to determine winner
        WPMComparator comparator = new WPMComparator();
        String winner = comparator.determineWinner(userResult, botResult);

        System.out.println("===========================================");
        if (winner.equals("USER")) {
            System.out.println("🎉 YOU WIN! 🎉");
        } else if (winner.equals("BOT")) {
            System.out.println("🤖 BOT WINS! 🤖");
        } else {
            System.out.println("🤝 IT'S A TIE! 🤝");
        }
        System.out.println("===========================================");
    }
}
