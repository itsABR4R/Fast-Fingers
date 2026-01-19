package com.typinggame.engine;

import com.typinggame.domain.Keystroke;
import com.typinggame.domain.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerformanceTracker class.
 * Tests Stack, Queue, and Set operations.
 */
class PerformanceTrackerTest {

    private PerformanceTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new PerformanceTracker();
    }

    @Test
    void testInitialState() {
        assertEquals(0, tracker.getTotalKeystrokes());
        assertEquals(0, tracker.getCorrectKeystrokes());
        assertEquals(0, tracker.getErrorCount());
        assertEquals(0, tracker.getWordsCompleted());
        assertEquals(0, tracker.getUniqueCorrectWordsCount());
    }

    // ========== Stack Tests (Keystroke History) ==========

    @Test
    void testRecordKeystroke_Stack() {
        tracker.recordKeystroke('h', true, 0);
        tracker.recordKeystroke('e', true, 1);
        tracker.recordKeystroke('x', false, 2);

        assertEquals(3, tracker.getKeystrokeHistorySize());
        assertEquals(3, tracker.getTotalKeystrokes());
        assertEquals(2, tracker.getCorrectKeystrokes());
        assertEquals(1, tracker.getErrorCount());
    }

    @Test
    void testPeekLastKeystroke_Stack() {
        tracker.recordKeystroke('a', true, 0);
        tracker.recordKeystroke('b', false, 1);

        Keystroke last = tracker.peekLastKeystroke();
        assertNotNull(last);
        assertEquals('b', last.getCharacter());
        assertFalse(last.isCorrect());
        assertEquals(1, last.getPosition());

        // Peek should not remove the keystroke
        assertEquals(2, tracker.getKeystrokeHistorySize());
    }

    @Test
    void testUndoLastKeystroke_Stack() {
        tracker.recordKeystroke('h', true, 0);
        tracker.recordKeystroke('e', true, 1);
        tracker.recordKeystroke('x', false, 2);

        assertEquals(3, tracker.getKeystrokeHistorySize());

        // Undo the last keystroke (the error)
        Keystroke undone = tracker.undoLastKeystroke();
        assertNotNull(undone);
        assertEquals('x', undone.getCharacter());
        assertFalse(undone.isCorrect());

        assertEquals(2, tracker.getKeystrokeHistorySize());
        assertEquals(2, tracker.getTotalKeystrokes());
        assertEquals(2, tracker.getCorrectKeystrokes());
        assertEquals(0, tracker.getErrorCount());
    }

    @Test
    void testUndoOnEmptyStack() {
        Keystroke undone = tracker.undoLastKeystroke();
        assertNull(undone);
    }

    @Test
    void testPeekOnEmptyStack() {
        Keystroke peeked = tracker.peekLastKeystroke();
        assertNull(peeked);
    }

    @Test
    void testMultipleUndos_Stack() {
        tracker.recordKeystroke('a', true, 0);
        tracker.recordKeystroke('b', true, 1);
        tracker.recordKeystroke('c', true, 2);

        tracker.undoLastKeystroke(); // Remove 'c'
        tracker.undoLastKeystroke(); // Remove 'b'

        assertEquals(1, tracker.getKeystrokeHistorySize());
        Keystroke last = tracker.peekLastKeystroke();
        assertEquals('a', last.getCharacter());
    }

    // ========== Queue Tests (Upcoming Words) ==========

    @Test
    void testAddUpcomingWords_Queue() {
        List<Word> words = Arrays.asList(
                new Word("hello"),
                new Word("world"),
                new Word("test"));

        tracker.addUpcomingWords(words);
        assertEquals(3, tracker.getUpcomingWordsCount());
    }

    @Test
    void testQueueLimit_MaxTenWords() {
        List<Word> words = Arrays.asList(
                new Word("one"), new Word("two"), new Word("three"),
                new Word("four"), new Word("five"), new Word("six"),
                new Word("seven"), new Word("eight"), new Word("nine"),
                new Word("ten"), new Word("eleven"), new Word("twelve"));

        tracker.addUpcomingWords(words);

        // Queue should only hold 10 words
        assertEquals(10, tracker.getUpcomingWordsCount());
    }

    @Test
    void testPeekNextWord_Queue() {
        List<Word> words = Arrays.asList(
                new Word("first"),
                new Word("second"));

        tracker.addUpcomingWords(words);

        Word next = tracker.peekNextWord();
        assertNotNull(next);
        assertEquals("first", next.getText());

        // Peek should not remove the word
        assertEquals(2, tracker.getUpcomingWordsCount());
    }

    @Test
    void testPollNextWord_Queue() {
        List<Word> words = Arrays.asList(
                new Word("first"),
                new Word("second"),
                new Word("third"));

        tracker.addUpcomingWords(words);

        Word polled = tracker.pollNextWord();
        assertEquals("first", polled.getText());
        assertEquals(2, tracker.getUpcomingWordsCount());

        polled = tracker.pollNextWord();
        assertEquals("second", polled.getText());
        assertEquals(1, tracker.getUpcomingWordsCount());
    }

    @Test
    void testPollOnEmptyQueue() {
        Word polled = tracker.pollNextWord();
        assertNull(polled);
    }

    @Test
    void testGetUpcomingWordsList_Queue() {
        List<Word> words = Arrays.asList(
                new Word("alpha"),
                new Word("beta"),
                new Word("gamma"));

        tracker.addUpcomingWords(words);
        List<Word> upcomingList = tracker.getUpcomingWordsList();

        assertEquals(3, upcomingList.size());
        assertEquals("alpha", upcomingList.get(0).getText());
        assertEquals("beta", upcomingList.get(1).getText());
        assertEquals("gamma", upcomingList.get(2).getText());
    }

    @Test
    void testRefillUpcomingWords_Queue() {
        List<Word> initialWords = Arrays.asList(
                new Word("one"),
                new Word("two"));
        tracker.addUpcomingWords(initialWords);
        assertEquals(2, tracker.getUpcomingWordsCount());

        List<Word> newWords = Arrays.asList(
                new Word("three"),
                new Word("four"),
                new Word("five"),
                new Word("six"),
                new Word("seven"),
                new Word("eight"),
                new Word("nine"),
                new Word("ten"));

        tracker.refillUpcomingWords(newWords);
        assertEquals(10, tracker.getUpcomingWordsCount());
    }

    // ========== Set Tests (Unique Correct Words) ==========

    @Test
    void testRecordCorrectWord_Set() {
        tracker.recordCorrectWord("hello");
        tracker.recordCorrectWord("world");

        assertEquals(2, tracker.getUniqueCorrectWordsCount());
        assertEquals(2, tracker.getWordsCompleted());
    }

    @Test
    void testSetUniqueness_NoDuplicates() {
        tracker.recordCorrectWord("test");
        tracker.recordCorrectWord("test");
        tracker.recordCorrectWord("TEST");
        tracker.recordCorrectWord("Test");

        // Set should only contain one unique word (case-insensitive)
        assertEquals(1, tracker.getUniqueCorrectWordsCount());

        // But words completed should count all attempts
        assertEquals(4, tracker.getWordsCompleted());
    }

    @Test
    void testHasTypedWordBefore_Set() {
        tracker.recordCorrectWord("hello");
        tracker.recordCorrectWord("world");

        assertTrue(tracker.hasTypedWordBefore("hello"));
        assertTrue(tracker.hasTypedWordBefore("HELLO"));
        assertTrue(tracker.hasTypedWordBefore("world"));
        assertFalse(tracker.hasTypedWordBefore("test"));
    }

    @Test
    void testGetUniqueCorrectWords_Set() {
        tracker.recordCorrectWord("apple");
        tracker.recordCorrectWord("banana");
        tracker.recordCorrectWord("apple"); // Duplicate

        Set<String> uniqueWords = tracker.getUniqueCorrectWords();
        assertEquals(2, uniqueWords.size());
        assertTrue(uniqueWords.contains("apple"));
        assertTrue(uniqueWords.contains("banana"));
    }

    // ========== Performance Metrics Tests ==========

    @Test
    void testCalculateAccuracy() {
        tracker.recordKeystroke('h', true, 0);
        tracker.recordKeystroke('e', true, 1);
        tracker.recordKeystroke('l', true, 2);
        tracker.recordKeystroke('x', false, 3);
        tracker.recordKeystroke('l', true, 4);

        double accuracy = tracker.calculateAccuracy();
        assertEquals(80.0, accuracy, 0.01); // 4 correct out of 5 = 80%
    }

    @Test
    void testCalculateAccuracyWithNoKeystrokes() {
        double accuracy = tracker.calculateAccuracy();
        assertEquals(0.0, accuracy);
    }

    @Test
    void testSessionDuration() throws InterruptedException {
        tracker.startSession();
        Thread.sleep(100); // Wait 100ms
        tracker.endSession();

        long duration = tracker.getSessionDuration();
        assertTrue(duration >= 100, "Session duration should be at least 100ms");
    }

    @Test
    void testGetMetrics() {
        tracker.startSession();
        tracker.recordKeystroke('h', true, 0);
        tracker.recordKeystroke('i', true, 1);
        tracker.recordCorrectWord("hi");

        PerformanceTracker.PerformanceMetrics metrics = tracker.getMetrics();

        assertEquals(2, metrics.getTotalKeystrokes());
        assertEquals(2, metrics.getCorrectKeystrokes());
        assertEquals(0, metrics.getErrors());
        assertEquals(1, metrics.getWordsCompleted());
        assertEquals(1, metrics.getUniqueWordsTyped());
        assertEquals(100.0, metrics.getAccuracy(), 0.01);
    }

    @Test
    void testResetMetrics() {
        tracker.recordKeystroke('a', true, 0);
        tracker.recordCorrectWord("test");
        tracker.addUpcomingWords(Arrays.asList(new Word("word")));

        tracker.resetMetrics();

        assertEquals(0, tracker.getTotalKeystrokes());
        assertEquals(0, tracker.getCorrectKeystrokes());
        assertEquals(0, tracker.getWordsCompleted());
        assertEquals(0, tracker.getUniqueCorrectWordsCount());
        assertEquals(0, tracker.getUpcomingWordsCount());
        assertEquals(0, tracker.getKeystrokeHistorySize());
    }
}
