package it.unibo.alchemist.boundary.gui;

import java.io.IOException;

import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
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

    private Pane rootLayout;

    @Override
    public void start(final Stage primaryStage) {
        final Stage stage = primaryStage;
        stage.setTitle("Alchemist Simulator base UI");

        try {
            initRootLayout();
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize RootLayout", e);
        }

        SVGImageUtils.installSvgLoader();
        stage.getIcons().add(SVGImageUtils.getSvgImage("/icon/icon.svg"));
        stage.setScene(new Scene(this.rootLayout));
        stage.show();
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
