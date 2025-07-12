package org.example.smartmuseum.model.service;

import org.example.smartmuseum.database.ChatbotDAO;
import org.example.smartmuseum.model.entity.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;   

public class ChatbotService {
    private Map<Integer, UserChatSession> activeSessions;
    private ChatbotDAO chatbotDAO;
    private Map<String, ConversationFlow> loadedFlows;

    public ChatbotService() {
        this.activeSessions = new HashMap<>();
        this.chatbotDAO = new ChatbotDAO();
        this.loadedFlows = new HashMap<>();
    }

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

    private void createFallbackFlow() {
        ConversationFlow fallbackFlow = new ConversationFlow("main", "Fallback conversation flow");
        fallbackFlow.setFlowId(1);
        fallbackFlow.setStartNodeId(1);
        loadedFlows.put("main", fallbackFlow);
        System.out.println("Fallback conversation flow created");
    }

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

    public ChatbotResponse generateResponseWithSound(int userId, String input) {
        UserChatSession session = activeSessions.get(userId);
        if (session == null) {
            session = initializeChatSession(userId);
        }

        session.updateActivity();

        // Generate response based on current flow and input
        ChatbotResponse response = generateContextualResponseWithSound(input, session);

        // Update session in database
        updateSessionInDatabase(session);

        return response;
    }

    public String generateResponse(int userId, String input) {
        ChatbotResponse response = generateResponseWithSound(userId, input);
        return response.getMessage();
    }

    private ChatbotResponse generateContextualResponseWithSound(String input, UserChatSession session) {
        try {
            String currentFlow = session.getCurrentFlow();
            System.out.println("       PROCESSING INPUT: '" + input + "' (Current Flow: " + currentFlow + ")");

            // Handle menu selections based on current flow
            try {
                int choice = Integer.parseInt(input.trim());
                return handleContextualMenuChoiceWithSound(choice, session, currentFlow);
            } catch (NumberFormatException e) {
                // Handle text input
                return handleTextInputWithSound(input, session);
            }

        } catch (Exception e) {
            System.err.println("Error generating contextual response: " + e.getMessage());
            return getFallbackResponseWithSound(input, session);
        }
    }

    private ChatbotResponse handleContextualMenuChoiceWithSound(int choice, UserChatSession session, String currentFlow) {
        System.out.println("       PROCESSING MENU CHOICE: " + choice + " in flow: " + currentFlow);

        // Handle based on current flow context
        switch (currentFlow) {
            case "welcome":
                return handleMainMenuChoiceWithSound(choice, session);

            case "artwork_info":
                return handleArtworkMenuChoiceWithSound(choice, session);

            case "auction_guide":
                return handleAuctionMenuChoiceWithSound(choice, session);

            case "technical_support":
                return handleTechnicalSupportChoiceWithSound(choice, session);

            default:
                System.out.println("    UNKNOWN FLOW: " + currentFlow + ", treating as main menu");
                return handleMainMenuChoiceWithSound(choice, session);
        }
    }

    private ChatbotResponse handleMainMenuChoiceWithSound(int choice, UserChatSession session) {
        switch (choice) {
            case 1:
                session.setCurrentFlow("artwork_info");
                System.out.println("     SWITCHED TO FLOW: artwork_info");
                return new ChatbotResponse(
                        "        Informasi Artwork\n\n" +
                                "Pilih kategori:\n" +
                                "1. Cari berdasarkan seniman\n" +
                                "2. Cari berdasarkan kategori\n" +
                                "3. Artwork terpopuler\n\n" +
                                "Ketik nomor pilihan:",
                        "mainmenu-1.m4a"
                );

            case 2:
                session.setCurrentFlow("auction_guide");
                System.out.println("     SWITCHED TO FLOW: auction_guide");
                return new ChatbotResponse(
                        "       Cara Mengikuti Lelang\n\n" +
                                "Langkah-langkah:\n" +
                                "1. Registrasi akun\n" +
                                "2. Verifikasi identitas\n" +
                                "3. Deposit jaminan\n" +
                                "4. Mulai bidding\n\n" +
                                "Pilih nomor untuk detail:",
                        "mainmenu-2.m4a"
                );

            case 3:
                session.setCurrentFlow("gallery_info");
                System.out.println("     SWITCHED TO FLOW: gallery_info");
                return new ChatbotResponse(
                        "        Info Galeri\n\n" +
                                "Jam Operasional: 09:00 - 17:00\n" +
                                "Lokasi: Jl. Seni Raya No. 123\n" +
                                "Telp: (021) 1234-5678\n" +
                                "Email: info@senimatic.com\n\n" +
                                "Ketik 'menu' untuk kembali ke menu utama.",
                        "mainmenu-3.m4a"
                );

            case 4:
                session.setCurrentFlow("technical_support");
                System.out.println("     SWITCHED TO FLOW: technical_support");
                return new ChatbotResponse(
                        "    Bantuan Teknis\n\n" +
                                "1. Masalah login\n" +
                                "2. Reset password\n" +
                                "3. Hubungi admin\n\n" +
                                "Ketik nomor untuk bantuan:",
                        "mainmenu-4.m4a"
                );

            default:
                System.out.println("    INVALID MAIN MENU CHOICE: " + choice);
                return new ChatbotResponse(
                        "Pilihan tidak valid",
                        "invalid-input.m4a"
                );
        }
    }

