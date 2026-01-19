package com.typinggame.network;

/**
 * Tracks player health in Elimination Mode.
 * Health decrements on wrong word submissions.
 */
public class PlayerHealth {

    private static final int INITIAL_HEALTH = 3;

    private final String playerId;
    private int currentHealth;
    private int totalMistakes;
    private boolean isEliminated;

    public PlayerHealth(String playerId) {
        this.playerId = playerId;
        this.currentHealth = INITIAL_HEALTH;
        this.totalMistakes = 0;
        this.isEliminated = false;
    }

    /**
     * Decrement health when player makes a mistake.
     * 
     * @return true if player is eliminated (health reached 0)
     */
    public synchronized boolean decrementHealth() {
        if (isEliminated) {
            return true;
        }

        currentHealth--;
        totalMistakes++;

        System.out.println("[PlayerHealth] Player " + playerId + " lost a life. " +
                "Health: " + currentHealth + "/" + INITIAL_HEALTH);

        if (currentHealth <= 0) {
            isEliminated = true;
            System.out.println("[PlayerHealth] Player " + playerId + " ELIMINATED!");
            return true;
        }

        return false;
    }

    /**
     * Reset health to initial value.
     */
    public synchronized void reset() {
        currentHealth = INITIAL_HEALTH;
        totalMistakes = 0;
        isEliminated = false;
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public synchronized int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return INITIAL_HEALTH;
    }

    public synchronized int getTotalMistakes() {
        return totalMistakes;
    }

    public synchronized boolean isEliminated() {
        return isEliminated;
    }

    public synchronized double getHealthPercentage() {
        return (currentHealth * 100.0) / INITIAL_HEALTH;
    }

    @Override
    public String toString() {
        return String.format("PlayerHealth{playerId='%s', health=%d/%d, mistakes=%d, eliminated=%b}",
                playerId, currentHealth, INITIAL_HEALTH, totalMistakes, isEliminated);
    }
}
