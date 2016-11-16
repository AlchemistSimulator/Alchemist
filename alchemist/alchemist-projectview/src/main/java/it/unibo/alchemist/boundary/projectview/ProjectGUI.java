package it.unibo.alchemist.boundary.projectview;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.projectview.controller.CenterLayoutController;
import it.unibo.alchemist.boundary.projectview.controller.LeftLayoutController;
import it.unibo.alchemist.boundary.projectview.controller.TopLayoutController;
import it.unibo.alchemist.boundary.projectview.controller.Watcher;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Main class to start the application.
 */
public class ProjectGUI extends Application {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);

    private BorderPane root;
    private CenterLayoutController controllerCenter;
    private LeftLayoutController controllerLeft;
    private Stage primaryStage;
    private Watcher watcher;

    /**
     * Returns the primary stage.
     * @return primary stage
     */
    public Stage getStage() {
        return this.primaryStage;
    }

    /**
     * Returns the watcher of file system.
     * @return a watcher.
     */
    public Watcher getWatcher() {
        return this.watcher;
    }

    /**
     * Method that initializes the scene by loading all needed .fxml files and 
     * sets the primary stage.
     */
    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist");

        initLayout("RootLayout");
        initLayout("LeftLayout");
        initLayout("CenterLayout");
        initLayout("TopLayout");

        this.watcher = new Watcher(this.controllerLeft);

        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(final WindowEvent wind) {
                watcher.terminate();
                primaryStage.close();
            }
        });

    }

    private void initLayout(final String layoutName) {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/" + layoutName + ".fxml"));
        try {
            if (layoutName.equals("RootLayout")) {
                this.root = (BorderPane) loader.load();
                //TODO: remove pixel values
                final Scene scene = new Scene(this.root, 1200, 950);
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                this.primaryStage.setScene(scene);
                this.primaryStage.show();
            } else {
                final AnchorPane pane = (AnchorPane) loader.load();
                if (layoutName.equals("TopLayout")) {
                    this.root.setTop(pane);

                    final TopLayoutController controller = loader.getController();
                    controller.setMain(this);
                    controller.setCtrlLeft(this.controllerLeft);
                    controller.setCtrlCenter(this.controllerCenter);
                } else if (layoutName.equals("LeftLayout")) {
                    this.root.setLeft(pane);

                    this.controllerLeft = loader.getController();
                } else {
                    this.root.setCenter(pane);

                    final CenterLayoutController controller = loader.getController();
                    controller.setMain(this);
                    controller.setCtrlLeft(this.controllerLeft);
                    this.controllerCenter = controller;
                }
            }
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
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