    private ChatbotResponse handleArtworkMenuChoiceWithSound(int choice, UserChatSession session) {
        System.out.println("     HANDLING ARTWORK SUBMENU CHOICE: " + choice);

        switch (choice) {
            case 1:
                return new ChatbotResponse(
                        "         Cari Berdasarkan Seniman\n\n" +
                                "Seniman terkenal di koleksi kami:\n" +
                                "• Affandi - Pelukis ekspresionisme\n" +
                                "• Raden Saleh - Pelukis romantisme\n" +
                                "• Basuki Abdullah - Pelukis realis\n" +
                                "• Sudjojono - Pelukis naturalis\n\n" +
                                "Ketik nama seniman atau 'menu' untuk kembali.",
                        "artwork-artist.m4a"
                );

            case 2:
                return new ChatbotResponse(
                        "          Cari Berdasarkan Kategori\n\n" +
                                "Kategori artwork:\n" +
                                "• Lukisan - Karya seni rupa 2D\n" +
                                "• Patung - Karya seni rupa 3D\n" +
                                "• Keramik - Seni kerajinan tanah liat\n" +
                                "• Batik - Seni tekstil tradisional\n\n" +
                                "Ketik kategori atau 'menu' untuk kembali.",
                        "artwork-category.m4a"
                );

            case 3:
                return new ChatbotResponse(
                        "    Artwork Terpopuler\n\n" +
                                "Top 3 artwork favorit pengunjung:\n" +
                                "1. 'Pemandangan Borobudur' - Affandi\n" +
                                "2. 'Penangkapan Diponegoro' - Raden Saleh\n" +
                                "3. 'Gadis Bali' - Basuki Abdullah\n\n" +
                                "Ketik 'menu' untuk kembali ke menu utama.",
                        "artwork-popular.m4a"
                );

            default:
                return new ChatbotResponse(
                        "Pilihan tidak valid.",
                        "invalid-input.m4a"
                );
        }
    }

    private ChatbotResponse handleAuctionMenuChoiceWithSound(int choice, UserChatSession session) {
        System.out.println("     HANDLING AUCTION SUBMENU CHOICE: " + choice);

        switch (choice) {
            case 1:
                return new ChatbotResponse(
                        "           Registrasi Akun\n\n" +
                                "Langkah registrasi:\n" +
                                "1. Kunjungi halaman registrasi\n" +
                                "2. Isi data pribadi lengkap\n" +
                                "5. Registrasi selesai\n\n" +
                                "Ketik 'menu' untuk kembali.",
                        "auction-register.m4a"
                );

            case 2:
                return new ChatbotResponse(
                        "     Verifikasi Identitas\n\n" +
                                "Dokumen yang diperlukan:\n" +
                                "• KTP/Paspor yang masih berlaku\n" +
                                "• NPWP (untuk lelang >50 juta)\n" +
                                "• Surat keterangan domisili\n" +
                                "• Foto selfie dengan KTP\n\n" +
                                "Serahkan dokumen melalui whatsapp admin\n" +
                                "Ketik 'menu' untuk kembali.",
                        "auction-verify.m4a"
                );

            case 3:
                return new ChatbotResponse(
                        "      Deposit Jaminan\n\n" +
                                "Ketentuan deposit:\n" +
                                "• Minimal 10% dari nilai lelang\n" +
                                "• Transfer ke rekening resmi\n" +
                                "• Deposit dikembalikan jika tidak menang\n" +
                                "• Berlaku untuk 1 sesi lelang\n\n" +
                                "Ketik 'menu' untuk kembali.",
                        "auction-deposit.m4a"
                );

            case 4:
                return new ChatbotResponse(
                        "     Mulai Bidding\n\n" +
                                "Cara bidding:\n" +
                                "1. Masuk ke halaman lelang terkini\n" +
                                "2. Pantau barang yang sedang dilelang\n" +
                                "3. Konfirmasi bid amount\n" +
                                "4. Klik tombol 'Bid Now'\n" +
                                "5. Tunggu hasil lelang sampai kamu menang!\n\n" +
                                "Ketik 'menu' untuk kembali.",
                        "auction-bidding.m4a"
                );

            default:
                return new ChatbotResponse(
                        "Pilihan tidak valid",
                        "invalid-input.m4a"
                );
        }
    }

