package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.ChatbotDAO;
import org.example.smartmuseum.model.entity.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced chatbot service using database for conversations
 */
public class ChatbotService {

    private Map<Integer, UserChatSession> activeSessions;
    private ChatbotDAO chatbotDAO;
    private Map<String, ConversationFlow> loadedFlows;

    public ChatbotService() {
        this.activeSessions = new HashMap<>();
        this.chatbotDAO = new ChatbotDAO();
        this.loadedFlows = new HashMap<>();
    }

    /**
     * Load conversation flows from database
     */
    public void loadConversationFlows() {
        try {
            // Load main conversation flow
            ConversationFlow mainFlow = chatbotDAO.getConversationFlow("main");
            if (mainFlow != null) {
                loadedFlows.put("main", mainFlow);
                System.out.println("Main conversation flow loaded from database");
            } else {
                System.out.println("No main conversation flow found in database, using fallback");
                createFallbackFlow();
            }
        } catch (Exception e) {
            System.err.println("Error loading conversation flows: " + e.getMessage());
            createFallbackFlow();
        }
    }

    /**
     * Create fallback conversation flow when database is not available
     */
    private void createFallbackFlow() {
        ConversationFlow fallbackFlow = new ConversationFlow("main", "Fallback conversation flow");
        fallbackFlow.setFlowId(1);
        fallbackFlow.setStartNodeId(1);
        loadedFlows.put("main", fallbackFlow);
        System.out.println("Fallback conversation flow created");
    }

    /**
     * Initialize chat session for user
     */
    public UserChatSession initializeChatSession(int userId) {
        try {
            // Try to create session in database
            UserChatSession session = chatbotDAO.createChatSession(userId);
            if (session != null) {
                activeSessions.put(userId, session);
                System.out.println("Chat session created in database with ID: " + session.getSessionId());
                return session;
            }
        } catch (Exception e) {
            System.err.println("Error creating database session: " + e.getMessage());
        }

        // Fallback to in-memory session
        UserChatSession session = new UserChatSession(userId);
        session.setSessionId(userId); // Use userId as sessionId for fallback
        activeSessions.put(userId, session);
        System.out.println("Fallback in-memory session created for user: " + userId);
        return session;
    }

    /**
     * Generate response only (without saving to database)
     */
    public String generateResponse(int userId, String input) {
        UserChatSession session = activeSessions.get(userId);
        if (session == null) {
            session = initializeChatSession(userId);
        }

        session.updateActivity();

        // Generate response based on current flow and input
        String response = generateContextualResponse(input, session);

        // Update session in database
        updateSessionInDatabase(session);

        return response;
    }

    /**
     * Generate response based on current context/flow
     */
    private String generateContextualResponse(String input, UserChatSession session) {
        try {
            String currentFlow = session.getCurrentFlow();
            System.out.println("       PROCESSING INPUT: '" + input + "' (Current Flow: " + currentFlow + ")");

            // Handle menu selections based on current flow
            try {
                int choice = Integer.parseInt(input.trim());
                return handleContextualMenuChoice(choice, session, currentFlow);
            } catch (NumberFormatException e) {
                // Handle text input
                return handleTextInput(input, session);
            }

        } catch (Exception e) {
            System.err.println("Error generating contextual response: " + e.getMessage());
            return getFallbackResponse(input, session);
        }
    }

    /**
     * Handle menu choice based on current context/flow
     */
    private String handleContextualMenuChoice(int choice, UserChatSession session, String currentFlow) {
        System.out.println("       PROCESSING MENU CHOICE: " + choice + " in flow: " + currentFlow);

        // Handle based on current flow context
        switch (currentFlow) {
            case "welcome":
                return handleMainMenuChoice(choice, session);

            case "artwork_info":
                return handleArtworkMenuChoice(choice, session);

            case "auction_guide":
                return handleAuctionMenuChoice(choice, session);

            case "technical_support":
                return handleTechnicalSupportChoice(choice, session);

            default:
                System.out.println("    UNKNOWN FLOW: " + currentFlow + ", treating as main menu");
                return handleMainMenuChoice(choice, session);
        }
    }

