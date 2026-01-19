package com.typinggame.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Word class, especially the Comparable implementation.
 */
class WordTest {

    @Test
    void testWordCreation() {
        Word word = new Word("hello");
        assertEquals("hello", word.getText());
        assertEquals(5, word.getLength());
        assertEquals(Word.DifficultyLevel.MEDIUM, word.getDifficulty());
    }

    @Test
    void testWordCreationWithWhitespace() {
        Word word = new Word("  world  ");
        assertEquals("world", word.getText());
        assertEquals(5, word.getLength());
    }

    @Test
    void testWordCreationThrowsExceptionForNull() {
        assertThrows(IllegalArgumentException.class, () -> new Word(null));
    }

    @Test
    void testWordCreationThrowsExceptionForEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Word(""));
        assertThrows(IllegalArgumentException.class, () -> new Word("   "));
    }

    @Test
    void testDifficultyLevels() {
        Word easy = new Word("cat");
        assertEquals(Word.DifficultyLevel.EASY, easy.getDifficulty());

        Word medium = new Word("hello");
        assertEquals(Word.DifficultyLevel.MEDIUM, medium.getDifficulty());

        Word hard = new Word("programming");
        assertEquals(Word.DifficultyLevel.HARD, hard.getDifficulty());
    }

    @Test
    void testComparableByLength() {
        Word short1 = new Word("cat");
        Word medium = new Word("hello");
        Word long1 = new Word("programming");

        assertTrue(short1.compareTo(medium) < 0, "Shorter word should come first");
        assertTrue(medium.compareTo(long1) < 0, "Medium word should come before long word");
        assertTrue(long1.compareTo(short1) > 0, "Long word should come after short word");
    }

    @Test
    void testComparableByAlphabetWhenSameLength() {
        Word apple = new Word("apple");
        Word zebra = new Word("zebra");

        assertTrue(apple.compareTo(zebra) < 0, "Apple should come before zebra alphabetically");
        assertTrue(zebra.compareTo(apple) > 0, "Zebra should come after apple alphabetically");
    }

    @Test
    void testComparableWithIdenticalWords() {
        Word word1 = new Word("test");
        Word word2 = new Word("test");

        assertEquals(0, word1.compareTo(word2), "Identical words should be equal");
    }

    @Test
    void testSortingWords() {
        List<Word> words = new ArrayList<>();
        words.add(new Word("programming"));
        words.add(new Word("cat"));
        words.add(new Word("hello"));
        words.add(new Word("dog"));
        words.add(new Word("world"));

        Collections.sort(words);

        // After sorting: cat (3), dog (3), hello (5), world (5), programming (11)
        assertEquals("cat", words.get(0).getText());
        assertEquals("dog", words.get(1).getText());
        assertEquals("hello", words.get(2).getText());
        assertEquals("world", words.get(3).getText());
        assertEquals("programming", words.get(4).getText());
    }

    @Test
    void testSortingWordsWithAlphabeticalOrder() {
        List<Word> words = new ArrayList<>();
        words.add(new Word("zebra"));
        words.add(new Word("apple"));
        words.add(new Word("mango"));

        Collections.sort(words);

        // All have length 5, so alphabetical order: apple, mango, zebra
        assertEquals("apple", words.get(0).getText());
        assertEquals("mango", words.get(1).getText());
        assertEquals("zebra", words.get(2).getText());
    }

    @Test
    void testEqualsAndHashCode() {
        Word word1 = new Word("test");
        Word word2 = new Word("test");
        Word word3 = new Word("different");

        assertEquals(word1, word2, "Same words should be equal");
        assertNotEquals(word1, word3, "Different words should not be equal");

        assertEquals(word1.hashCode(), word2.hashCode(), "Same words should have same hash code");
    }

    @Test
    void testToString() {
        Word word = new Word("hello");
        assertEquals("hello", word.toString());
    }

    @Test
    void testCaseInsensitivity() {
        Word word1 = new Word("Hello");
        Word word2 = new Word("HELLO");
        Word word3 = new Word("hello");

        assertEquals(word1, word2);
        assertEquals(word2, word3);
        assertEquals("hello", word1.getText());
        assertEquals("hello", word2.getText());
    }
}
