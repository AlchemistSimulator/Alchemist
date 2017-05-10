package it.unibo.alchemist.boundary.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Main class to start an empty simulator visualization.
 */
public class MainApp extends Application {
    private static final String ROOT_LAYOUT = "RootLayout";

    private Stage primaryStage;
    private Pane rootLayout;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist Simulator base UI");

        this.initRootLayout();
        this.primaryStage.setScene(new Scene(this.rootLayout));
        this.primaryStage.show();
    }

    private void initRootLayout() throws IOException {
        this.rootLayout = FXResourceLoader.getLayout(AnchorPane.class, this, ROOT_LAYOUT);
    }

    /**
     * Method that launches the application.
     * 
     * @param args
     *            arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
