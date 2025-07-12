package org.example.smartmuseum.model.service;

import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Timer management for auction countdown system
 * Handles 30-second countdown with reset functionality when bids are placed
 */
public class AuctionTimer {
    private static final int INITIAL_TIME_SECONDS = 30;
    private static final int WARNING_TIME_SECONDS = 10;
    private static final int CRITICAL_TIME_SECONDS = 5;

    private final Map<Integer, AuctionTimerData> activeTimers = new ConcurrentHashMap<>();

    public static class AuctionTimerData {
        private Timer timer;
        private int remainingSeconds;
        private boolean isRunning;
        private Consumer<Integer> onTimeUpdate;
        private Consumer<Integer> onTimeEnd;
        private Runnable onWarning;
        private Runnable onCritical;

        public AuctionTimerData() {
            this.remainingSeconds = INITIAL_TIME_SECONDS;
            this.isRunning = false;
        }

        // Getters and setters
        public Timer getTimer() { return timer; }
        public void setTimer(Timer timer) { this.timer = timer; }

        public int getRemainingSeconds() { return remainingSeconds; }
        public void setRemainingSeconds(int remainingSeconds) { this.remainingSeconds = remainingSeconds; }

        public boolean isRunning() { return isRunning; }
        public void setRunning(boolean running) { isRunning = running; }

        public Consumer<Integer> getOnTimeUpdate() { return onTimeUpdate; }
        public void setOnTimeUpdate(Consumer<Integer> onTimeUpdate) { this.onTimeUpdate = onTimeUpdate; }

        public Consumer<Integer> getOnTimeEnd() { return onTimeEnd; }
        public void setOnTimeEnd(Consumer<Integer> onTimeEnd) { this.onTimeEnd = onTimeEnd; }

        public Runnable getOnWarning() { return onWarning; }
        public void setOnWarning(Runnable onWarning) { this.onWarning = onWarning; }

        public Runnable getOnCritical() { return onCritical; }
        public void setOnCritical(Runnable onCritical) { this.onCritical = onCritical; }
    }

    public void startTimer(int auctionId,
                           Consumer<Integer> onTimeUpdate,
                           Consumer<Integer> onTimeEnd,
                           Runnable onWarning,
                           Runnable onCritical) {

        System.out.println("🕐 Starting timer for auction " + auctionId);

        // Stop existing timer if any
        stopTimer(auctionId);

        AuctionTimerData timerData = new AuctionTimerData();
        timerData.setOnTimeUpdate(onTimeUpdate);
        timerData.setOnTimeEnd(onTimeEnd);
        timerData.setOnWarning(onWarning);
        timerData.setOnCritical(onCritical);
        timerData.setRunning(true);

        Timer timer = new Timer(true);
        timerData.setTimer(timer);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                AuctionTimerData data = activeTimers.get(auctionId);
                if (data == null || !data.isRunning()) {
                    this.cancel();
                    return;
                }

                Platform.runLater(() -> {
                    try {
                        int remaining = data.getRemainingSeconds();

                        // Trigger callbacks
                        if (data.getOnTimeUpdate() != null) {
                            data.getOnTimeUpdate().accept(remaining);
                        }

                        // Warning and critical alerts
                        if (remaining == WARNING_TIME_SECONDS && data.getOnWarning() != null) {
                            data.getOnWarning().run();
                        } else if (remaining == CRITICAL_TIME_SECONDS && data.getOnCritical() != null) {
                            data.getOnCritical().run();
                        }

                        // Check if time is up
                        if (remaining <= 0) {
                            if (data.getOnTimeEnd() != null) {
                                data.getOnTimeEnd().accept(auctionId);
                            }
                            stopTimer(auctionId);
                            return;
                        }

                        // Decrement time
                        data.setRemainingSeconds(remaining - 1);

                    } catch (Exception e) {
                        System.err.println("Error in auction timer task: " + e.getMessage());
                        stopTimer(auctionId);
                    }
                });
            }
        };

        activeTimers.put(auctionId, timerData);
        timer.scheduleAtFixedRate(task, 0, 1000); // Execute every second

        System.out.println("✅ Timer started for auction " + auctionId);
    }

    public void resetTimer(int auctionId) {
        AuctionTimerData timerData = activeTimers.get(auctionId);
        if (timerData != null && timerData.isRunning()) {
            timerData.setRemainingSeconds(INITIAL_TIME_SECONDS);
            System.out.println("🔄 Timer reset for auction " + auctionId + " - back to " + INITIAL_TIME_SECONDS + " seconds");
        } else {
            System.out.println("⚠️ Attempted to reset timer for inactive auction " + auctionId);
        }
    }

    public void stopTimer(int auctionId) {
        AuctionTimerData timerData = activeTimers.remove(auctionId);
        if (timerData != null) {
            timerData.setRunning(false);
            if (timerData.getTimer() != null) {
                timerData.getTimer().cancel();
            }
            System.out.println("⏹️ Timer stopped for auction " + auctionId);
        }
    }

    public int getRemainingTime(int auctionId) {
        AuctionTimerData timerData = activeTimers.get(auctionId);
        return timerData != null ? timerData.getRemainingSeconds() : 0;
    }

    public boolean isTimerRunning(int auctionId) {
        AuctionTimerData timerData = activeTimers.get(auctionId);
        return timerData != null && timerData.isRunning();
    }

    public Map<Integer, Integer> getAllActiveTimers() {
        Map<Integer, Integer> result = new ConcurrentHashMap<>();
        activeTimers.forEach((auctionId, data) -> {
            if (data.isRunning()) {
                result.put(auctionId, data.getRemainingSeconds());
            }
        });
        return result;
    }

    public void stopAllTimers() {
        System.out.println("🛑 Stopping all auction timers");
        activeTimers.forEach((auctionId, data) -> {
            data.setRunning(false);
            if (data.getTimer() != null) {
                data.getTimer().cancel();
            }
        });
        activeTimers.clear();
    }

    public String getTimerStatus() {
        StringBuilder status = new StringBuilder("Active Auction Timers:\n");
        if (activeTimers.isEmpty()) {
            status.append("No active timers");
        } else {
            activeTimers.forEach((auctionId, data) -> {
                status.append(String.format("Auction %d: %d seconds remaining (Running: %s)\n",
                        auctionId, data.getRemainingSeconds(), data.isRunning()));
            });
        }
        return status.toString();
    }
}