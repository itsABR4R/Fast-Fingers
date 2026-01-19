package com.typinggame.domain;

import java.util.Objects;

/**
 * Represents a word in the typing game.
 * Implements Comparable to sort words by length (primary) and alphabetically
 * (secondary).
 */
public class Word implements Comparable<Word> {
    private final String text;
    private final int length;
    private final DifficultyLevel difficulty;

    public Word(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Word text cannot be null or empty");
        }
        this.text = text.toLowerCase().trim();
        this.length = this.text.length();
        this.difficulty = calculateDifficulty();
    }

    /**
     * Calculate difficulty based on word length.
     */
    private DifficultyLevel calculateDifficulty() {
        if (length <= 3) {
            return DifficultyLevel.EASY;
        } else if (length <= 6) {
            return DifficultyLevel.MEDIUM;
        } else {
            return DifficultyLevel.HARD;
        }
    }

    /**
     * Compare words by length first, then alphabetically.
     * This allows sorting words from shortest to longest.
     */
    @Override
    public int compareTo(Word other) {
        // Primary sort: by length
        int lengthComparison = Integer.compare(this.length, other.length);
        if (lengthComparison != 0) {
            return lengthComparison;
        }
        // Secondary sort: alphabetically
        return this.text.compareTo(other.text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Word word = (Word) o;
        return text.equals(word.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return text;
    }

    // Getters
    public String getText() {
        return text;
    }

    public int getLength() {
        return length;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    /**
     * Enum representing difficulty levels based on word length.
     */
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}
