package com.typinggame.domain;

/**
 * Enumeration of available game modes.
 * Each mode has different rules and mechanics.
 */
public enum GameMode {
    PRACTICE("Practice Mode", false, false, false),
    VS_BOT("Vs Bot Mode", true, false, false),
    VS_FRIEND("Vs Friend Mode", true, false, false),
    ELIMINATION("Elimination Mode", true, true, true);

    private final String displayName;
    private final boolean isMultiplayer;
    private final boolean hasHealthSystem;
    private final boolean hasAttackMechanic;

    GameMode(String displayName, boolean isMultiplayer, boolean hasHealthSystem, boolean hasAttackMechanic) {
        this.displayName = displayName;
        this.isMultiplayer = isMultiplayer;
        this.hasHealthSystem = hasHealthSystem;
        this.hasAttackMechanic = hasAttackMechanic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public boolean hasHealthSystem() {
        return hasHealthSystem;
    }

    public boolean hasAttackMechanic() {
        return hasAttackMechanic;
    }

    /**
     * Parse mode from string (case-insensitive).
     */
    public static GameMode fromString(String mode) {
        if (mode == null) {
            return PRACTICE;
        }

        try {
            return GameMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("[GameMode] Invalid mode: " + mode + ", defaulting to PRACTICE");
            return PRACTICE;
        }
    }
}
