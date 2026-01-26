package com.typinggame.websocket;

import com.typinggame.engine.TypingEngine;
import com.typinggame.io.ScoreManager;
import com.typinggame.io.UserStats;
import com.typinggame.multiplayer.MultiplayerRoomService;
import com.typinggame.websocket.WebSocketSessionManager.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket controller to bridge frontend STOMP messages to backend raw Socket
 * logic.
 * Handles player join, progress updates, and chat messages.
 * Broadcasts updates in format expected by frontend.
 */
@Controller
public class GameWebSocketController {

    private static final int ROOM_SIZE = 2;

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final MultiplayerRoomService multiplayerRoomService;
    private final TypingEngine typingEngine;
    private final ScoreManager scoreManager;

    @Autowired
    public GameWebSocketController(SimpMessagingTemplate messagingTemplate,
            WebSocketSessionManager sessionManager,
            MultiplayerRoomService multiplayerRoomService,
            TypingEngine typingEngine,
            ScoreManager scoreManager) {
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
        this.multiplayerRoomService = multiplayerRoomService;
        this.typingEngine = typingEngine;
        this.scoreManager = scoreManager;
    }

    /**
     * Handle player join request.
     * Endpoint: /app/join/{roomId}
     */
    @MessageMapping("/join/{roomId}")
    public void handlePlayerJoin(@DestinationVariable String roomId,
            @Payload Map<String, String> payload) {
        String username = payload.get("username");
        String sessionId = payload.getOrDefault("sessionId", "unknown");

        System.out.println("[WebSocket] Player " + username + " joining room " + roomId);

        // Add player to room
        sessionManager.addPlayerToRoom(roomId, username, sessionId);
        sessionManager.initializeWordQueue(roomId);

        // Broadcast updated player list to all clients in room
        broadcastPlayerList(roomId);

        // If room is full, broadcast START once
        PlayerInfo[] playersNow = sessionManager.getPlayersInRoom(roomId);
        if (playersNow.length >= ROOM_SIZE && !sessionManager.isRoomStarted(roomId)) {
            String text = generateSharedText(50);
            sessionManager.markRoomStarted(roomId, text);

            StartMessage startMessage = new StartMessage("START", roomId, text, System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/game/" + roomId, startMessage);

            // Also tell raw socket clients (Requirement 3 bridge)
            multiplayerRoomService.broadcastStartToSocketClients(roomId);
        }
    }

    /**
     * Handle player progress update.
     * Endpoint: /app/progress/{roomId}
     */
    @MessageMapping("/progress/{roomId}")
    public void handleProgressUpdate(@DestinationVariable String roomId,
            @Payload Map<String, Object> payload) {
        String username = (String) payload.get("username");
        int progress = ((Number) payload.getOrDefault("progress", 0)).intValue();
        Double wpm = ((Number) payload.getOrDefault("wpm", 0.0)).doubleValue();

        // Update player progress
        sessionManager.updatePlayerProgress(roomId, username, progress, wpm, "ACTIVE");

        // Bridge: broadcast to raw socket players in same room (Requirement 3)
        multiplayerRoomService.broadcastProgressToSocketClients(roomId, username, wpm, progress);

        // Broadcast updated player list
        broadcastPlayerList(roomId);

        // Winner detection (Requirement 5)
        if (progress >= 100) {
            String winner = sessionManager.setWinnerIfAbsent(roomId, username);
            if (username != null && username.equals(winner)) {
                // Mark status
                sessionManager.updatePlayerProgress(roomId, username, 100, wpm, "FINISHED");
                broadcastPlayerList(roomId);

                FinishMessage finishMessage = new FinishMessage("FINISH", roomId, winner, System.currentTimeMillis());
                messagingTemplate.convertAndSend("/topic/game/" + roomId, finishMessage);
                multiplayerRoomService.broadcastFinishToSocketClients(roomId, winner);

                // Save to scores.dat via ObjectOutputStream (Requirement 3) with mode/test type 'multiplayer'
                saveMultiplayerResult(winner, wpm);
            }
        }
    }

    /**
     * Optional: accept a final stats payload to save a cleaner multiplayer record.
     * Endpoint: /app/finish/{roomId}
     */
    @MessageMapping("/finish/{roomId}")
    public void handleFinish(@DestinationVariable String roomId,
                             @Payload Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Double wpm = ((Number) payload.getOrDefault("wpm", 0.0)).doubleValue();
        Double accuracy = ((Number) payload.getOrDefault("accuracy", 100.0)).doubleValue();
        Integer wordsTyped = ((Number) payload.getOrDefault("wordsTyped", 0)).intValue();
        Long duration = ((Number) payload.getOrDefault("duration", 0L)).longValue();

        // Winner remains first to 100%, but we can still store player record.
        saveMultiplayerResult(username, wpm, accuracy, wordsTyped, duration);
    }

    /**
     * Handle chat message.
     * Endpoint: /app/chat/{roomId}
     */
    @MessageMapping("/chat/{roomId}")
    public void handleChatMessage(@DestinationVariable String roomId,
            @Payload Map<String, String> payload) {
        String sender = payload.get("sender");
        String content = payload.get("content");

        // Create chat message
        ChatMessage chatMessage = new ChatMessage(sender, content, System.currentTimeMillis());

        // Broadcast to all clients in room
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);
    }

