/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import org.kaikikm.threadresloader.ResourceLoader;

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

/**
 * Main class to start the application.
 */
public class ProjectGUI extends Application {

    /**
     *
     */
    public static final String RESOURCE_LOCATION = ProjectGUI.class.getPackage().getName().replace('.', '/');
    private BorderPane root;
    private CenterLayoutController controllerCenter;
    private LeftLayoutController controllerLeft;
    private Stage primaryStage;
    private TopLayoutController controllerTop;

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
     *
     * @throws IOException
     *             in case of bugs
     */
    @Override
    public void start(final Stage primaryStage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            FXUtil.errorAlert(ex);
        });
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Alchemist");
        initLayout("RootLayout");
        initLayout("LeftLayout");
        initLayout("CenterLayout");
        initLayout("TopLayout");
        Platform.setImplicitExit(false);
        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent wind) {
                wind.consume();
                controllerCenter.checkChanges();
                if (controllerCenter.isCorrectnessSpinTime() && controllerCenter.isCorrectnessSpinOut()) {
                    controllerTop.terminateWatcher();
                    Platform.exit();
                }
            }
        });
    }

    private void initLayout(final String layoutName) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourceLoader.getResource(ProjectGUI.RESOURCE_LOCATION + "/view/" + layoutName + ".fxml"));
        if (layoutName.equals("RootLayout")) {
            this.root = (BorderPane) loader.load();
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final double width = screenSize.getWidth() * 62.5 / 100;
            final double height = screenSize.getHeight() * 87.96 / 100;
            final Scene scene = new Scene(this.root, width, height);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            this.primaryStage.setScene(scene);
            this.primaryStage.show();
        } else {
            final AnchorPane pane = (AnchorPane) loader.load();
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

    /**
     * Method that launches the application.
     * @param args arguments
     */
    public static void main(final String... args) {
        launch(args);
    }

}
