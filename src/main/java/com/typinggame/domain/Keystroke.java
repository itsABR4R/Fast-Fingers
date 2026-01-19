package com.typinggame.domain;

/**
 * Represents a single keystroke event in the typing game.
 * Used in Stack for undo/backspace functionality.
 */
public class Keystroke {
    private final char character;
    private final long timestamp;
    private final boolean correct;
    private final int position;

    public Keystroke(char character, long timestamp, boolean correct, int position) {
        this.character = character;
        this.timestamp = timestamp;
        this.correct = correct;
        this.position = position;
    }

    public char getCharacter() {
        return character;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("Keystroke{char='%c', correct=%b, pos=%d, time=%d}",
                character, correct, position, timestamp);
    }
}
