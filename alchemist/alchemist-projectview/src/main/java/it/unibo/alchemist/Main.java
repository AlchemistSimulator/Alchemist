package it.unibo.alchemist;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.controller.CenterLayoutController;
import it.unibo.alchemist.controller.TopLayoutController;
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

    private static final Logger L = LoggerFactory.getLogger(Main.class);

    private Stage primaryStage;
    private BorderPane root;

    /**
     * Returns the primary stage.
     * @return primary stage
     */
    public Stage getStage() {
        return this.primaryStage;
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
        initLayout("TopLayout");
        initLayout("LeftLayout");
        initLayout("CenterLayout");
    }

    private void initLayout(final String layoutName) {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/" + layoutName + ".fxml"));
        try {
            if (layoutName.equals("RootLayout")) {
                this.root = (BorderPane) loader.load();
                /*TODO: remove pixel values*/
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
                } else if (layoutName.equals("LeftLayout")) {
                    this.root.setLeft(pane);
                } else {
                    this.root.setCenter(pane);

                    final CenterLayoutController controller = loader.getController();
                    controller.setMain(this);
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