    private ChatbotResponse handleTechnicalSupportChoiceWithSound(int choice, UserChatSession session) {
        System.out.println("     HANDLING TECHNICAL SUPPORT CHOICE: " + choice);

        switch (choice) {
            case 1:
                return new ChatbotResponse(
                        "          Masalah Login\n\n" +
                                "Solusi umum masalah login:\n" +
                                "• Pastikan username/email benar\n" +
                                "• Cek caps lock pada password\n" +
                                "• Clear browser cache & cookies\n" +
                                "• Coba browser lain\n" +
                                "• Reset password jika perlu\n\n" +
                                "Masih bermasalah? Hubungi admin.\n" +
                                "Ketik 'menu' untuk kembali.",
                        "support-login.m4a"
                );

            case 2:
                return new ChatbotResponse(
                        "      Reset Password\n\n" +
                                "Cara reset password:\n" +
                                "1. Klik 'Lupa Password' di halaman login\n" +
                                "2. Masukkan email terdaftar\n" +
                                "3. Cek email untuk link reset\n" +
                                "4. Klik link dalam 15 menit\n" +
                                "5. Buat password baru\n\n" +
                                "Password harus min. 8 karakter.\n" +
                                "Ketik 'menu' untuk kembali.",
                        "support-password.m4a"
                );

            case 3:
                return new ChatbotResponse(
                        "            Hubungi Admin\n\n" +
                                "Kontak admin SeniMatic:\n" +
                                "       Email: admin@senimatic.com\n" +
                                "         WhatsApp: +62 812-3456-7890\n" +
                                "       Telepon: (021) 1234-5678\n" +
                                "         Jam kerja: 09:00 - 17:00 WIB\n\n" +
                                "Respon dalam 1x24 jam.\n" +
                                "Ketik 'menu' untuk kembali.",
                        "support-contact.m4a"
                );

            default:
                return new ChatbotResponse(
                        "Pilihan tidak valid",
                        "invalid-input.m4a"
                );
        }
    }

    private ChatbotResponse handleTextInputWithSound(String input, UserChatSession session) {
        String lowerInput = input.toLowerCase().trim();

        // Handle "menu" command to return to main menu
        if (lowerInput.equals("menu")) {
            session.setCurrentFlow("welcome");
            System.out.println("     RETURNED TO MAIN MENU");
            return getWelcomeResponseWithSound();
        }

        // Handle greetings
        if (lowerInput.contains("halo") || lowerInput.contains("hai")) {
            session.setCurrentFlow("welcome");
            return new ChatbotResponse(
                    "Halo! " + getWelcomeResponse().getMessage(),
                    "greeting.m4a"
            );
        }

        // Handle thanks
        if (lowerInput.contains("terima kasih")) {
            return new ChatbotResponse(
                    "Sama-sama! Senang bisa membantu Anda. Ketik 'menu' untuk kembali ke menu utama.",
                    "terimakasih.m4a"
            );
        }

        // Default response based on current flow
        String currentFlow = session.getCurrentFlow();
        if ("welcome".equals(currentFlow)) {
            return new ChatbotResponse(
                    "Mohon masukkan nomor pilihan yang valid (1-4) atau ketik 'halo' untuk memulai.",
                    "invalid-input.m4a"
            );
        } else {
            return new ChatbotResponse(
                    "Mohon masukkan nomor pilihan yang valid atau ketik 'menu' untuk kembali ke menu utama.",
                    "invalid-input.m4a"
            );
        }
    }

    public ChatbotResponse getWelcomeResponseWithSound() {
        return new ChatbotResponse(
                "Selamat datang di SeniMatic Chat Assistant!         \n\n" +
                        "Saya siap membantu Anda dengan:\n" +
                        "1. Informasi Artwork\n" +
                        "2. Cara Mengikuti Lelang\n" +
                        "3. Info Galeri\n" +
                        "4. Bantuan Teknis\n\n" +
                        "Ketik nomor pilihan Anda untuk memulai:",
                "welcome.m4a"
        );
    }

    public ChatbotResponse getWelcomeResponse() {
        return getWelcomeResponseWithSound();
    }

    private ChatbotResponse getFallbackResponseWithSound(String input, UserChatSession session) {
        try {
            int choice = Integer.parseInt(input.trim());
            return handleMainMenuChoiceWithSound(choice, session);
        } catch (NumberFormatException e) {
            return handleTextInputWithSound(input, session);
        }
    }

    @Deprecated
    public String processUserInput(int userId, String input) {
        return generateResponse(userId, input);
    }

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

    public UserChatSession getSession(int userId) {
        return activeSessions.get(userId);
    }

    public void endChatSession(int userId) {
        UserChatSession session = activeSessions.get(userId);
        if (session != null) {
            session.endSession();
            updateSessionInDatabase(session);
            activeSessions.remove(userId);
            System.out.println("Chat session ended for user: " + userId);
        }
    }

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

    public int getMessageCount(int sessionId) {
        try {
            List<ChatLog> history = chatbotDAO.getChatHistory(sessionId);
            return history.size();
        } catch (Exception e) {
            System.err.println("Error getting message count: " + e.getMessage());
            return 0;
        }
    }

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