    /**
     * Handle main menu choices (1-4)
     */
    private String handleMainMenuChoice(int choice, UserChatSession session) {
        switch (choice) {
            case 1:
                session.setCurrentFlow("artwork_info");
                System.out.println("     SWITCHED TO FLOW: artwork_info");
                return "        Informasi Artwork\n\n" +
                        "Pilih kategori:\n" +
                    "1. Cari berdasarkan seniman\n" +
                    "2. Cari berdasarkan kategori\n" +
                    "3. Artwork terpopuler\n\n" +
                    "Ketik nomor pilihan:";

            case 2:
                session.setCurrentFlow("auction_guide");
                System.out.println("     SWITCHED TO FLOW: auction_guide");
                return "       Cara Mengikuti Lelang\n\n" +
                        "Langkah-langkah:\n" +
                        "1. Registrasi akun\n" +
                        "2. Verifikasi identitas\n" +
                        "3. Deposit jaminan\n" +
                        "4. Mulai bidding\n\n" +
                        "Pilih nomor untuk detail:";

            case 3:
                session.setCurrentFlow("gallery_info");
                System.out.println("     SWITCHED TO FLOW: gallery_info");
                return "        Info Galeri\n\n" +
                        "Jam Operasional: 09:00 - 17:00\n" +
                        "Lokasi: Jl. Seni Raya No. 123\n" +
                        "Telp: (021) 1234-5678\n" +
                        "Email: info@senimatic.com\n\n" +
                        "Ketik 'menu' untuk kembali ke menu utama.";

            case 4:
                session.setCurrentFlow("technical_support");
                System.out.println("     SWITCHED TO FLOW: technical_support");
                return "    Bantuan Teknis\n\n" +
                        "1. Masalah login\n" +
                        "2. Reset password\n" +
                        "3. Hubungi admin\n\n" +
                        "Ketik nomor untuk bantuan:";

            default:
                System.out.println("    INVALID MAIN MENU CHOICE: " + choice);
                return "Pilihan tidak valid. Silakan ketik nomor 1-4 untuk memilih menu.";
        }
    }

    /**
     * Handle artwork info submenu choices
     */
    private String handleArtworkMenuChoice(int choice, UserChatSession session) {
        System.out.println("     HANDLING ARTWORK SUBMENU CHOICE: " + choice);

        switch (choice) {
            case 1:
                return "         Cari Berdasarkan Seniman\n\n" +
                        "Seniman terkenal di koleksi kami:\n" +
                        "• Affandi - Pelukis ekspresionisme\n" +
                        "• Raden Saleh - Pelukis romantisme\n" +
                        "• Basuki Abdullah - Pelukis realis\n" +
                        "• Sudjojono - Pelukis naturalis\n\n" +
                        "Ketik nama seniman atau 'menu' untuk kembali.";

            case 2:
                return "          Cari Berdasarkan Kategori\n\n" +
                        "Kategori artwork:\n" +
                        "• Lukisan - Karya seni rupa 2D\n" +
                        "• Patung - Karya seni rupa 3D\n" +
                        "• Keramik - Seni kerajinan tanah liat\n" +
                        "• Batik - Seni tekstil tradisional\n\n" +
                        "Ketik kategori atau 'menu' untuk kembali.";

            case 3:
                return "    Artwork Terpopuler\n\n" +
                        "Top 3 artwork favorit pengunjung:\n" +
                        "1. 'Pemandangan Borobudur' - Affandi\n" +
                        "2. 'Penangkapan Diponegoro' - Raden Saleh\n" +
                        "3. 'Gadis Bali' - Basuki Abdullah\n\n" +
                        "Ketik 'menu' untuk kembali ke menu utama.";

            default:
                return "Pilihan tidak valid untuk menu artwork. Ketik 1-3 atau 'menu' untuk kembali.";
        }
    }

