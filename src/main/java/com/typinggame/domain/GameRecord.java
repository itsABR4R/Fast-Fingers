package com.typinggame.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * GameRecord entity stored in MongoDB.
 * Represents a single game session result.
 */
@Document(collection = "game_records")
public class GameRecord {

    @Id
    private String id;

    private String userId;
    private String username;
    private double wpm;
    private double accuracy;
    private int wordsTyped;
    private String gameMode;
    private long duration; // in milliseconds
    private Date timestamp;

    public GameRecord() {
        this.timestamp = new Date();
    }

    public GameRecord(String userId, String username, double wpm, double accuracy,
            int wordsTyped, String gameMode, long duration) {
        this.userId = userId;
        this.username = username;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.wordsTyped = wordsTyped;
        this.gameMode = gameMode;
        this.duration = duration;
        this.timestamp = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getWpm() {
        return wpm;
    }

    public void setWpm(double wpm) {
        this.wpm = wpm;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getWordsTyped() {
        return wordsTyped;
    }

    public void setWordsTyped(int wordsTyped) {
        this.wordsTyped = wordsTyped;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("GameRecord{username='%s', wpm=%.1f, accuracy=%.1f%%, mode='%s', timestamp=%s}",
                username, wpm, accuracy, gameMode, timestamp);
    }
}
