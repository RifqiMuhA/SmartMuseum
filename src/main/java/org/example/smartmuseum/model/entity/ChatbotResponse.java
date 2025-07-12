package org.example.smartmuseum.model.entity;

/**
 * Response object that contains both message and sound file information
 */
public class ChatbotResponse {
    private String message;
    private String soundFile;

    public ChatbotResponse(String message) {
        this.message = message;
        this.soundFile = null; // No sound by default
    }

    public ChatbotResponse(String message, String soundFile) {
        this.message = message;
        this.soundFile = soundFile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSoundFile() {
        return soundFile;
    }

    public void setSoundFile(String soundFile) {
        this.soundFile = soundFile;
    }

    public boolean hasSound() {
        return soundFile != null && !soundFile.trim().isEmpty();
    }
}