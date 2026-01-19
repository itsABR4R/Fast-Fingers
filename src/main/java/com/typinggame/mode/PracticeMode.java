package com.typinggame.mode;

import com.typinggame.domain.Word;
import com.typinggame.engine.TypingEngine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Practice Mode implementation.
 * Uses FileReader to load massive word list into ArrayList.
 * Tracks unique words typed using Set.
 */
public class PracticeMode {

    private final ArrayList<Word> wordList;
    private final Set<String> uniqueWordsTyped;
    private final TypingEngine typingEngine;

    private int currentWordIndex;
    private long startTime;
    private int correctKeystrokes;
    private int totalKeystrokes;

    public PracticeMode() {
        this.wordList = new ArrayList<>();
        this.uniqueWordsTyped = new HashSet<>();
        this.typingEngine = new TypingEngine();
        this.currentWordIndex = 0;
    }

    /**
     * Load massive word list from file using FileReader.
     * Demonstrates FileReader usage for Practice Mode.
     */
    public void loadWordsFromFile(String filePath) throws IOException {
        System.out.println("[PracticeMode] Loading words from: " + filePath);

        try (FileReader fileReader = new FileReader(filePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            int count = 0;

            while ((line = bufferedReader.readLine()) != null) {
                String trimmed = line.trim();

                // Skip empty lines and comments
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    wordList.add(new Word(trimmed));
                    count++;
                }
            }

            System.out.println("[PracticeMode] Loaded " + count + " words into ArrayList");
        }
    }

    /**
     * Load default word list from TypingEngine.
     */
    public void loadDefaultWords(int count) {
        wordList.clear();
        wordList.addAll(typingEngine.getRandomWords(count));
        System.out.println("[PracticeMode] Loaded " + wordList.size() + " default words");
    }

    /**
     * Start practice session.
     */
    public void startSession() {
        startTime = System.currentTimeMillis();
        currentWordIndex = 0;
        correctKeystrokes = 0;
        totalKeystrokes = 0;
        uniqueWordsTyped.clear();
        System.out.println("[PracticeMode] Session started - No time limit!");
    }

    /**
     * Record a correctly typed word.
     * Uses Set to track unique words.
     */
    public void recordCorrectWord(String word) {
        uniqueWordsTyped.add(word.toLowerCase());
        currentWordIndex++;
    }

    /**
     * Record keystroke.
     */
    public void recordKeystroke(boolean correct) {
        totalKeystrokes++;
        if (correct) {
            correctKeystrokes++;
        }
    }

    /**
     * Get current word.
     */
    public Word getCurrentWord() {
        if (currentWordIndex < wordList.size()) {
            return wordList.get(currentWordIndex);
        }
        return null;
    }

    /**
     * Get next word without advancing index.
     */
    public Word peekNextWord() {
        if (currentWordIndex + 1 < wordList.size()) {
            return wordList.get(currentWordIndex + 1);
        }
        return null;
    }

    /**
     * Calculate current WPM.
     */
    public double calculateWPM() {
        if (startTime == 0)
            return 0.0;

        long elapsed = System.currentTimeMillis() - startTime;
        double minutes = elapsed / 60000.0;

        if (minutes <= 0)
            return 0.0;

        // Standard: 5 characters = 1 word
        double words = correctKeystrokes / 5.0;
        return words / minutes;
    }

    /**
     * Calculate accuracy.
     */
    public double calculateAccuracy() {
        if (totalKeystrokes == 0)
            return 100.0;
        return (correctKeystrokes * 100.0) / totalKeystrokes;
    }

    /**
     * Get statistics.
     */
    public PracticeModeStats getStats() {
        return new PracticeModeStats(
                currentWordIndex,
                wordList.size(),
                uniqueWordsTyped.size(), // Unique words typed (Set)
                calculateWPM(),
                calculateAccuracy(),
                System.currentTimeMillis() - startTime);
    }

    // Getters
    public ArrayList<Word> getWordList() {
        return wordList;
    }

    public Set<String> getUniqueWordsTyped() {
        return new HashSet<>(uniqueWordsTyped);
    }

    public int getUniqueWordCount() {
        return uniqueWordsTyped.size();
    }

    public int getTotalWords() {
        return wordList.size();
    }

    public int getWordsCompleted() {
        return currentWordIndex;
    }

    /**
     * Statistics container for Practice Mode.
     */
    public static class PracticeModeStats {
        private final int wordsCompleted;
        private final int totalWords;
        private final int uniqueWordsTyped;
        private final double wpm;
        private final double accuracy;
        private final long duration;

        public PracticeModeStats(int wordsCompleted, int totalWords, int uniqueWordsTyped,
                double wpm, double accuracy, long duration) {
            this.wordsCompleted = wordsCompleted;
            this.totalWords = totalWords;
            this.uniqueWordsTyped = uniqueWordsTyped;
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.duration = duration;
        }

        public int getWordsCompleted() {
            return wordsCompleted;
        }

        public int getTotalWords() {
            return totalWords;
        }

        public int getUniqueWordsTyped() {
            return uniqueWordsTyped;
        }

        public double getWpm() {
            return wpm;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public long getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return String.format("Practice Stats: %d/%d words, %d unique, %.1f WPM, %.1f%% accuracy, %.1fs",
                    wordsCompleted, totalWords, uniqueWordsTyped, wpm, accuracy, duration / 1000.0);
        }
    }
}
