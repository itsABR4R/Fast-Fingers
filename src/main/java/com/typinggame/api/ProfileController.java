package com.typinggame.api;

import com.typinggame.domain.GameRecord;
import com.typinggame.domain.User;
import com.typinggame.repository.GameRecordRepository;
import com.typinggame.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for user profile endpoints.
 * Handles fetching user profile data and game history.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AuthService authService;
    private final GameRecordRepository gameRecordRepository;

    @Autowired
    public ProfileController(AuthService authService, GameRecordRepository gameRecordRepository) {
        this.authService = authService;
        this.gameRecordRepository = gameRecordRepository;
    }

    /**
     * GET /api/profile/{username} - Get user profile and statistics
     */
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String username) {
        try {
            User user = authService.getUserProfile(username);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("createdAt", user.getCreatedAt());
            response.put("totalGames", user.getTotalGames());
            response.put("bestWPM", user.getBestWPM());
            response.put("avgWPM", user.getAvgWPM());
            response.put("totalWins", user.getTotalWins());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/profile/{username}/history - Get user's game history
     */
    @GetMapping("/{username}/history")
    public ResponseEntity<Map<String, Object>> getGameHistory(@PathVariable String username) {
        try {
            User user = authService.getUserProfile(username);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<GameRecord> gameRecords = gameRecordRepository.findTop20ByUserIdOrderByTimestampDesc(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", username);
            response.put("totalRecords", gameRecords.size());
            response.put("games", gameRecords);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching game history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