    /**
     * Broadcast player list to all clients in room.
     * Format: [{ "username": "...", "progress": 0, "wpm": 0, "status": "ACTIVE" }]
     */
    private void broadcastPlayerList(String roomId) {
        PlayerInfo[] players = sessionManager.getPlayersInRoom(roomId);

        // Create update message
        PlayerUpdateMessage message = new PlayerUpdateMessage("PLAYER_UPDATE", players);

        // Broadcast to topic
        messagingTemplate.convertAndSend("/topic/game/" + roomId, message);
    }

    private String generateSharedText(int wordCount) {
        List<String> words = typingEngine.getRandomWords(wordCount)
                .stream()
                .map(w -> w.getText())
                .collect(Collectors.toList());
        return String.join(" ", words);
    }

    private void saveMultiplayerResult(String username, double wpm) {
        saveMultiplayerResult(username, wpm, 100.0, 0, 0L);
    }

    private void saveMultiplayerResult(String username,
                                       double wpm,
                                       double accuracy,
                                       int wordsTyped,
                                       long duration) {
        if (username == null || username.isBlank()) return;
        try {
            UserStats stats;
            try {
                stats = scoreManager.loadStats();
                if (stats == null || !stats.getUsername().equals(username)) {
                    stats = new UserStats(username);
                }
            } catch (Exception e) {
                stats = new UserStats(username);
            }

            // mode labeled as 'multiplayer'
            stats.addGameRecord(wpm, accuracy, wordsTyped, duration, "multiplayer");
            scoreManager.saveStats(stats);
        } catch (Exception e) {
            System.err.println("[WebSocket] Failed to save multiplayer result: " + e.getMessage());
        }
    }

    /**
     * Message class for player updates.
     */
    public static class PlayerUpdateMessage {
        private String type;
        private PlayerInfo[] players;

        public PlayerUpdateMessage(String type, PlayerInfo[] players) {
            this.type = type;
            this.players = players;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public PlayerInfo[] getPlayers() {
            return players;
        }

        public void setPlayers(PlayerInfo[] players) {
            this.players = players;
        }
    }

    public static class StartMessage {
        private String type;
        private String roomId;
        private String text;
        private long timestamp;

        public StartMessage(String type, String roomId, String text, long timestamp) {
            this.type = type;
            this.roomId = roomId;
            this.text = text;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class FinishMessage {
        private String type;
        private String roomId;
        private String winner;
        private long timestamp;

        public FinishMessage(String type, String roomId, String winner, long timestamp) {
            this.type = type;
            this.roomId = roomId;
            this.winner = winner;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public String getWinner() { return winner; }
        public void setWinner(String winner) { this.winner = winner; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Chat message class.
     */
    public static class ChatMessage {
        private String sender;
        private String content;
        private long timestamp;

        public ChatMessage(String sender, String content, long timestamp) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
