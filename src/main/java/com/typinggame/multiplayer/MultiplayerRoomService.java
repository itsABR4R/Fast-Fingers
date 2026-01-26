package com.typinggame.multiplayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Room registry for raw Java Socket players.
 *
 * Requirement (2): Uses a HashMap<String, List<PlayerThread>> to manage rooms.
 *
 * This service is also used as a bridge target: when the Spring WebSocket
 * controller receives progress updates, it can broadcast those updates to
 * raw Socket clients in the same room.
 */
@Component
public class MultiplayerRoomService {

    public static final int DEFAULT_ROOM_SIZE = 2;

    // Requirement 2: HashMap<String, List<PlayerThread>>
    private final HashMap<String, List<PlayerThread>> rooms = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @Nullable
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MultiplayerRoomService(@Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Adds a raw socket player to a room.
     */
    public synchronized void joinRoom(String roomId, PlayerThread player) {
        rooms.computeIfAbsent(roomId, k -> new ArrayList<>()).add(player);

        // Broadcast start when room is full (Requirement 2)
        if (rooms.get(roomId).size() >= DEFAULT_ROOM_SIZE) {
            broadcastStartToSocketClients(roomId);
        }
    }

    /**
     * Removes a raw socket player from a room.
     */
    public synchronized void leaveRoom(String roomId, PlayerThread player) {
        List<PlayerThread> players = rooms.get(roomId);
        if (players == null) return;

        players.remove(player);
        if (players.isEmpty()) {
            rooms.remove(roomId);
        }
    }

    /**
     * Returns current raw-socket room size.
     */
    public synchronized int socketRoomSize(String roomId) {
        List<PlayerThread> players = rooms.get(roomId);
        return players == null ? 0 : players.size();
    }

    /**
     * Broadcast a START signal to raw socket clients.
     */
    public void broadcastStartToSocketClients(String roomId) {
        Map<String, Object> msg = Map.of(
                "type", "START",
                "roomId", roomId,
                "timestamp", System.currentTimeMillis()
        );
        broadcastToSocketRoom(roomId, msg, null);
    }

    /**
     * Broadcast progress update to raw socket clients in room.
     * Excludes sender username if provided.
     */
    public void broadcastProgressToSocketClients(String roomId,
                                                 String senderUsername,
                                                 double wpm,
                                                 int progressPercentage) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "PROGRESS");
        msg.put("roomId", roomId);
        msg.put("username", senderUsername);
        msg.put("wpm", wpm);
        msg.put("progress", progressPercentage);
        msg.put("timestamp", System.currentTimeMillis());

        broadcastToSocketRoom(roomId, msg, senderUsername);
    }

    /**
     * Broadcast a FINISH signal to raw socket clients.
     */
    public void broadcastFinishToSocketClients(String roomId, String winnerUsername) {
        Map<String, Object> msg = Map.of(
                "type", "FINISH",
                "roomId", roomId,
                "winner", winnerUsername,
                "timestamp", System.currentTimeMillis()
        );
        broadcastToSocketRoom(roomId, msg, null);
    }

    /**
     * Optional helper to broadcast to WebSocket topic (UI clients).
     */
    public void broadcastToWebSocketRoom(String roomId, Object payload) {
        if (messagingTemplate == null) return;
        messagingTemplate.convertAndSend("/topic/game/" + roomId, payload);
    }

    private void broadcastToSocketRoom(String roomId, Map<String, Object> message, String excludeUsername) {
        final String json;
        try {
            json = mapper.writeValueAsString(message);
        } catch (IOException e) {
            return;
        }

        List<PlayerThread> snapshot;
        synchronized (this) {
            List<PlayerThread> players = rooms.get(roomId);
            if (players == null || players.isEmpty()) return;
            snapshot = new ArrayList<>(players);
        }

        for (PlayerThread p : snapshot) {
            if (excludeUsername != null && excludeUsername.equalsIgnoreCase(p.getUsername())) {
                continue;
            }
            p.send(json);
        }
    }
}
