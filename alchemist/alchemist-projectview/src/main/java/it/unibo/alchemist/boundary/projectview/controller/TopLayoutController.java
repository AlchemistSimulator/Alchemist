/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;

import org.kaikikm.threadresloader.ResourceLoader;

import com.google.common.io.Files;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.utils.SVGImageUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 *
 */
public class TopLayoutController {

    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");
    private static final double IMG_WIDTH = 3;
    private static final double IMG_HEIGHT = 5;
    private static final String USER_HOME = System.getProperty("user.home");

    @FXML
    private Button btnNew;
    @FXML
    private Button btnOpen;
    @FXML
    private Button btnSave;

    private CenterLayoutController ctrlCenter;
    private ProjectGUI main;
    private LeftLayoutController ctrlLeft;
    private Watcher watcher;

    /**
     * 
     */
    public void initialize() {
        SVGImageUtils.installSvgLoader();
        this.btnNew.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/new.svg", IMG_WIDTH, IMG_HEIGHT)));
        this.btnNew.setText(RESOURCES.getString("new"));
        this.btnOpen.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/open.svg", IMG_WIDTH, IMG_HEIGHT)));
        this.btnOpen.setText(RESOURCES.getString("open"));
        this.btnSave.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/save.svg", IMG_WIDTH, IMG_HEIGHT)));
        this.btnSave.setText(RESOURCES.getString("save"));
        this.btnSave.setDisable(true);
    }

    /**
     * Sets the main class.
     * @param main main class
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @param controller LeftLayout controller
     */
    public void setCtrlLeft(final LeftLayoutController controller) {
        this.ctrlLeft = controller;
    }

    /**
     * 
     * @param controller CenterLayout controller
     */
    public void setCtrlCenter(final CenterLayoutController controller) {
        this.ctrlCenter = controller;
    }

    /**
     * Terminates the watcher.
     */
    public void terminateWatcher() {
        if (this.watcher != null) {
            this.watcher.terminate();
        }
    }

    /**
     * Show a view to create new project.
     * 
     * @throws IOException
     *             if the FXML can't get loaded
     */
    @FXML
    public void clickNew() throws IOException {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourceLoader.getResource(ProjectGUI.RESOURCE_LOCATION + "/view/NewProjLayoutFolder.fxml"));
        final AnchorPane pane = (AnchorPane) loader.load();
        final Stage stage = new Stage();
        stage.setTitle(RESOURCES.getString("new_proj"));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(this.main.getStage());
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double width = screenSize.getWidth() * 20.83 / 100;
        final double height = screenSize.getHeight() * 13.89 / 100;
        final Scene scene = new Scene(pane, width, height);
        stage.setScene(scene);
        final NewProjLayoutFolderController ctrl = loader.getController();
        ctrl.setMain(this.main);
        ctrl.setStage(stage);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                ctrl.setFolderPath(null);
            }
        });
        stage.showAndWait();
        if (ctrl.getFolderPath() != null) {
            setView(new File(ctrl.getFolderPath()));
        }
    }

    /**
     * Show a directory chooser to open an existing project.
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    @FXML
    public void clickOpen() throws FileNotFoundException, IOException {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final String folderPath = USER_HOME + File.separator + ".alchemist" + File.separator;
        if (!new File(folderPath).exists() && !new File(folderPath).mkdirs()) {
            throw new IllegalStateException("Error creating the folder to save the Alchemist settings.");
        }
        final String settingsPath = folderPath + File.separator + "alchemist-settings";
        final File settingsFile = new File(settingsPath);
        final DirectoryChooser dirChooser = new DirectoryChooser();
        if (settingsFile.exists()) {
            final String lastUsed = Optional.ofNullable(Files.asCharSource(settingsFile, StandardCharsets.UTF_8)
                    .readFirstLine())
                    .orElse(USER_HOME);
            final File lastUsedDir = new File(lastUsed);
            dirChooser.setInitialDirectory(lastUsedDir.exists() ? lastUsedDir : new File(USER_HOME));
        }
        dirChooser.setTitle(RESOURCES.getString("select_folder_proj"));
        final File dir = dirChooser.showDialog(this.main.getStage());
        if (dir != null) {
            Files.asCharSink(settingsFile, StandardCharsets.UTF_8).write(dir.getPath());
            setView(dir);
        }
    }

    /**
     * Save the project.
     */
    @FXML
    public void clickSave() {
        this.ctrlCenter.saveProject();
    }

    private void setView(final File dir) {
        final String pathFolder = dir.getAbsolutePath();
        this.ctrlLeft.setTreeView(dir);
        this.btnSave.setDisable(false);
        this.ctrlCenter.setField();
        if (this.watcher == null) {
            this.watcher = createWatcher();
        } else if (this.watcher.isWatcherAlive() && !this.watcher.getFolderPath().equals(pathFolder)) {
            terminateWatcher();
            this.watcher = createWatcher();
        }
        this.watcher.registerPath(pathFolder);
        new Thread(this.watcher, "WatcherProjectView").start();
    }

    private Watcher createWatcher() {
        return new Watcher(this.ctrlLeft, this.ctrlCenter);
    }

}
