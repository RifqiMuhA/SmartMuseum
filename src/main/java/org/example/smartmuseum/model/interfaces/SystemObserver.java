package org.example.smartmuseum.model.interfaces;

/**
 * Observer interface for system events
 * Implements Observer pattern for real-time updates
 */
public interface SystemObserver {
    /**
     * Handle bid placement events
     * @param event Bid event details
     */
    void onBidPlaced(Object event);

    /**
     * Handle attendance change events
     * @param event Attendance event details
     */
    void onAttendanceChanged(Object event);
}