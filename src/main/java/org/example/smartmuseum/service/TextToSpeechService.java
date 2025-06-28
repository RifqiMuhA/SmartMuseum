package org.example.smartmuseum.service;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.example.smartmuseum.model.entity.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class TextToSpeechService {
    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);
    private static TextToSpeechService instance;
    private Voice voice;
    private boolean isInitialized = false;
    private boolean isSpeaking = false;

    private TextToSpeechService() {
        initializeVoice();
    }

    public static synchronized TextToSpeechService getInstance() {
        if (instance == null) {
            instance = new TextToSpeechService();
        }
        return instance;
    }

    private void initializeVoice() {
        try {
            // Set system property for FreeTTS
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

            VoiceManager voiceManager = VoiceManager.getInstance();
            Voice[] voices = voiceManager.getVoices();

            if (voices.length > 0) {
                // Try to get Kevin voice (default FreeTTS voice)
                voice = voiceManager.getVoice("kevin16");
                if (voice == null) {
                    voice = voices[0]; // Use first available voice
                }

                if (voice != null) {
                    voice.allocate();
                    isInitialized = true;
                    logger.info("TTS Voice initialized: {}", voice.getName());
                } else {
                    logger.error("No TTS voice available");
                }
            } else {
                logger.error("No TTS voices found");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize TTS: {}", e.getMessage(), e);
        }
    }

    public String createArtworkNarration(Artwork artwork) {
        StringBuilder narration = new StringBuilder();

        narration.append("Welcome to SeniMatic. ");
        narration.append("You are looking at an artwork titled ");
        narration.append(artwork.getTitle()).append(". ");

        if (artwork.getArtistName() != null && !artwork.getArtistName().equals("Unknown Artist")) {
            narration.append("This artwork was created by ");
            narration.append(artwork.getArtistName()).append(". ");
        }

        narration.append("Created in the year ");
        narration.append(artwork.getYear()).append(". ");

        if (artwork.getArtworkType() != null) {
            narration.append("This is a ");
            narration.append(artwork.getArtworkType().toLowerCase()).append(". ");
        }

        if (artwork.getTechnique() != null && !artwork.getTechnique().trim().isEmpty()) {
            narration.append("Using technique ");
            narration.append(artwork.getTechnique()).append(". ");
        }

        if (artwork.getDescription() != null && !artwork.getDescription().trim().isEmpty()) {
            narration.append("Description of the artwork: ");
            narration.append(artwork.getDescription());
        }

        narration.append(" Thank you for visiting the SeniMatic");

        return narration.toString();
    }

    public CompletableFuture<Void> speakArtwork(Artwork artwork) {
        return CompletableFuture.runAsync(() -> {
            if (!isInitialized) {
                logger.warn("TTS not initialized");
                return;
            }

            if (isSpeaking) {
                stopSpeaking();
            }

            try {
                isSpeaking = true;
                String narration = createArtworkNarration(artwork);
                logger.info("Speaking: {}", narration.substring(0, Math.min(50, narration.length())) + "...");

                voice.speak(narration);

            } catch (Exception e) {
                logger.error("Error during speech: {}", e.getMessage(), e);
            } finally {
                isSpeaking = false;
            }
        });
    }

    public void stopSpeaking() {
        if (voice != null && isSpeaking) {
            try {
                voice.getAudioPlayer().cancel();
                isSpeaking = false;
                logger.info("Speech stopped");
            } catch (Exception e) {
                logger.error("Error stopping speech: {}", e.getMessage(), e);
            }
        }
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void shutdown() {
        if (voice != null) {
            try {
                stopSpeaking();
                voice.deallocate();
                logger.info("TTS service shutdown");
            } catch (Exception e) {
                logger.error("Error during TTS shutdown: {}", e.getMessage(), e);
            }
        }
    }
}
