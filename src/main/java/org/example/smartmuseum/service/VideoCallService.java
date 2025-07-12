package org.example.smartmuseum.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Logger;

public class VideoCallService {

    private static final Logger LOGGER = Logger.getLogger(VideoCallService.class.getName());

    // Base URL untuk video call yang sudah di-hosting
    private static final String BASE_VIDEO_CALL_URL = "http://vidcall.project2ks2.my.id/vidcall.html";

    public String generateRoomId() {
        return "senimatic_" + (new Random().nextInt(9999) + 1000);
    }

    /**
     * Generate URL untuk video call dengan roomID yang spesifik
     * @param roomId Room ID untuk video conference
     * @return Complete URL untuk load di WebView
     */
    public String generateVideoCallURL(String roomId) {
        try {
            String encodedRoomId = URLEncoder.encode(roomId, StandardCharsets.UTF_8.toString());
            String url = BASE_VIDEO_CALL_URL + "?roomID=" + encodedRoomId;
            LOGGER.info("Generated video call URL: " + url);
            return url;
        } catch (UnsupportedEncodingException e) {
            LOGGER.warning("Failed to encode room ID, using as-is: " + e.getMessage());
            String url = BASE_VIDEO_CALL_URL + "?roomID=" + roomId;
            LOGGER.info("Generated video call URL (fallback): " + url);
            return url;
        }
    }


    /**
     * Test if CEF can access the video call URL
     */
    public boolean testCefCompatibility(String url) {
        try {
            java.net.URL testUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            int responseCode = connection.getResponseCode();
            LOGGER.info("CEF compatibility test for " + url + " - HTTP " + responseCode);

            return responseCode == 200;

        } catch (Exception e) {
            LOGGER.warning("CEF compatibility test failed for " + url + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate URL with CEF-specific parameters
     */
    public String generateCefOptimizedURL(String roomId, String userId, String userName) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_CALL_URL);
            urlBuilder.append("?roomID=").append(URLEncoder.encode(roomId, StandardCharsets.UTF_8.toString()));

            if (userId != null && !userId.trim().isEmpty()) {
                urlBuilder.append("&userID=").append(URLEncoder.encode(userId, StandardCharsets.UTF_8.toString()));
            }

            if (userName != null && !userName.trim().isEmpty()) {
                urlBuilder.append("&userName=").append(URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()));
            }

            // Add CEF-specific parameters
            urlBuilder.append("&browser=cef");
            urlBuilder.append("&webrtc=enabled");
            urlBuilder.append("&autoplay=true");

            String url = urlBuilder.toString();
            LOGGER.info("Generated CEF-optimized video call URL: " + url);
            return url;

        } catch (UnsupportedEncodingException e) {
            LOGGER.warning("Failed to encode parameters for CEF URL: " + e.getMessage());
            return generateVideoCallURL(roomId, userId, userName); // Fallback
        }
    }

    /**
     * Generate URL dengan parameter tambahan
     * @param roomId Room ID
     * @param userId User ID (opsional)
     * @param userName User Name (opsional)
     * @return Complete URL dengan parameters
     */
    public String generateVideoCallURL(String roomId, String userId, String userName) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_CALL_URL);
            urlBuilder.append("?roomID=").append(URLEncoder.encode(roomId, StandardCharsets.UTF_8.toString()));

            if (userId != null && !userId.trim().isEmpty()) {
                urlBuilder.append("&userID=").append(URLEncoder.encode(userId, StandardCharsets.UTF_8.toString()));
            }

            if (userName != null && !userName.trim().isEmpty()) {
                urlBuilder.append("&userName=").append(URLEncoder.encode(userName, StandardCharsets.UTF_8.toString()));
            }

            String url = urlBuilder.toString();
            LOGGER.info("Generated video call URL with params: " + url);
            return url;

        } catch (UnsupportedEncodingException e) {
            LOGGER.warning("Failed to encode parameters, using fallback encoding: " + e.getMessage());
            // Fallback encoding
            StringBuilder urlBuilder = new StringBuilder(BASE_VIDEO_CALL_URL);
            urlBuilder.append("?roomID=").append(roomId.replace(" ", "%20").replace("&", "%26"));

            if (userId != null && !userId.trim().isEmpty()) {
                urlBuilder.append("&userID=").append(userId.replace(" ", "%20").replace("&", "%26"));
            }

            if (userName != null && !userName.trim().isEmpty()) {
                urlBuilder.append("&userName=").append(userName.replace(" ", "%20").replace("&", "%26"));
            }

            String url = urlBuilder.toString();
            LOGGER.info("Generated video call URL with params (fallback): " + url);
            return url;
        }
    }

    /**
     * Validate room ID format
     * @param roomId Room ID to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidRoomId(String roomId) {
        return roomId != null &&
                !roomId.trim().isEmpty() &&
                roomId.trim().length() >= 3 &&
                roomId.trim().length() <= 50;
    }

    /**
     * Clean room ID (remove invalid characters)
     * @param roomId Raw room ID
     * @return Cleaned room ID
     */
    public String cleanRoomId(String roomId) {
        if (roomId == null) return "";
        return roomId.trim().replaceAll("[^a-zA-Z0-9_-]", "");
    }

    /**
     * Get base URL for testing purposes
     * @return Base URL
     */
    public String getBaseURL() {
        return BASE_VIDEO_CALL_URL;
    }

    /**
     * Test URL accessibility
     * @param url URL to test
     * @return true if accessible, false otherwise
     */
    public boolean testURLAccessibility(String url) {
        try {
            java.net.URL testUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();
            LOGGER.info("URL accessibility test for " + url + " - HTTP " + responseCode);

            return responseCode == 200;

        } catch (Exception e) {
            LOGGER.warning("URL accessibility test failed for " + url + ": " + e.getMessage());
            return false;
        }
    }
}