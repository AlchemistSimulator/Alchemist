package it.unibo.alchemist;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main class to start the application.
 */
public class Main extends Application {

    private static final Logger LOG = Logger.getLogger("EXCEPTION");
    private static final String IOEXP = "IOException";

    private Stage primaryStage;
    private BorderPane root;

    /**
     * Method that initializes the scene by loading all needed .fxml files and 
     * sets the primary stage.
     */
    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist");

        initRootLayout();
        showTopLayout();
        showLeftLayout();
        showCenterLayout();
    }

    private void initRootLayout() {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/RootLayout.fxml"));
            this.root = (BorderPane) loader.load();

            /*Modificare dimensione*/
            final Scene scene = new Scene(this.root, 1200, 950);
            scene.getStylesheets().add(getClass().getResource("/style.css")
                    .toExternalForm());

            this.primaryStage.setScene(scene);
            this.primaryStage.show();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, IOEXP, e);
        }
    }

    private void showTopLayout() {
        try {
            final FXMLLoader loaderTop = new FXMLLoader();
            loaderTop.setLocation(Main.class
                    .getResource("view/TopLayout.fxml"));
            final AnchorPane top = (AnchorPane) loaderTop.load();
            this.root.setTop(top);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, IOEXP, e);
        }
    }

    private void showLeftLayout() {
        try {
            final FXMLLoader loaderLeft = new FXMLLoader();
            loaderLeft.setLocation(Main.class
                    .getResource("view/LeftLayout.fxml"));
            final AnchorPane left = (AnchorPane) loaderLeft.load();
            this.root.setLeft(left);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, IOEXP, e);
        }
    }

    private void showCenterLayout() {
        try {
            final FXMLLoader loaderCenter = new FXMLLoader();
            loaderCenter.setLocation(Main.class
                    .getResource("view/CenterLayout.fxml"));
            final AnchorPane center = (AnchorPane) loaderCenter.load();
            this.root.setCenter(center);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, IOEXP, e);
        }
    }

    /**
     * Method that launches the application.
     * @param args arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }

}
