package com.typinggame.websocket;

import com.typinggame.websocket.WebSocketSessionManager.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket controller to bridge frontend STOMP messages to backend raw Socket
 * logic.
 * Handles player join, progress updates, and chat messages.
 * Broadcasts updates in format expected by frontend.
 */
@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    @Autowired
    public GameWebSocketController(SimpMessagingTemplate messagingTemplate,
            WebSocketSessionManager sessionManager) {
        this.messagingTemplate = messagingTemplate;
        this.sessionManager = sessionManager;
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
    }

    /**
     * Handle player progress update.
     * Endpoint: /app/progress/{roomId}
     */
    @MessageMapping("/progress/{roomId}")
    public void handleProgressUpdate(@DestinationVariable String roomId,
            @Payload Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Integer progress = (Integer) payload.getOrDefault("progress", 0);
        Double wpm = ((Number) payload.getOrDefault("wpm", 0.0)).doubleValue();

        // Update player progress
        sessionManager.updatePlayerProgress(roomId, username, progress, wpm, "ACTIVE");

        // Broadcast updated player list
        broadcastPlayerList(roomId);
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
