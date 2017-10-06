package it.unibo.alchemist.boundary.projectview;

import it.unibo.alchemist.boundary.gui.utility.SVGImageUtils;
import it.unibo.alchemist.boundary.projectview.controller.CenterLayoutController;
import it.unibo.alchemist.boundary.projectview.controller.LeftLayoutController;
import it.unibo.alchemist.boundary.projectview.controller.TopLayoutController;
import it.unibo.alchemist.boundary.util.FXUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.IOException;

/**
 * Main class to start the application.
 */
public class ProjectGUI extends Application {

    private BorderPane root;
    private CenterLayoutController controllerCenter;
    private LeftLayoutController controllerLeft;
    private Stage primaryStage;
    private TopLayoutController controllerTop;

    /**
     * Method that launches the application.
     *
     * @param args arguments
     */
    public static void main(final String... args) {
        launch(args);
    }

    /**
     * Returns the primary stage.
     *
     * @return primary stage
     */
    public Stage getStage() {
        return this.primaryStage;
    }

    /**
     * Method that initializes the scene by loading all needed .fxml files and
     * sets the primary stage.
     *
     * @throws IOException in case of bugs
     */
    @Override
    public void start(final Stage primaryStage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(FXUtil::errorAlert);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist");
        this.primaryStage.getIcons().add(SVGImageUtils.getSvgImage(SVGImageUtils.DEFAULT_ALCHEMIST_ICON_PATH));
        initLayout("RootLayout");
        initLayout("LeftLayout");
        initLayout("CenterLayout");
        initLayout("TopLayout");
        Platform.setImplicitExit(false);
        this.primaryStage.setOnCloseRequest(wind -> {
            wind.consume();
            controllerCenter.checkChanges();
            if (controllerCenter.isCorrectnessSpinTime() && controllerCenter.isCorrectnessSpinOut()) {
                controllerTop.terminateWatcher();
                Platform.exit();
            }
        });
    }

    private void initLayout(final String layoutName) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/" + layoutName + ".fxml"));
        if (layoutName.equals("RootLayout")) {
            this.root = loader.load();
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final double width = screenSize.getWidth() * 62.5 / 100;
            final double height = screenSize.getHeight() * 87.96 / 100;
            final Scene scene = new Scene(this.root, width, height);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            this.primaryStage.setScene(scene);
            this.primaryStage.show();
        } else {
            final AnchorPane pane = loader.load();
            if (layoutName.equals("TopLayout")) {
                this.root.setTop(pane);
                this.controllerTop = loader.getController();
                this.controllerTop.setMain(this);
                this.controllerTop.setCtrlLeft(this.controllerLeft);
                this.controllerTop.setCtrlCenter(this.controllerCenter);
            } else if (layoutName.equals("LeftLayout")) {
                this.root.setLeft(pane);
                this.controllerLeft = loader.getController();
                this.controllerLeft.setMain(this);
            } else {
                this.root.setCenter(pane);
                this.controllerCenter = loader.getController();
                this.controllerCenter.setMain(this);
                this.controllerCenter.setCtrlLeft(this.controllerLeft);
            }
            this.controllerLeft.setCtrlCenter(this.controllerCenter);
        }
    }

}