    /**
     * Handle auction guide submenu choices
     */
    private String handleAuctionMenuChoice(int choice, UserChatSession session) {
        System.out.println("     HANDLING AUCTION SUBMENU CHOICE: " + choice);

        switch (choice) {
            case 1:
                return "           Registrasi Akun\n\n" +
                        "Langkah registrasi:\n" +
                        "1. Kunjungi halaman registrasi\n" +
                        "2. Isi data pribadi lengkap\n" +
                        "3. Upload foto KTP/identitas\n" +
                        "4. Verifikasi email\n" +
                        "5. Tunggu konfirmasi admin\n\n" +
                        "Ketik 'menu' untuk kembali.";

            case 2:
                return "     Verifikasi Identitas\n\n" +
                        "Dokumen yang diperlukan:\n" +
                        "• KTP/Paspor yang masih berlaku\n" +
                        "• NPWP (untuk lelang >50 juta)\n" +
                        "• Surat keterangan domisili\n" +
                        "• Foto selfie dengan KTP\n\n" +
                        "Proses verifikasi: 1-3 hari kerja\n" +
                        "Ketik 'menu' untuk kembali.";

            case 3:
                return "      Deposit Jaminan\n\n" +
                        "Ketentuan deposit:\n" +
                        "• Minimal 10% dari nilai lelang\n" +
                        "• Transfer ke rekening resmi\n" +
                        "• Deposit dikembalikan jika tidak menang\n" +
                        "• Berlaku untuk 1 sesi lelang\n\n" +
                        "Ketik 'menu' untuk kembali.";

            case 4:
                return "     Mulai Bidding\n\n" +
                        "Cara bidding:\n" +
                        "1. Pilih artwork yang diminati\n" +
                        "2. Tentukan batas maksimal bid\n" +
                        "3. Klik tombol 'Bid Now'\n" +
                        "4. Konfirmasi bid amount\n" +
                        "5. Tunggu hasil lelang\n\n" +
                        "Ketik 'menu' untuk kembali.";

            default:
                return "Pilihan tidak valid untuk menu lelang. Ketik 1-4 atau 'menu' untuk kembali.";
        }
    }

    /**
     * Handle technical support submenu choices
     */
    private String handleTechnicalSupportChoice(int choice, UserChatSession session) {
        System.out.println("     HANDLING TECHNICAL SUPPORT CHOICE: " + choice);

        switch (choice) {
            case 1:
                return "          Masalah Login\n\n" +
                        "Solusi umum masalah login:\n" +
                        "• Pastikan username/email benar\n" +
                        "• Cek caps lock pada password\n" +
                        "• Clear browser cache & cookies\n" +
                        "• Coba browser lain\n" +
                        "• Reset password jika perlu\n\n" +
                        "Masih bermasalah? Hubungi admin.\n" +
                        "Ketik 'menu' untuk kembali.";

            case 2:
                return "      Reset Password\n\n" +
                        "Cara reset password:\n" +
                        "1. Klik 'Lupa Password' di halaman login\n" +
                        "2. Masukkan email terdaftar\n" +
                        "3. Cek email untuk link reset\n" +
                        "4. Klik link dalam 15 menit\n" +
                        "5. Buat password baru\n\n" +
                        "Password harus min. 8 karakter.\n" +
                        "Ketik 'menu' untuk kembali.";

            case 3:
                return "            Hubungi Admin\n\n" +
                        "Kontak admin SeniMatic:\n" +
                        "       Email: admin@senimatic.com\n" +
                        "         WhatsApp: +62 812-3456-7890\n" +
                        "       Telepon: (021) 1234-5678\n" +
                        "         Jam kerja: 09:00 - 17:00 WIB\n\n" +
                        "Respon dalam 1x24 jam.\n" +
                        "Ketik 'menu' untuk kembali.";

            default:
                return "Pilihan tidak valid untuk bantuan teknis. Ketik 1-3 atau 'menu' untuk kembali.";
        }
    }

