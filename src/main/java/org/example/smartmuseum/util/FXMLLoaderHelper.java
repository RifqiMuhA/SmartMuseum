package org.example.smartmuseum.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.smartmuseum.controller.SessionAwareController;
import org.example.smartmuseum.model.entity.User;
import org.example.smartmuseum.model.enums.UserRole;

import java.io.IOException;
import java.net.URL;

/**
 * Helper class untuk loading FXML dengan SessionContext injection
 * Memudahkan pembuatan window baru dengan session yang tepat
 */
public class FXMLLoaderHelper {

    /**
     * Load FXML dan inject SessionContext ke controller
     */
    public static LoadResult loadFXMLWithSession(String fxmlPath, SessionContext sessionContext) throws IOException {
        URL fxmlUrl = FXMLLoaderHelper.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Object controller = loader.getController();

        // Inject session context ke controller jika support
        if (controller instanceof SessionAwareController && sessionContext != null) {
            ((SessionAwareController) controller).setSessionContext(sessionContext);
            System.out.println("SessionContext injected to controller: " + controller.getClass().getSimpleName());
        }

        return new LoadResult(root, controller, loader);
    }

    /**
     * Create window baru dengan SessionContext
     */
    public static Stage createWindowWithSession(String fxmlPath, SessionContext sessionContext,
                                                String title, boolean maximized) throws IOException {
        LoadResult result = loadFXMLWithSession(fxmlPath, sessionContext);

        Stage stage = new Stage();
        Scene scene = new Scene(result.getRoot());

        // Add CSS
        String css = FXMLLoaderHelper.class.getResource("/org/example/smartmuseum/css/main-style.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        stage.setScene(scene);
        stage.setTitle(title);

        if (maximized) {
            stage.setMaximized(true);
        }

        // Set stage ke session context
        if (sessionContext != null) {
            sessionContext.setStage(stage);
        }

        // Setup cleanup saat window ditutup
        stage.setOnCloseRequest(event -> {
            if (result.getController() instanceof SessionAwareController) {
                ((SessionAwareController) result.getController()).cleanup();
            }
        });

        return stage;
    }

    /**
     * Create window dashboard untuk user tertentu
     * FIXED: Check authorization BEFORE creating window
     */
    public static Stage createDashboardWindow(User user) throws IOException {
        // FIXED: Authorization check BEFORE creating dashboard window
        if (user == null) {
            showAuthorizationError("No user provided for dashboard access.");
            throw new IllegalArgumentException("User cannot be null");
        }

        // Check if user has permission to access dashboard
        if (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.BOSS) {
            showAuthorizationError("Access denied. Dashboard is only available for Staff and Boss.\n" +
                    "Your role: " + user.getRole().getValue().toUpperCase());
            throw new SecurityException("Unauthorized access attempt by user: " + user.getUsername() +
                    " with role: " + user.getRole());
        }

        System.out.println("✅ Authorization check passed for user: " + user.getUsername() +
                " (Role: " + user.getRole() + ")");

        SessionContext context = SessionContext.createNewWindowContext("DASHBOARD");
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/fxml/dashboard.fxml",
                context,
                "Smart Museum - Dashboard (" + user.getUsername() + ")",
                true);
    }

    /**
     * Create welcome window untuk user tertentu
     * Semua role dapat mengakses welcome window
     */
    public static Stage createWelcomeWindow(User user) throws IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        System.out.println("✅ Creating welcome window for user: " + user.getUsername() +
                " (Role: " + user.getRole() + ")");

        SessionContext context = SessionContext.createNewWindowContext("WELCOME");
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/fxml/welcome.fxml",
                context,
                "Smart Museum - Welcome (" + user.getUsername() + ")",
                true);
    }

    /**
     * Create welcome window tanpa user (untuk akses publik)
     */
    public static Stage createWelcomeWindow() throws IOException {
        SessionContext context = SessionContext.createNewWindowContext("WELCOME");

        return createWindowWithSession("/org/example/smartmuseum/fxml/welcome.fxml",
                context,
                "Smart Museum - Welcome",
                true);
    }

    /**
     * Show authorization error dialog
     */
    private static void showAuthorizationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Access Denied");
        alert.setHeaderText("Unauthorized Access");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Create window untuk auction system
     */
    public static Stage createAuctionWindow(User user, String auctionId) throws IOException {
        // Authorization check for auction
        if (user == null) {
            showAuthorizationError("No user provided for auction access.");
            throw new IllegalArgumentException("User cannot be null");
        }

        // Only STAFF and BOSS can manage auctions
        if (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.BOSS) {
            showAuthorizationError("Access denied. Auction management is only available for Staff and Boss.");
            throw new SecurityException("Unauthorized auction access attempt");
        }

        SessionContext context = SessionContext.createNewWindowContext("AUCTION_" + auctionId);
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/fxml/auction-window.fxml",
                context,
                "Auction " + auctionId + " - " + user.getUsername(),
                false);
    }

    public static Stage createAuctionManagerWindow(User user) throws IOException {
        // Check authorization
        if (user.getRole() != UserRole.STAFF && user.getRole() != UserRole.BOSS) {
            throw new IOException("Access denied. Auction management is only available for Staff and Boss.");
        }

        SessionContext context = SessionContext.createNewWindowContext("AUCTION_MANAGER");
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/fxml/auction-management.fxml",
                context,
                "Auction Management - " + user.getUsername(),
                true);
    }

    /**
     * Create window untuk bidder
     */
    public static Stage createBidderWindow(User user, String auctionId) throws IOException {
        // Authorization check for bidding - all roles can bid
        if (user == null) {
            showAuthorizationError("No user provided for bidding access.");
            throw new IllegalArgumentException("User cannot be null");
        }

        // All roles can participate in bidding
        SessionContext context = SessionContext.createNewWindowContext("BIDDER_" + auctionId);
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/fxml/bidder-window.fxml",
                context,
                "Bidding - " + user.getUsername(),
                false);
    }

    public static Stage createLelangWindow(User user) throws IOException {
        SessionContext context = SessionContext.createNewWindowContext("LELANG_PUBLIC");
        context.getSessionManager().login(user);

        return createWindowWithSession("/org/example/smartmuseum/view/lelang.fxml",
                context,
                "Live Auction - " + user.getUsername(),
                true);
    }

    /**
     * Load child content dengan session context dari parent
     */
    public static LoadResult loadChildContent(String fxmlPath, SessionContext parentSessionContext) throws IOException {
        // Create child context yang share session tapi berbeda window type
        SessionContext childContext = SessionContext.fromExistingSession(
                parentSessionContext.getSessionManager(),
                "CHILD_" + System.currentTimeMillis()
        );

        return loadFXMLWithSession(fxmlPath, childContext);
    }

    /**
     * Result class untuk menyimpan hasil loading FXML
     */
    public static class LoadResult {
        private final Parent root;
        private final Object controller;
        private final FXMLLoader loader;

        public LoadResult(Parent root, Object controller, FXMLLoader loader) {
            this.root = root;
            this.controller = controller;
            this.loader = loader;
        }

        public Parent getRoot() { return root; }
        public Object getController() { return controller; }
        public FXMLLoader getLoader() { return loader; }

        @SuppressWarnings("unchecked")
        public <T> T getController(Class<T> controllerClass) {
            if (controllerClass.isInstance(controller)) {
                return (T) controller;
            }
            throw new ClassCastException("Controller is not instance of " + controllerClass.getName());
        }
    }
}