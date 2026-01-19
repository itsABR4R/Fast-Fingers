package com.typinggame.engine;

import com.typinggame.domain.Word;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Core typing game engine that manages the word bank and game logic.
 * Uses ArrayList to store the word bank.
 */
@Component
public class TypingEngine {
    private final ArrayList<Word> wordBank;
    private final Random random;

    public TypingEngine() {
        this.wordBank = new ArrayList<>();
        this.random = new Random();
        loadWordsFromFile();
    }

    /**
     * Load words from the resources/words.txt file.
     */
    private void loadWordsFromFile() {
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("words.txt")) {

            if (inputStream == null) {
                throw new RuntimeException("words.txt file not found in resources");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        wordBank.add(new Word(trimmed));
                    }
                }
            }

            // Sort the word bank using Word's Comparable implementation
            Collections.sort(wordBank);

            System.out.println("Loaded " + wordBank.size() + " words into word bank");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load words from file", e);
        }
    }

    /**
     * Load custom word list from external file using FileInputStream.
     * This allows users to provide their own word lists.
     * 
     * @param filePath Path to the custom word list file
     * @return Number of words loaded
     * @throws IOException if file cannot be read
     */
    public int loadCustomWordList(String filePath) throws IOException {
        int initialSize = wordBank.size();

        try (FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Skip empty lines and comments
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    try {
                        wordBank.add(new Word(trimmed));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping invalid word: " + trimmed);
                    }
                }
            }

            // Re-sort the word bank after adding new words
            Collections.sort(wordBank);

            int wordsAdded = wordBank.size() - initialSize;
            System.out.println("[TypingEngine] Loaded " + wordsAdded + " words from custom file: " + filePath);
            System.out.println("[TypingEngine] Total words in bank: " + wordBank.size());

            return wordsAdded;
        }
    }

    /**
     * Replace the entire word bank with words from a custom file.
     * 
     * @param filePath Path to the custom word list file
     * @return Number of words loaded
     * @throws IOException if file cannot be read
     */
    public int replaceWordBankFromFile(String filePath) throws IOException {
        wordBank.clear();
        return loadCustomWordList(filePath);
    }

    /**
     * Get all words from the word bank.
     */
    public ArrayList<Word> getAllWords() {
        return new ArrayList<>(wordBank);
    }

    /**
     * Get a random selection of words from the word bank.
     * Uses Collections.shuffle() to randomize and subList() to get exact count.
     * AOOP Requirement 2: Demonstrates ArrayList usage with Collections framework.
     * 
     * @param count Number of words to retrieve
     * @return List of random words
     */
    public List<Word> getRandomWords(int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }

        // Ensure we don't request more words than available
        int actualCount = Math.min(count, wordBank.size());

        // Create a copy of the word bank to shuffle (AOOP Req 2)
        ArrayList<Word> shuffledWords = new ArrayList<>(wordBank);

        // Use Collections.shuffle() to randomize the list (AOOP Req 2)
        Collections.shuffle(shuffledWords);

        // Use subList() to get exactly the requested number of words (AOOP Req 2)
        List<Word> selectedWords = new ArrayList<>(shuffledWords.subList(0, actualCount));

        System.out.println(
                "[TypingEngine] Returning " + selectedWords.size() + " random words using Collections.shuffle()");

        return selectedWords;
    }

    /**
     * Get words filtered by difficulty level.
     */
    public List<Word> getWordsByDifficulty(Word.DifficultyLevel difficulty) {
        return wordBank.stream()
                .filter(word -> word.getDifficulty() == difficulty)
                .collect(Collectors.toList());
    }

    /**
     * Get words within a specific length range.
     */
    public List<Word> getWordsByLengthRange(int minLength, int maxLength) {
        return wordBank.stream()
                .filter(word -> word.getLength() >= minLength && word.getLength() <= maxLength)
                .collect(Collectors.toList());
    }

    /**
     * Validate if a typed word matches the expected word.
     */
    public boolean validateWord(String typed, Word expected) {
        if (typed == null || expected == null) {
            return false;
        }
        return typed.trim().equalsIgnoreCase(expected.getText());
    }

    /**
     * Calculate Words Per Minute (WPM).
     */
    public double calculateWPM(int charactersTyped, long timeInMillis) {
        if (timeInMillis <= 0) {
            return 0.0;
        }
        // Standard: 5 characters = 1 word
        double words = charactersTyped / 5.0;
        double minutes = timeInMillis / 60000.0;
        return minutes > 0 ? words / minutes : 0.0;
    }

    /**
     * Calculate accuracy percentage.
     */
    public double calculateAccuracy(int correctChars, int totalChars) {
        if (totalChars <= 0) {
            return 0.0;
        }
        return (correctChars * 100.0) / totalChars;
    }

    /**
     * Get the total number of words in the word bank.
     */
    public int getWordBankSize() {
        return wordBank.size();
    }

    /**
     * Shuffle the word bank.
     */
    public void shuffleWordBank() {
        Collections.shuffle(wordBank, random);
    }
}
