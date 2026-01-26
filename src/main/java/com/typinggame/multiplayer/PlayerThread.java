package com.typinggame.multiplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Handles one raw TCP socket client.
 *
 * Protocol (newline-delimited JSON):
 *  - JOIN: {"type":"JOIN","username":"...","roomId":"..."}
 *  - PROGRESS: {"type":"PROGRESS","wpm":45.0,"progress":37}
 *  - FINISH: {"type":"FINISH"}
 */
public class PlayerThread extends Thread {

    private final Socket socket;
    private final MultiplayerRoomService roomService;

    private final ObjectMapper mapper = new ObjectMapper();

    private volatile boolean running = true;
    private volatile String username = "Anonymous";
    private volatile String roomId = null;

    private BufferedReader in;
    private BufferedWriter out;

    public PlayerThread(Socket socket, MultiplayerRoomService roomService) {
        this.socket = socket;
        this.roomService = roomService;
        setName("PlayerThread-" + socket.getRemoteSocketAddress());
    }

    public String getUsername() {
        return username;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // Handshake: expect JOIN as first message
            String first = in.readLine();
            if (first == null) {
                shutdown();
                return;
            }

            Map<String, Object> joinMsg = parseJson(first);
            if (joinMsg == null || !"JOIN".equalsIgnoreCase(String.valueOf(joinMsg.get("type")))) {
                send(error("First message must be JOIN"));
                shutdown();
                return;
            }

            this.username = String.valueOf(joinMsg.getOrDefault("username", "Anonymous"));
            this.roomId = String.valueOf(joinMsg.getOrDefault("roomId", "room_1"));

            roomService.joinRoom(roomId, this);
            send(ack("JOINED", roomId, username));

            while (running) {
                String line = in.readLine();
                if (line == null) break;

                Map<String, Object> msg = parseJson(line);
                if (msg == null) continue;

                String type = String.valueOf(msg.getOrDefault("type", "")).toUpperCase();

                switch (type) {
                    case "PROGRESS" -> {
                        double wpm = ((Number) msg.getOrDefault("wpm", 0.0)).doubleValue();
                        int progress = ((Number) msg.getOrDefault("progress", 0)).intValue();
                        roomService.broadcastProgressToSocketClients(roomId, username, wpm, progress);
                    }
                    case "FINISH" -> {
                        roomService.broadcastFinishToSocketClients(roomId, username);
                    }
                    default -> send(error("Unknown type: " + type));
                }
            }

        } catch (IOException ignored) {
            // connection lost
        } finally {
            shutdown();
        }
    }

    public void send(String jsonLine) {
        try {
            if (out == null) return;
            out.write(jsonLine);
            out.write("\n");
            out.flush();
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (roomId != null) {
                roomService.leaveRoom(roomId, this);
            }
        } catch (Exception ignored) {}

        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String error(String message) {
        try {
            return mapper.writeValueAsString(Map.of("type", "ERROR", "message", message));
        } catch (Exception e) {
            return "{\"type\":\"ERROR\",\"message\":\"" + message.replace("\"", "'") + "\"}";
        }
    }

    private String ack(String type, String roomId, String username) {
        try {
            return mapper.writeValueAsString(Map.of(
                    "type", type,
                    "roomId", roomId,
                    "username", username,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return "{\"type\":\"" + type + "\"}";
        }
    }
}
