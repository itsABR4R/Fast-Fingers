package com.typinggame.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Message protocol for client-server communication in multiplayer mode.
 * Uses JSON serialization for network transmission.
 */
public class GameMessage {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MessageType type;
    private String playerId;
    private String playerName;
    private PlayerProgress progress;
    private List<String> attackWords;
    private String gameMode;
    private String message;
    private String winnerId;
    private Integer health; // Current health
    private Integer maxHealth; // Maximum health
    private String typedWord; // Word typed by player
    private Boolean wordCorrect; // Whether word was correct

    public GameMessage() {
    }

    @JsonCreator
    public GameMessage(@JsonProperty("type") MessageType type) {
        this.type = type;
    }

    /**
     * Message types for different game events.
     */
    public enum MessageType {
        CONNECT, // Client connects to server
        READY, // Player is ready to start
        GAME_START, // Game has started
        PROGRESS_UPDATE, // Player progress update (WPM, accuracy)
        WORD_VALIDATION, // Player submitted a word for validation
        HEALTH_UPDATE, // Health changed (wrong word)
        ATTACK, // Elimination mode attack
        OPPONENT_PROGRESS, // Opponent's progress
        GAME_OVER, // Player eliminated (health = 0)
        GAME_END, // Game finished
        DISCONNECT, // Player disconnected
        ERROR // Error message
    }

    /**
     * Player progress data for real-time updates.
     */
    public static class PlayerProgress {
        private double wpm;
        private double accuracy;
        private int wordsCompleted;
        private int totalWords;
        private String currentWord;
        private long elapsedTime;

        public PlayerProgress() {
        }

        public PlayerProgress(double wpm, double accuracy, int wordsCompleted,
                int totalWords, String currentWord, long elapsedTime) {
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.wordsCompleted = wordsCompleted;
            this.totalWords = totalWords;
            this.currentWord = currentWord;
            this.elapsedTime = elapsedTime;
        }

        // Getters and Setters
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

        public int getWordsCompleted() {
            return wordsCompleted;
        }

        public void setWordsCompleted(int wordsCompleted) {
            this.wordsCompleted = wordsCompleted;
        }

        public int getTotalWords() {
            return totalWords;
        }

        public void setTotalWords(int totalWords) {
            this.totalWords = totalWords;
        }

        public String getCurrentWord() {
            return currentWord;
        }

        public void setCurrentWord(String currentWord) {
            this.currentWord = currentWord;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
    }

    /**
     * Serialize message to JSON string.
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize GameMessage", e);
        }
    }

    /**
     * Deserialize message from JSON string.
     */
    public static GameMessage fromJson(String json) {
        try {
            return objectMapper.readValue(json, GameMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize GameMessage", e);
        }
    }

    // Builder pattern for easy message creation
    public static GameMessage connect(String playerId, String playerName, String gameMode) {
        GameMessage msg = new GameMessage(MessageType.CONNECT);
        msg.playerId = playerId;
        msg.playerName = playerName;
        msg.gameMode = gameMode;
        return msg;
    }

    public static GameMessage ready(String playerId) {
        GameMessage msg = new GameMessage(MessageType.READY);
        msg.playerId = playerId;
        return msg;
    }

    public static GameMessage gameStart(List<String> words) {
        GameMessage msg = new GameMessage(MessageType.GAME_START);
        msg.attackWords = words; // Reusing field for word list
        return msg;
    }

    public static GameMessage progressUpdate(String playerId, PlayerProgress progress) {
        GameMessage msg = new GameMessage(MessageType.PROGRESS_UPDATE);
        msg.playerId = playerId;
        msg.progress = progress;
        return msg;
    }

    public static GameMessage attack(String playerId, List<String> attackWords) {
        GameMessage msg = new GameMessage(MessageType.ATTACK);
        msg.playerId = playerId;
        msg.attackWords = attackWords;
        return msg;
    }

    public static GameMessage opponentProgress(String playerId, PlayerProgress progress) {
        GameMessage msg = new GameMessage(MessageType.OPPONENT_PROGRESS);
        msg.playerId = playerId;
        msg.progress = progress;
        return msg;
    }

    public static GameMessage gameEnd(String winnerId, String message) {
        GameMessage msg = new GameMessage(MessageType.GAME_END);
        msg.winnerId = winnerId;
        msg.message = message;
        return msg;
    }

    public static GameMessage error(String message) {
        GameMessage msg = new GameMessage(MessageType.ERROR);
        msg.message = message;
        return msg;
    }

    public static GameMessage wordValidation(String playerId, String typedWord) {
        GameMessage msg = new GameMessage(MessageType.WORD_VALIDATION);
        msg.playerId = playerId;
        msg.typedWord = typedWord;
        return msg;
    }

    public static GameMessage healthUpdate(String playerId, int currentHealth, int maxHealth, boolean wordCorrect) {
        GameMessage msg = new GameMessage(MessageType.HEALTH_UPDATE);
        msg.playerId = playerId;
        msg.health = currentHealth;
        msg.maxHealth = maxHealth;
        msg.wordCorrect = wordCorrect;
        return msg;
    }

    public static GameMessage gameOver(String playerId, String message) {
        GameMessage msg = new GameMessage(MessageType.GAME_OVER);
        msg.playerId = playerId;
        msg.message = message;
        return msg;
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public PlayerProgress getProgress() {
        return progress;
    }

    public void setProgress(PlayerProgress progress) {
        this.progress = progress;
    }

    public List<String> getAttackWords() {
        return attackWords;
    }

    public void setAttackWords(List<String> attackWords) {
        this.attackWords = attackWords;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public String getTypedWord() {
        return typedWord;
    }

    public void setTypedWord(String typedWord) {
        this.typedWord = typedWord;
    }

    public Boolean getWordCorrect() {
        return wordCorrect;
    }

    public void setWordCorrect(Boolean wordCorrect) {
        this.wordCorrect = wordCorrect;
    }

    @Override
    public String toString() {
        return String.format("GameMessage{type=%s, playerId='%s', playerName='%s'}",
                type, playerId, playerName);
    }
}
