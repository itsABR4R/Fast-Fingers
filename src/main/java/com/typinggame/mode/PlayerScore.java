package com.typinggame.mode;

/**
 * Player score for Vs Friend mode.
 * Implements Comparable to compare scores for winner determination.
 */
public class PlayerScore implements Comparable<PlayerScore> {

    private final String playerId;
    private final String playerName;
    private final int wordsCompleted;
    private final double wpm;
    private final double accuracy;
    private final long duration;

    public PlayerScore(String playerId, String playerName, int wordsCompleted,
            double wpm, double accuracy, long duration) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.wordsCompleted = wordsCompleted;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.duration = duration;
    }

    /**
     * Compare scores for Vs Friend mode.
     * Primary: Words completed
     * Secondary: WPM (higher is better)
     * Tertiary: Accuracy (higher is better)
     */
    @Override
    public int compareTo(PlayerScore other) {
        // Primary: Compare by words completed
        int wordsComparison = Integer.compare(this.wordsCompleted, other.wordsCompleted);
        if (wordsComparison != 0) {
            return wordsComparison;
        }

        // Secondary: Compare by WPM (higher is better)
        int wpmComparison = Double.compare(this.wpm, other.wpm);
        if (wpmComparison != 0) {
            return wpmComparison;
        }

        // Tertiary: Compare by accuracy (higher is better)
        return Double.compare(this.accuracy, other.accuracy);
    }

    /**
     * Determine winner between two players.
     */
    public static String determineWinner(PlayerScore player1, PlayerScore player2) {
        int comparison = player1.compareTo(player2);

        if (comparison > 0) {
            return player1.getPlayerId();
        } else if (comparison < 0) {
            return player2.getPlayerId();
        } else {
            return "TIE";
        }
    }

    /**
     * Get winner with detailed reason.
     */
    public static WinnerResult getWinnerWithReason(PlayerScore player1, PlayerScore player2) {
        int comparison = player1.compareTo(player2);

        if (comparison > 0) {
            String reason = determineWinReason(player1, player2);
            return new WinnerResult(player1.getPlayerId(), player1.getPlayerName(), reason);
        } else if (comparison < 0) {
            String reason = determineWinReason(player2, player1);
            return new WinnerResult(player2.getPlayerId(), player2.getPlayerName(), reason);
        } else {
            return new WinnerResult("TIE", "Tie", "Both players had identical scores!");
        }
    }

    /**
     * Determine the reason for winning.
     */
    private static String determineWinReason(PlayerScore winner, PlayerScore loser) {
        if (winner.wordsCompleted > loser.wordsCompleted) {
            return String.format("Completed %d words vs %d", winner.wordsCompleted, loser.wordsCompleted);
        } else if (winner.wpm > loser.wpm) {
            return String.format("Higher WPM: %.1f vs %.1f", winner.wpm, loser.wpm);
        } else if (winner.accuracy > loser.accuracy) {
            return String.format("Better accuracy: %.1f%% vs %.1f%%", winner.accuracy, loser.accuracy);
        }
        return "Better overall performance";
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getWordsCompleted() {
        return wordsCompleted;
    }

    public double getWpm() {
        return wpm;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("%s: %d words, %.1f WPM, %.1f%% accuracy",
                playerName, wordsCompleted, wpm, accuracy);
    }

    /**
     * Winner result with reason.
     */
    public static class WinnerResult {
        private final String winnerId;
        private final String winnerName;
        private final String reason;

        public WinnerResult(String winnerId, String winnerName, String reason) {
            this.winnerId = winnerId;
            this.winnerName = winnerName;
            this.reason = reason;
        }

        public String getWinnerId() {
            return winnerId;
        }

        public String getWinnerName() {
            return winnerName;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return String.format("%s wins! Reason: %s", winnerName, reason);
        }
    }
}
