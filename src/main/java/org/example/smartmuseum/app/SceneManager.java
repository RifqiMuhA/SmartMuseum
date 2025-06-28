package org.example.smartmuseum.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages scene navigation and transitions
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private Scene currentScene;
    private Map<String, Scene> sceneCache;

    private SceneManager() {
        this.sceneCache = new HashMap<>();
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    public void switchToScene(String fxmlFile, String title) throws IOException {
        Scene scene = sceneCache.get(fxmlFile);

        if (scene == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            scene = new Scene(loader.load());
            sceneCache.put(fxmlFile, scene);
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        this.currentScene = scene;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}
