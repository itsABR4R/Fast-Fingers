package com.typinggame.engine;

import com.typinggame.domain.Keystroke;
import com.typinggame.domain.Word;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Tracks performance metrics for the typing game.
 * - Uses Stack to store keystroke history (for undo/backspace)
 * - Uses Queue to manage next 10 upcoming words
 * - Uses Set to track unique words typed correctly
 */
@Component
public class PerformanceTracker {

    // Stack for keystroke history (allows undo/backspace)
    private final Stack<Keystroke> keystrokeHistory;

    // Queue for upcoming words (next 10 words to type)
    private final Queue<Word> upcomingWords;

    // Set for unique correctly typed words
    private final Set<String> uniqueCorrectWords;

    // Performance metrics
    private int totalKeystrokes;
    private int correctKeystrokes;
    private int errorCount;
    private long sessionStartTime;
    private long sessionEndTime;
    private int wordsCompleted;

    public PerformanceTracker() {
        this.keystrokeHistory = new Stack<>();
        this.upcomingWords = new LinkedList<>();
        this.uniqueCorrectWords = new HashSet<>();
        resetMetrics();
    }

    /**
     * Reset all performance metrics.
     */
    public void resetMetrics() {
        keystrokeHistory.clear();
        upcomingWords.clear();
        uniqueCorrectWords.clear();
        totalKeystrokes = 0;
        correctKeystrokes = 0;
        errorCount = 0;
        wordsCompleted = 0;
        sessionStartTime = 0;
        sessionEndTime = 0;
    }

    /**
     * Start a new typing session.
     */
    public void startSession() {
        resetMetrics();
        sessionStartTime = System.currentTimeMillis();
    }

    /**
     * End the current typing session.
     */
    public void endSession() {
        sessionEndTime = System.currentTimeMillis();
    }

    /**
     * Record a keystroke using Stack.
     */
    public void recordKeystroke(char character, boolean correct, int position) {
        Keystroke keystroke = new Keystroke(character, System.currentTimeMillis(), correct, position);
        keystrokeHistory.push(keystroke);

        totalKeystrokes++;
        if (correct) {
            correctKeystrokes++;
        } else {
            errorCount++;
        }
    }

    /**
     * Undo the last keystroke (backspace functionality).
     * Uses Stack's pop operation.
     */
    public Keystroke undoLastKeystroke() {
        if (keystrokeHistory.isEmpty()) {
            return null;
        }

        Keystroke removed = keystrokeHistory.pop();
        totalKeystrokes--;

        if (removed.isCorrect()) {
            correctKeystrokes--;
        } else {
            errorCount--;
        }

        return removed;
    }

    /**
     * Peek at the last keystroke without removing it.
     */
    public Keystroke peekLastKeystroke() {
        if (keystrokeHistory.isEmpty()) {
            return null;
        }
        return keystrokeHistory.peek();
    }

    /**
     * Get the size of keystroke history.
     */
    public int getKeystrokeHistorySize() {
        return keystrokeHistory.size();
    }

    /**
     * Add words to the upcoming queue.
     * Queue maintains the next 10 words to type.
     */
    public void addUpcomingWords(List<Word> words) {
        for (Word word : words) {
            if (upcomingWords.size() < 10) {
                upcomingWords.offer(word);
            }
        }
    }

    /**
     * Get the next word from the queue without removing it.
     */
    public Word peekNextWord() {
        return upcomingWords.peek();
    }

    /**
     * Remove and return the next word from the queue.
     */
    public Word pollNextWord() {
        return upcomingWords.poll();
    }

    /**
     * Get all upcoming words as a list.
     */
    public List<Word> getUpcomingWordsList() {
        return new ArrayList<>(upcomingWords);
    }

    /**
     * Get the number of words in the upcoming queue.
     */
    public int getUpcomingWordsCount() {
        return upcomingWords.size();
    }

