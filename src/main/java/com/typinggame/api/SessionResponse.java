package com.typinggame.api;

import com.typinggame.engine.PerformanceTracker.PerformanceMetrics;

/**
 * Data Transfer Object for session responses.
 */
public class SessionResponse {
    private String sessionId;
    private String status;
    private PerformanceMetrics metrics;
    private String message;

    public SessionResponse() {
    }

    public SessionResponse(String sessionId, String status, String message) {
        this.sessionId = sessionId;
        this.status = status;
        this.message = message;
    }

    public SessionResponse(String sessionId, String status, PerformanceMetrics metrics) {
        this.sessionId = sessionId;
        this.status = status;
        this.metrics = metrics;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(PerformanceMetrics metrics) {
        this.metrics = metrics;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
