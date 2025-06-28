package org.example.smartmuseum.database;

import org.example.smartmuseum.model.entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for chatbot operations - CORRECTED for actual database structure
 */
public class ChatbotDAO {

    private DatabaseConnection dbConnection;

    public ChatbotDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        verifyDatabaseTables();
    }

    /**
     * Verify that required tables exist with correct names
     */
    private void verifyDatabaseTables() {
        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.out.println("    Database not available");
                return;
            }

            // Check if tables exist with correct names
            DatabaseMetaData metaData = conn.getMetaData();
            String[] tableNames = {"Conversation_Flows", "Flow_Nodes", "Node_Options", "User_Chat_Sessions", "Chat_Logs"};

            for (String tableName : tableNames) {
                try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
                    if (rs.next()) {
                        System.out.println("     Table exists: " + tableName);
                    } else {
                        System.out.println("    Table missing: " + tableName);
                    }
                }
            }

            // Check if conversation flow data exists
            checkConversationFlowData(conn);

        } catch (SQLException e) {
            System.err.println("    Error verifying database tables: " + e.getMessage());
        }
    }

    /**
     * Check if conversation flow data exists
     */
    private void checkConversationFlowData(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Conversation_Flows WHERE flow_name = 'main'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("     Main conversation flow exists in database");

                // Check node count
                String nodeCountSql = "SELECT COUNT(*) FROM Flow_Nodes WHERE flow_id = 1";
                try (PreparedStatement nodeStmt = conn.prepareStatement(nodeCountSql);
                     ResultSet nodeRs = nodeStmt.executeQuery()) {
                    if (nodeRs.next()) {
                        int nodeCount = nodeRs.getInt(1);
                        System.out.println("        Found " + nodeCount + " conversation nodes in database");

                        if (nodeCount == 0) {
                            System.out.println("     WARNING: No conversation nodes found. Please run populate_correct_chatbot_data.sql");
                        }
                    }
                }
            } else {
                System.out.println("     WARNING: Main conversation flow not found. Please run populate_correct_chatbot_data.sql");
            }
        }
    }

    /**
     * Save chat message to database - CORRECTED for Chat_Logs structure
     */
    public boolean saveChatMessage(ChatLog chatLog) {
        String sql = "INSERT INTO Chat_Logs (session_id, user_input, bot_response, node_id, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database connection not available for saving message");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, chatLog.getSessionId());

                // Handle user vs bot message for Chat_Logs structure
                if (chatLog.isUserMessage()) {
                    stmt.setString(2, chatLog.getMessageText()); // user_input
                    stmt.setNull(3, Types.LONGVARCHAR); // bot_response = NULL
                } else {
                    stmt.setNull(2, Types.VARCHAR); // user_input = NULL
                    stmt.setString(3, chatLog.getMessageText()); // bot_response
                }

                // Handle nullable node_id
                if (chatLog.getNodeId() != null) {
                    stmt.setInt(4, chatLog.getNodeId());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }

                stmt.setTimestamp(5, Timestamp.valueOf(chatLog.getTimestamp()));

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            chatLog.setLogId(generatedKeys.getInt(1));
                        }
                    }

                    String messageType = chatLog.isUserMessage() ? "USER" : "BOT";
                    String messagePreview = chatLog.getMessageText().length() > 50 ?
                            chatLog.getMessageText().substring(0, 50) + "..." :
                            chatLog.getMessageText();

                    System.out.println("         SAVED TO DATABASE: [" + messageType + "] " + messagePreview +
                            " (Session: " + chatLog.getSessionId() + ", ID: " + chatLog.getLogId() + ")");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("    ERROR saving chat message to database: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get conversation flow by name - CORRECTED table name
     */
    public ConversationFlow getConversationFlow(String flowName) {
        String sql = "SELECT * FROM Conversation_Flows WHERE flow_name = ? AND is_active = 1";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database not available for getting conversation flow");
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, flowName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ConversationFlow flow = new ConversationFlow();
                        flow.setFlowId(rs.getInt("flow_id"));
                        flow.setFlowName(rs.getString("flow_name"));
                        flow.setDescription(rs.getString("description"));
                        flow.setActive(rs.getBoolean("is_active"));
                        flow.setStartNodeId(1); // Default to root node

                        System.out.println("        LOADED FLOW FROM DATABASE: " + flowName + " (Start Node: " + flow.getStartNodeId() + ")");
                        return flow;
                    } else {
                        System.out.println("    FLOW NOT FOUND IN DATABASE: " + flowName);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("    Error getting conversation flow: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get flow node by ID - CORRECTED table and column names
     */
    public FlowNode getFlowNode(int nodeId) {
        String sql = "SELECT * FROM Flow_Nodes WHERE node_id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database not available for getting flow node");
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, nodeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        FlowNode node = new FlowNode();
                        node.setNodeId(rs.getInt("node_id"));
                        node.setFlowId(rs.getInt("flow_id"));
                        node.setNodeType(rs.getString("node_type"));
                        node.setNodeContent(rs.getString("node_text")); // CORRECTED: node_text -> node_content
                        node.setNodeKey("node_" + nodeId); // Generate key since not in DB

                        // Handle nullable parent_node_id
                        Object parentNodeId = rs.getObject("parent_node_id");
                        if (parentNodeId != null) {
                            node.setParentNodeId((Integer) parentNodeId);
                        }

                        node.setActive(true); // Default active

                        System.out.println("      LOADED NODE FROM DATABASE: " + nodeId + " (Type: " + node.getNodeType() + ")");
                        return node;
                    } else {
                        System.out.println("    NODE NOT FOUND IN DATABASE: " + nodeId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("    Error getting flow node: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get node options for a specific node - CORRECTED table and column names
     */
    public List<NodeOption> getNodeOptions(int nodeId) {
        List<NodeOption> options = new ArrayList<>();
        String sql = "SELECT * FROM Node_Options WHERE node_id = ? AND is_active = 1 ORDER BY option_number";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database not available for getting node options");
                return options;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, nodeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        NodeOption option = new NodeOption();
                        option.setOptionId(rs.getInt("option_id"));
                        option.setNodeId(rs.getInt("node_id"));
                        option.setOptionText(rs.getString("option_text"));
                        option.setOptionValue(String.valueOf(rs.getInt("option_number"))); // CORRECTED: option_number -> option_value
                        option.setNextNodeId(rs.getInt("target_node_id")); // CORRECTED: target_node_id -> next_node_id
                        option.setOrderIndex(rs.getInt("option_number"));
                        option.setActive(rs.getBoolean("is_active"));
                        options.add(option);
                    }
                }

                System.out.println("     LOADED " + options.size() + " OPTIONS FROM DATABASE for Node: " + nodeId);

                if (options.isEmpty()) {
                    System.out.println("     WARNING: No options found for node " + nodeId);
                }
            }
        } catch (SQLException e) {
            System.err.println("    Error getting node options: " + e.getMessage());
            e.printStackTrace();
        }

        return options;
    }

    /**
     * Create new chat session - CORRECTED table name
     */
    public UserChatSession createChatSession(int userId) {
        String sql = "INSERT INTO User_Chat_Sessions (user_id, current_node_id, session_data, last_activity, is_active) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database connection not available for creating session");
                return null;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                LocalDateTime now = LocalDateTime.now();
                stmt.setInt(1, userId);
                stmt.setInt(2, 1); // Start at welcome node
                stmt.setString(3, "{}"); // Empty JSON session data
                stmt.setTimestamp(4, Timestamp.valueOf(now));
                stmt.setBoolean(5, true);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            UserChatSession session = new UserChatSession(userId);
                            session.setSessionId(generatedKeys.getInt(1));
                            session.setLastActivity(now);
                            session.setCurrentNodeId(1);
                            session.setActive(true);

                            System.out.println("     NEW SESSION CREATED IN DATABASE: " + session.getSessionId() + " for User: " + userId);
                            return session;
                        }
                    }
                } else {
                    System.out.println("    Failed to create session in database");
                }
            }
        } catch (SQLException e) {
            System.err.println("    Error creating chat session: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update user chat session - CORRECTED table name
     */
    public boolean updateChatSession(UserChatSession session) {
        String sql = "UPDATE User_Chat_Sessions SET current_node_id = ?, session_data = ?, last_activity = ? WHERE session_id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database not available for updating session");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, session.getCurrentNodeId());
                stmt.setString(2, session.getSessionData() != null ? session.getSessionData() : "{}");
                stmt.setTimestamp(3, Timestamp.valueOf(session.getLastActivity()));
                stmt.setInt(4, session.getSessionId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("     SESSION UPDATED IN DATABASE: " + session.getSessionId() +
                            " (Node: " + session.getCurrentNodeId() + ")");
                    return true;
                } else {
                    System.out.println("     WARNING: No session updated for ID: " + session.getSessionId());
                }
            }
        } catch (SQLException e) {
            System.err.println("    Error updating chat session: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get chat history for a session - CORRECTED for Chat_Logs structure
     */
    public List<ChatLog> getChatHistory(int sessionId) {
        List<ChatLog> history = new ArrayList<>();
        String sql = "SELECT * FROM Chat_Logs WHERE session_id = ? ORDER BY timestamp ASC";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database connection not available for getting chat history");
                return history;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, sessionId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Handle user input
                        String userInput = rs.getString("user_input");
                        if (userInput != null && !userInput.trim().isEmpty()) {
                            ChatLog userLog = new ChatLog();
                            userLog.setLogId(rs.getInt("log_id"));
                            userLog.setSessionId(rs.getInt("session_id"));
                            userLog.setMessageText(userInput);
                            userLog.setUserMessage(true);
                            userLog.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                            userLog.setMessageType("USER");

                            Object nodeIdObj = rs.getObject("node_id");
                            if (nodeIdObj != null) {
                                userLog.setNodeId((Integer) nodeIdObj);
                            }

                            history.add(userLog);
                        }

                        // Handle bot response
                        String botResponse = rs.getString("bot_response");
                        if (botResponse != null && !botResponse.trim().isEmpty()) {
                            ChatLog botLog = new ChatLog();
                            botLog.setLogId(rs.getInt("log_id"));
                            botLog.setSessionId(rs.getInt("session_id"));
                            botLog.setMessageText(botResponse);
                            botLog.setUserMessage(false);
                            botLog.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                            botLog.setMessageType("BOT");

                            Object nodeIdObj = rs.getObject("node_id");
                            if (nodeIdObj != null) {
                                botLog.setNodeId((Integer) nodeIdObj);
                            }

                            history.add(botLog);
                        }
                    }
                }

                System.out.println("            LOADED CHAT HISTORY FROM DATABASE: " + history.size() + " messages for Session: " + sessionId);
            }
        } catch (SQLException e) {
            System.err.println("    Error getting chat history: " + e.getMessage());
            e.printStackTrace();
        }

        return history;
    }

    /**
     * Clear chat history for a session - CORRECTED table name
     */
    public boolean clearChatHistory(int sessionId) {
        String sql = "DELETE FROM Chat_Logs WHERE session_id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            if (conn == null) {
                System.err.println("    Database not available for clearing chat history");
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, sessionId);

                int deletedRows = stmt.executeUpdate();
                System.out.println("         CLEARED CHAT HISTORY FROM DATABASE: " + deletedRows + " messages deleted for Session: " + sessionId);
                return deletedRows >= 0;
            }
        } catch (SQLException e) {
            System.err.println("    Error clearing chat history: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
