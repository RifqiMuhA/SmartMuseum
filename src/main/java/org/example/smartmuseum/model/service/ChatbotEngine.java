package org.example.smartmuseum.model.service;

import org.example.smartmuseum.model.entity.UserChatSession;
import org.example.smartmuseum.model.concrete.Visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chatbot engine for processing conversations
 */
public class ChatbotEngine {

    private Map<Integer, UserChatSession> activeSessions;
    private Map<String, String> responses;
    private ChatbotService chatbotService;

    public ChatbotEngine() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.responses = new HashMap<>();
        this.chatbotService = new ChatbotService();
        initializeResponses();
    }

    private void initializeResponses() {
        responses.put("greeting", "Selamat datang di SeniMatic! Bagaimana saya bisa membantu Anda?");
        responses.put("artwork_info", "Berikut informasi artwork yang tersedia...");
        responses.put("auction_guide", "Panduan mengikuti lelang...");
        responses.put("gallery_info", "Informasi galeri dan jam operasional...");
        responses.put("technical_support", "Tim teknis siap membantu Anda...");
        responses.put("goodbye", "Terima kasih telah menggunakan SeniMatic. Sampai jumpa!");
        responses.put("default", "Maaf, saya tidak mengerti. Bisa diulang?");
    }

    public String processMessage(Visitor visitor, String message) {
        if (visitor == null || message == null || message.trim().isEmpty()) {
            return responses.get("default");
        }

        // Get or create session
        UserChatSession session = getOrCreateSession(visitor);

        // Update session activity
        session.updateActivity();

        // Process the message
        return generateResponse(message, session);
    }

    private UserChatSession getOrCreateSession(Visitor visitor) {
        UserChatSession session = activeSessions.get(visitor.getVisitorId());

        if (session == null) {
            session = new UserChatSession(visitor.getVisitorId());
            activeSessions.put(visitor.getVisitorId(), session);
            visitor.setChatSession(session);
        }

        return session;
    }

    private String generateResponse(String message, UserChatSession session) {
        String lowerMessage = message.toLowerCase().trim();

        // Handle greetings
        if (lowerMessage.contains("halo") || lowerMessage.contains("hai") ||
                lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return responses.get("greeting");
        }

        // Handle goodbyes
        if (lowerMessage.contains("bye") || lowerMessage.contains("selamat tinggal") ||
                lowerMessage.contains("terima kasih")) {
            return responses.get("goodbye");
        }

        // Handle menu selections
        try {
            int choice = Integer.parseInt(lowerMessage);
            return handleMenuChoice(choice, session);
        } catch (NumberFormatException e) {
            // Handle text-based queries
            return handleTextQuery(lowerMessage, session);
        }
    }

    private String handleMenuChoice(int choice, UserChatSession session) {
        switch (choice) {
            case 1:
                session.setCurrentFlow("artwork_info");
                return "        Informasi Artwork\n\n" +
                        "Pilih kategori:\n" +
                        "1. Cari berdasarkan seniman\n" +
                        "2. Cari berdasarkan kategori\n" +
                        "3. Artwork terpopuler\n\n" +
                        "Ketik nomor pilihan:";

            case 2:
                session.setCurrentFlow("auction_guide");
                return "       Cara Mengikuti Lelang\n\n" +
                        "Langkah-langkah:\n" +
                        "1. Registrasi akun\n" +
                        "2. Verifikasi identitas\n" +
                        "3. Deposit jaminan\n" +
                        "4. Mulai bidding\n\n" +
                        "Pilih nomor untuk detail:";

            case 3:
                session.setCurrentFlow("gallery_info");
                return "        Info Galeri\n\n" +
                        "Jam Operasional: 09:00 - 17:00\n" +
                        "Lokasi: Jl. Seni Raya No. 123\n" +
                        "Telp: (021) 1234-5678\n" +
                        "Email: info@senimatic.com";

            case 4:
                session.setCurrentFlow("technical_support");
                return "    Bantuan Teknis\n\n" +
                        "1. Masalah login\n" +
                        "2. Reset password\n" +
                        "3. Hubungi admin\n\n" +
                        "Ketik nomor untuk bantuan:";

            default:
                return "Pilihan tidak valid. Silakan ketik nomor 1-4.";
        }
    }

    private String handleTextQuery(String query, UserChatSession session) {
        if (query.contains("artwork") || query.contains("seni")) {
            return responses.get("artwork_info");
        } else if (query.contains("lelang") || query.contains("auction")) {
            return responses.get("auction_guide");
        } else if (query.contains("galeri") || query.contains("museum")) {
            return responses.get("gallery_info");
        } else if (query.contains("bantuan") || query.contains("help")) {
            return responses.get("technical_support");
        } else {
            return responses.get("default");
        }
    }

    public void endSession(int visitorId) {
        UserChatSession session = activeSessions.remove(visitorId);
        if (session != null) {
            session.endSession();
        }
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    public UserChatSession getSession(int visitorId) {
        return activeSessions.get(visitorId);
    }
}
