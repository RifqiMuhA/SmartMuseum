package org.example.smartmuseum.model.interfaces;

public interface SystemObserver {
    void onBidPlaced(Object event);
    void onMessageReceived(Object event);
    void onAttendanceChanged(Object event);
}