package com.typinggame.bot;

import java.util.Comparator;

/**
 * Bot difficulty levels for Vs Bot mode.
 * Each difficulty has a target WPM.
 */
public enum BotDifficulty {
    EASY("Easy Bot", 30.0),
    MEDIUM("Medium Bot", 50.0),
    HARD("Hard Bot", 70.0),
    EXPERT("Expert Bot", 90.0);

    private final String displayName;
    private final double targetWPM;

    BotDifficulty(String displayName, double targetWPM) {
        this.displayName = displayName;
        this.targetWPM = targetWPM;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getTargetWPM() {
        return targetWPM;
    }

    /**
     * Parse difficulty from string.
     */
    public static BotDifficulty fromString(String difficulty) {
        if (difficulty == null) {
            return MEDIUM;
        }

        try {
            return BotDifficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }

    /**
     * Get comparator for sorting difficulties by WPM.
     */
    public static Comparator<BotDifficulty> getComparator() {
        return Comparator.comparingDouble(BotDifficulty::getTargetWPM);
    }
}
