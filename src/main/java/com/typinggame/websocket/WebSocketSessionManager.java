package com.typinggame.websocket;

import com.typinggame.network.GameSession;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages WebSocket sessions and bridges them to raw Socket GameSession
 * instances.
 * Uses Queue to buffer words being sent to frontend (Req 2).
 * Thread-safe implementation using ConcurrentHashMap.
 */
@Component
public class WebSocketSessionManager {

    // Map WebSocket session IDs to GameSession instances
    private final ConcurrentHashMap<String, GameSession> sessionMap;

    // Map room IDs to player lists for broadcasting
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, PlayerInfo>> roomPlayers;

    // Queue for buffering words to prevent lag (Req 2)
    private final ConcurrentHashMap<String, Queue<String>> wordQueues;

    // Per-room state (started/text/winner)
    private final ConcurrentHashMap<String, RoomState> roomStates;

    public WebSocketSessionManager() {
        this.sessionMap = new ConcurrentHashMap<>();
        this.roomPlayers = new ConcurrentHashMap<>();
        this.wordQueues = new ConcurrentHashMap<>();
        this.roomStates = new ConcurrentHashMap<>();
    }

    /**
     * Add player to a room.
     */
    public synchronized void addPlayerToRoom(String roomId, String username, String sessionId) {
        roomPlayers.putIfAbsent(roomId, new ConcurrentHashMap<>());
        ConcurrentHashMap<String, PlayerInfo> players = roomPlayers.get(roomId);

        PlayerInfo playerInfo = new PlayerInfo(username, sessionId);
        players.put(username, playerInfo);

        System.out.println("[WebSocketSessionManager] Player " + username + " joined room " + roomId);

        // Ensure room state exists
        roomStates.putIfAbsent(roomId, new RoomState());
    }

    /**
     * Remove player from a room.
     */
    public synchronized void removePlayerFromRoom(String roomId, String username) {
        ConcurrentHashMap<String, PlayerInfo> players = roomPlayers.get(roomId);
        if (players != null) {
            players.remove(username);
            if (players.isEmpty()) {
                roomPlayers.remove(roomId);
                wordQueues.remove(roomId);
                roomStates.remove(roomId);
            }
        }
    }

    public RoomState getRoomState(String roomId) {
        roomStates.putIfAbsent(roomId, new RoomState());
        return roomStates.get(roomId);
    }

    public synchronized void markRoomStarted(String roomId, String text) {
        RoomState state = getRoomState(roomId);
        if (!state.started) {
            state.started = true;
            state.text = text;
            state.startTimestamp = System.currentTimeMillis();
        }
    }

    public boolean isRoomStarted(String roomId) {
        return getRoomState(roomId).started;
    }

    public String getRoomText(String roomId) {
        return getRoomState(roomId).text;
    }

    /**
     * Set winner only if none exists.
     * @return winner username after attempt.
     */
    public synchronized String setWinnerIfAbsent(String roomId, String winnerUsername) {
        RoomState state = getRoomState(roomId);
        if (state.winner == null) {
            state.winner = winnerUsername;
        }
        return state.winner;
    }

    public String getWinner(String roomId) {
        return getRoomState(roomId).winner;
    }

    /**
     * Update player progress.
     */
    public synchronized void updatePlayerProgress(String roomId, String username,
            int progress, double wpm, String status) {
        ConcurrentHashMap<String, PlayerInfo> players = roomPlayers.get(roomId);
        if (players != null && players.containsKey(username)) {
            PlayerInfo player = players.get(username);
            player.progress = progress;
            player.wpm = wpm;
            player.status = status;
        }
    }

    /**
     * Get all players in a room for broadcasting.
     * Returns array format expected by frontend: [{ "username": "...", "progress":
     * 0, "wpm": 0, "status": "ACTIVE" }]
     */
    public PlayerInfo[] getPlayersInRoom(String roomId) {
        ConcurrentHashMap<String, PlayerInfo> players = roomPlayers.get(roomId);
        if (players == null || players.isEmpty()) {
            return new PlayerInfo[0];
        }
        return players.values().toArray(new PlayerInfo[0]);
    }

    /**
     * Initialize word queue for a room.
     */
    public void initializeWordQueue(String roomId) {
        wordQueues.putIfAbsent(roomId, new LinkedList<>());
    }

    /**
     * Add words to queue for buffering (Req 2).
     */
    public synchronized void addWordsToQueue(String roomId, String... words) {
        Queue<String> queue = wordQueues.get(roomId);
        if (queue != null) {
            for (String word : words) {
                queue.offer(word);
            }
        }
    }

    /**
     * Get next word from queue.
     */
    public synchronized String getNextWord(String roomId) {
        Queue<String> queue = wordQueues.get(roomId);
        return (queue != null) ? queue.poll() : null;
    }

    /**
     * Check if room has players.
     */
    public boolean hasPlayers(String roomId) {
        ConcurrentHashMap<String, PlayerInfo> players = roomPlayers.get(roomId);
        return players != null && !players.isEmpty();
    }

    /**
     * Internal per-room state.
     */
    public static class RoomState {
        public boolean started = false;
        public String text = null;
        public long startTimestamp = 0L;
        public String winner = null;
    }

    /**
     * Player information class for JSON serialization.
     */
    public static class PlayerInfo {
        public String username;
        public String sessionId;
        public int progress;
        public double wpm;
        public String status;

        public PlayerInfo(String username, String sessionId) {
            this.username = username;
            this.sessionId = sessionId;
            this.progress = 0;
            this.wpm = 0.0;
            this.status = "ACTIVE";
        }

        // Getters and setters for JSON serialization
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public double getWpm() {
            return wpm;
        }

        public void setWpm(double wpm) {
            this.wpm = wpm;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
