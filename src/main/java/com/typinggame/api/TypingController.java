package com.typinggame.api;

import com.typinggame.domain.Word;
import com.typinggame.engine.PerformanceTracker;
import com.typinggame.engine.TypingEngine;
import com.typinggame.io.CodeSnippetLoader;
import com.typinggame.io.ScoreManager;
import com.typinggame.io.UserStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for the Typing Game API.
 * Provides endpoints for Practice Mode and session management.
 * CORS is configured globally in CorsConfig.java
 */
@RestController
@RequestMapping("/api")
public class TypingController {

    private final TypingEngine typingEngine;
    private final PerformanceTracker performanceTracker;
    private final CodeSnippetLoader snippetLoader;
    private final ScoreManager scoreManager;

    @Autowired
    public TypingController(TypingEngine typingEngine, PerformanceTracker performanceTracker,
            CodeSnippetLoader snippetLoader, ScoreManager scoreManager) {
        this.typingEngine = typingEngine;
        this.performanceTracker = performanceTracker;
        this.snippetLoader = snippetLoader;
        this.scoreManager = scoreManager;
    }

    /**
     * GET /api/words - Fetch all words for practice mode.
     */
    @GetMapping("/words")
    public ResponseEntity<WordResponse> getAllWords() {
        List<Word> words = typingEngine.getAllWords();
        WordResponse response = new WordResponse(words, "Retrieved all words from word bank");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/words/random?count=10 - Get random words.
     */
    @GetMapping("/words/random")
    public ResponseEntity<WordResponse> getRandomWords(
            @RequestParam(defaultValue = "10") int count) {

        if (count <= 0 || count > 100) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Count must be between 1 and 100"));
        }

        List<Word> words = typingEngine.getRandomWords(count);
        WordResponse response = new WordResponse(words,
                String.format("Retrieved %d random words", words.size()));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/words/difficulty/{level} - Get words by difficulty.
     */
    @GetMapping("/words/difficulty/{level}")
    public ResponseEntity<WordResponse> getWordsByDifficulty(
            @PathVariable String level) {

        try {
            Word.DifficultyLevel difficulty = Word.DifficultyLevel.valueOf(level.toUpperCase());
            List<Word> words = typingEngine.getWordsByDifficulty(difficulty);

            WordResponse response = new WordResponse(words,
                    String.format("Retrieved %d words with %s difficulty", words.size(), level));
            response.setDifficulty(level.toUpperCase());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid difficulty level. Use: EASY, MEDIUM, or HARD"));
        }
    }

    /**
     * GET /api/words/length - Get words within a length range.
     */
    @GetMapping("/words/length")
    public ResponseEntity<WordResponse> getWordsByLength(
            @RequestParam(defaultValue = "1") int min,
            @RequestParam(defaultValue = "10") int max) {

        if (min < 1 || max > 20 || min > max) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid length range"));
        }

        List<Word> words = typingEngine.getWordsByLengthRange(min, max);
        WordResponse response = new WordResponse(words,
                String.format("Retrieved %d words with length %d-%d", words.size(), min, max));

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/session/start - Start a new typing session.
     */
    @PostMapping("/session/start")
    public ResponseEntity<SessionResponse> startSession(
            @RequestParam(defaultValue = "10") int wordCount) {

        // Generate a unique session ID
        String sessionId = UUID.randomUUID().toString();

        // Reset and start performance tracking
        performanceTracker.startSession();

        // Load upcoming words into the queue
        List<Word> words = typingEngine.getRandomWords(wordCount);
        performanceTracker.addUpcomingWords(words);

        SessionResponse response = new SessionResponse(
                sessionId,
                "ACTIVE",
                String.format("Session started with %d words", wordCount));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/session/next-words - Get next words from the queue.
     */
    @GetMapping("/session/next-words")
    public ResponseEntity<WordResponse> getNextWords() {
        List<Word> upcomingWords = performanceTracker.getUpcomingWordsList();

        if (upcomingWords.isEmpty()) {
            return ResponseEntity.ok(new WordResponse(upcomingWords, "No more words in queue"));
        }

        WordResponse response = new WordResponse(upcomingWords,
                String.format("Next %d words from queue", upcomingWords.size()));

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/session/end - End the current session and get metrics.
     */
    @PostMapping("/session/end")
    public ResponseEntity<SessionResponse> endSession() {
        performanceTracker.endSession();

        SessionResponse response = new SessionResponse(
                "session-ended",
                "COMPLETED",
                performanceTracker.getMetrics());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/session/metrics - Get current session metrics.
     */
    @GetMapping("/session/metrics")
    public ResponseEntity<SessionResponse> getMetrics() {
        SessionResponse response = new SessionResponse(
                "current-session",
                "ACTIVE",
                performanceTracker.getMetrics());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/stats - Get word bank statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        return ResponseEntity.ok(new Object() {
            public final int totalWords = typingEngine.getWordBankSize();
            public final int easyWords = typingEngine.getWordsByDifficulty(Word.DifficultyLevel.EASY).size();
            public final int mediumWords = typingEngine.getWordsByDifficulty(Word.DifficultyLevel.MEDIUM).size();
            public final int hardWords = typingEngine.getWordsByDifficulty(Word.DifficultyLevel.HARD).size();
        });
    }

    /**
     * GET /api/game/text - Get text for Practice or Code mode.
     * Accepts lang parameter: java, javascript, or english (practice).
     * Accepts count parameter for word count in practice mode (AOOP Req 2 -
     * ArrayList).
     */
    @GetMapping("/game/text")
    public ResponseEntity<String> getGameText(
            @RequestParam(defaultValue = "english") String lang,
            @RequestParam(defaultValue = "50") int count) {
        try {
            String text;

            if ("java".equalsIgnoreCase(lang)) {
                // Load Java code snippet using FileReader (Req 3)
                text = snippetLoader.loadRandomSnippet("java");
                System.out.println("[TypingController] Loaded Java snippet");
            } else if ("javascript".equalsIgnoreCase(lang)) {
                // Load JavaScript code snippet using FileReader (Req 3)
                text = snippetLoader.loadRandomSnippet("javascript");
                System.out.println("[TypingController] Loaded JavaScript snippet");
            } else {
                // Practice mode - load exact word count from word bank ArrayList (Req 2)
                // Validate count parameter
                if (count < 1 || count > 200) {
                    count = 50; // Default to 50 if invalid
                }

                // Get exact number of words using ArrayList (AOOP Req 2)
                List<Word> words = typingEngine.getRandomWords(count);
                text = words.stream()
                        .map(Word::getText)
                        .collect(Collectors.joining(" "));

                System.out.println("[TypingController] Loaded practice text with " + words.size() + " words");
            }

            return ResponseEntity.ok(text);

        } catch (IOException e) {
            System.err.println("[TypingController] Error loading text: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading text: " + e.getMessage());
        }
    }

    /**
     * POST /api/scores - Save game score using ObjectOutputStream (Req 3).
     */
    @PostMapping("/scores")
    public ResponseEntity<Map<String, Object>> saveScore(@RequestBody Map<String, Object> scoreData) {
        try {
            String username = (String) scoreData.getOrDefault("username", "Anonymous");
            String mode = (String) scoreData.getOrDefault("mode", "PRACTICE");
            Double wpm = ((Number) scoreData.getOrDefault("wpm", 0.0)).doubleValue();
            Double accuracy = ((Number) scoreData.getOrDefault("accuracy", 100.0)).doubleValue();
            Integer wordsTyped = ((Number) scoreData.getOrDefault("wordsTyped", 0)).intValue();
            Long duration = ((Number) scoreData.getOrDefault("duration", 0L)).longValue();

            // Load existing stats or create new
            UserStats stats;
            try {
                stats = scoreManager.loadStats();
                if (stats == null || !stats.getUsername().equals(username)) {
                    stats = new UserStats(username);
                }
            } catch (Exception e) {
                stats = new UserStats(username);
            }

            // Add game record
            stats.addGameRecord(wpm, accuracy, wordsTyped, duration, mode);

            // Save using ObjectOutputStream (Req 3)
            scoreManager.saveStats(stats);

            System.out.println("[TypingController] Saved score for " + username +
                    ": " + wpm + " WPM, " + accuracy + "% accuracy");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Score saved successfully",
                    "bestWPM", stats.getBestWPM(),
                    "averageWPM", stats.getAverageWPM()));

        } catch (Exception e) {
            System.err.println("[TypingController] Error saving score: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error saving score: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error responses.
     */
    private WordResponse createErrorResponse(String message) {
        WordResponse response = new WordResponse();
        response.setMessage(message);
        response.setWords(List.of());
        return response;
    }
}
