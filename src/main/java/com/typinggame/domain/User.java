package com.typinggame.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * User entity stored in MongoDB.
 * Represents a registered user account with statistics.
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password; // Plain text for simplicity (educational project)

    private Date createdAt;

    // User statistics
    private int totalGames = 0;
    private double bestWPM = 0.0;
    private double avgWPM = 0.0;
    private int totalWins = 0;

    public User() {
        this.createdAt = new Date();
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = new Date();
        this.totalGames = 0;
        this.bestWPM = 0.0;
        this.avgWPM = 0.0;
        this.totalWins = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public double getBestWPM() {
        return bestWPM;
    }

    public void setBestWPM(double bestWPM) {
        this.bestWPM = bestWPM;
    }

    public double getAvgWPM() {
        return avgWPM;
    }

    public void setAvgWPM(double avgWPM) {
        this.avgWPM = avgWPM;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    /**
     * Update user statistics after a game.
     */
    public void updateStats(double wpm, boolean isWin) {
        this.totalGames++;

        if (wpm > this.bestWPM) {
            this.bestWPM = wpm;
        }

        // Recalculate average WPM
        this.avgWPM = ((this.avgWPM * (this.totalGames - 1)) + wpm) / this.totalGames;

        if (isWin) {
            this.totalWins++;
        }
    }

    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', email='%s', totalGames=%d, bestWPM=%.1f, avgWPM=%.1f}",
                id, username, email, totalGames, bestWPM, avgWPM);
    }
}