    /**
     * Refill the upcoming words queue if it's running low.
     */
    public void refillUpcomingWords(List<Word> newWords) {
        while (upcomingWords.size() < 10 && !newWords.isEmpty()) {
            upcomingWords.offer(newWords.remove(0));
        }
    }

    /**
     * Record a correctly typed word using Set (ensures uniqueness).
     */
    public void recordCorrectWord(String word) {
        uniqueCorrectWords.add(word.toLowerCase());
        wordsCompleted++;
    }

    /**
     * Get the count of unique words typed correctly.
     */
    public int getUniqueCorrectWordsCount() {
        return uniqueCorrectWords.size();
    }

    /**
     * Get all unique correct words.
     */
    public Set<String> getUniqueCorrectWords() {
        return new HashSet<>(uniqueCorrectWords);
    }

    /**
     * Check if a word has been typed correctly before.
     */
    public boolean hasTypedWordBefore(String word) {
        return uniqueCorrectWords.contains(word.toLowerCase());
    }

    /**
     * Calculate current WPM (Words Per Minute).
     */
    public double calculateCurrentWPM() {
        long currentTime = sessionEndTime > 0 ? sessionEndTime : System.currentTimeMillis();
        long elapsedTime = currentTime - sessionStartTime;

        if (elapsedTime <= 0) {
            return 0.0;
        }

        // Standard: 5 characters = 1 word
        double words = correctKeystrokes / 5.0;
        double minutes = elapsedTime / 60000.0;

        return minutes > 0 ? words / minutes : 0.0;
    }

    /**
     * Calculate accuracy percentage.
     */
    public double calculateAccuracy() {
        if (totalKeystrokes == 0) {
            return 0.0;
        }
        return (correctKeystrokes * 100.0) / totalKeystrokes;
    }

    /**
     * Get session duration in milliseconds.
     */
    public long getSessionDuration() {
        if (sessionStartTime == 0) {
            return 0;
        }
        long endTime = sessionEndTime > 0 ? sessionEndTime : System.currentTimeMillis();
        return endTime - sessionStartTime;
    }

    /**
     * Get a summary of the performance metrics.
     */
    public PerformanceMetrics getMetrics() {
        return new PerformanceMetrics(
                totalKeystrokes,
                correctKeystrokes,
                errorCount,
                calculateAccuracy(),
                calculateCurrentWPM(),
                wordsCompleted,
                uniqueCorrectWords.size(),
                getSessionDuration());
    }

    // Getters
    public int getTotalKeystrokes() {
        return totalKeystrokes;
    }

    public int getCorrectKeystrokes() {
        return correctKeystrokes;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWordsCompleted() {
        return wordsCompleted;
    }

    /**
     * Inner class to encapsulate performance metrics.
     */
    public static class PerformanceMetrics {
        private final int totalKeystrokes;
        private final int correctKeystrokes;
        private final int errors;
        private final double accuracy;
        private final double wpm;
        private final int wordsCompleted;
        private final int uniqueWordsTyped;
        private final long sessionDuration;

        public PerformanceMetrics(int totalKeystrokes, int correctKeystrokes, int errors,
                double accuracy, double wpm, int wordsCompleted,
                int uniqueWordsTyped, long sessionDuration) {
            this.totalKeystrokes = totalKeystrokes;
            this.correctKeystrokes = correctKeystrokes;
            this.errors = errors;
            this.accuracy = accuracy;
            this.wpm = wpm;
            this.wordsCompleted = wordsCompleted;
            this.uniqueWordsTyped = uniqueWordsTyped;
            this.sessionDuration = sessionDuration;
        }

        // Getters
        public int getTotalKeystrokes() {
            return totalKeystrokes;
        }

        public int getCorrectKeystrokes() {
            return correctKeystrokes;
        }

        public int getErrors() {
            return errors;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public double getWpm() {
            return wpm;
        }

        public int getWordsCompleted() {
            return wordsCompleted;
        }

        public int getUniqueWordsTyped() {
            return uniqueWordsTyped;
        }

        public long getSessionDuration() {
            return sessionDuration;
        }
    }
}