    /**
     * Handle text input
     */
    private String handleTextInput(String input, UserChatSession session) {
        String lowerInput = input.toLowerCase().trim();

        // Handle "menu" command to return to main menu
        if (lowerInput.equals("menu")) {
            session.setCurrentFlow("welcome");
            System.out.println("     RETURNED TO MAIN MENU");
            return getWelcomeResponse();
        }

        // Handle greetings
        if (lowerInput.contains("halo") || lowerInput.contains("hai")) {
            session.setCurrentFlow("welcome");
            return "Halo! " + getWelcomeResponse();
        }

        // Handle thanks
        if (lowerInput.contains("terima kasih")) {
            return "Sama-sama! Senang bisa membantu Anda. Ketik 'menu' untuk kembali ke menu utama.";
        }

        // Default response based on current flow
        String currentFlow = session.getCurrentFlow();
        if ("welcome".equals(currentFlow)) {
            return "Mohon masukkan nomor pilihan yang valid (1-4) atau ketik 'halo' untuk memulai.";
        } else {
            return "Mohon masukkan nomor pilihan yang valid atau ketik 'menu' untuk kembali ke menu utama.";
        }
    }

    /**
     * Get welcome response
     */
    private String getWelcomeResponse() {
        return "Selamat datang di SeniMatic Chat Assistant!         \n\n" +
                "Saya siap membantu Anda dengan:\n" +
                "1. Informasi Artwork\n" +
                "2. Cara Mengikuti Lelang\n" +
                "3. Info Galeri\n" +
                "4. Bantuan Teknis\n\n" +
                "Ketik nomor pilihan Anda untuk memulai:";
    }

    /**
     * Get fallback response when database is not available
     */
    private String getFallbackResponse(String input, UserChatSession session) {
        try {
            int choice = Integer.parseInt(input.trim());
            return handleMainMenuChoice(choice, session);
        } catch (NumberFormatException e) {
            return handleTextInput(input, session);
        }
    }

    /**
     * Process user input and generate response (DEPRECATED - use generateResponse instead)
     */
    @Deprecated
    public String processUserInput(int userId, String input) {
        return generateResponse(userId, input);
    }

    /**
     * Save chat message to database (public method for external use)
     */
    public boolean saveChatMessage(ChatLog chatLog) {
        try {
            boolean saved = chatbotDAO.saveChatMessage(chatLog);
            if (saved) {
                System.out.println("Chat message saved to database: " +
                        (chatLog.isUserMessage() ? "USER" : "BOT") + " - " +
                        chatLog.getMessageText().substring(0, Math.min(50, chatLog.getMessageText().length())) + "...");
            }
            return saved;
        } catch (Exception e) {
            System.err.println("Error saving chat message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update session in database
     */
    private void updateSessionInDatabase(UserChatSession session) {
        try {
            boolean updated = chatbotDAO.updateChatSession(session);
            if (updated) {
                System.out.println("Session updated in database: " + session.getSessionId());
            }
        } catch (Exception e) {
            System.err.println("Error updating session: " + e.getMessage());
        }
    }

    /**
     * Get active session for user
     */
    public UserChatSession getSession(int userId) {
        return activeSessions.get(userId);
    }

    /**
     * End chat session
     */
    public void endChatSession(int userId) {
        UserChatSession session = activeSessions.get(userId);
        if (session != null) {
            session.endSession();
            updateSessionInDatabase(session);
            activeSessions.remove(userId);
            System.out.println("Chat session ended for user: " + userId);
        }
    }

    /**
     * Get chat history for a session
     */
    public List<ChatLog> getChatHistory(int sessionId) {
        try {
            List<ChatLog> history = chatbotDAO.getChatHistory(sessionId);
            System.out.println("Retrieved " + history.size() + " chat messages from database for session: " + sessionId);
            return history;
        } catch (Exception e) {
            System.err.println("Error getting chat history: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get total message count for session
     */
    public int getMessageCount(int sessionId) {
        try {
            List<ChatLog> history = chatbotDAO.getChatHistory(sessionId);
            return history.size();
        } catch (Exception e) {
            System.err.println("Error getting message count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get latest messages from database
     */
    public List<ChatLog> getLatestMessages(int sessionId, int limit) {
        try {
            List<ChatLog> allMessages = chatbotDAO.getChatHistory(sessionId);
            if (allMessages.size() <= limit) {
                return allMessages;
            }
            return allMessages.subList(allMessages.size() - limit, allMessages.size());
        } catch (Exception e) {
            System.err.println("Error getting latest messages: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
