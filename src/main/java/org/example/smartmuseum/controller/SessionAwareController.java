package org.example.smartmuseum.controller;

import org.example.smartmuseum.util.SessionContext;

/**
 * Interface untuk controllers yang membutuhkan session context
 * Implementasi ini memungkinkan injection SessionContext ke controller
 */
public interface SessionAwareController {

    /**
     * Set session context untuk controller ini
     * Method ini dipanggil setelah FXML loaded tapi sebelum initialize()
     */
    void setSessionContext(SessionContext sessionContext);

    /**
     * Get session context dari controller ini
     */
    SessionContext getSessionContext();

    /**
     * Cleanup method yang dipanggil saat window ditutup
     */
    default void cleanup() {
        SessionContext context = getSessionContext();
        if (context != null) {
            context.cleanup();
        }
    }
}