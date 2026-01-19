package com.typinggame.bot;

import java.util.Comparator;

/**
 * Comparator to compare User's WPM against Bot's WPM.
 * Used to determine the winner in Vs Bot mode.
 */
public class WPMComparator implements Comparator<WPMComparator.PlayerResult> {

    /**
     * Container class for player results.
     */
    public static class PlayerResult {
        private final String playerName;
        private final int wordsCompleted;
        private final double wpm;
        private final double accuracy;
        private final long duration;

        public PlayerResult(String playerName, int wordsCompleted, double wpm,
                double accuracy, long duration) {
            this.playerName = playerName;
            this.wordsCompleted = wordsCompleted;
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.duration = duration;
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
            return String.format("%s: %d words, %.1f WPM, %.1f%% accuracy, %.1fs",
                    playerName, wordsCompleted, wpm, accuracy, duration / 1000.0);
        }
    }

    @Override
    public int compare(PlayerResult p1, PlayerResult p2) {
        // Primary: Compare by words completed
        int wordsComparison = Integer.compare(p1.getWordsCompleted(), p2.getWordsCompleted());
        if (wordsComparison != 0) {
            return wordsComparison;
        }

        // Secondary: Compare by WPM
        return Double.compare(p1.getWpm(), p2.getWpm());
    }

    /**
     * Determine winner between user and bot.
     * 
     * @param userResult User's result
     * @param botResult  Bot's result
     * @return "USER", "BOT", or "TIE"
     */
    public String determineWinner(PlayerResult userResult, PlayerResult botResult) {
        int comparison = compare(userResult, botResult);

        if (comparison > 0) {
            return "USER";
        } else if (comparison < 0) {
            return "BOT";
        } else {
            return "TIE";
        }
    }
}
