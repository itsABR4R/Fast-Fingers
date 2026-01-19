package com.typinggame.api;

import com.typinggame.domain.Word;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for API responses containing word lists.
 */
public class WordResponse {
    private List<String> words;
    private int count;
    private String difficulty;
    private String message;

    public WordResponse() {
    }

    public WordResponse(List<Word> wordList) {
        this.words = wordList.stream()
                .map(Word::getText)
                .collect(Collectors.toList());
        this.count = words.size();
    }

    public WordResponse(List<Word> wordList, String message) {
        this(wordList);
        this.message = message;
    }

    // Getters and Setters
    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
        this.count = words != null ? words.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
