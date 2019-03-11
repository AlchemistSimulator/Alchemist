/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import org.jooq.lambda.Unchecked;
import org.kaikikm.threadresloader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.Project;
import it.unibo.alchemist.boundary.projectview.utils.URLManager;
import it.unibo.alchemist.boundary.projectview.utils.ProjectIOUtils;
import it.unibo.alchemist.boundary.projectview.utils.SVGImageUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class LeftLayoutController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");
    private static final double RUN_WIDTH = 1.875;
    private static final double RUN_HEIGHT = 3.33;
    private static final double TREE_ICON_WIDTH = 1.3021;
    private static final double TREE_ICON_HEIGHT = 2.3148;

    @FXML
    private Button run;
    @FXML
    private StackPane pane;
    @FXML
    private TreeView<String> treeView; // NOPMD: Casadio - JavaFX requires the variable is not local

    private Image folder;
    private Image file;
    private ProjectGUI main;
    private String pathFolder;
    private String selectedFile;
    private CenterLayoutController ctrlCenter;

    /**
     * 
     */
    public void initialize() {
        SVGImageUtils.installSvgLoader();
        this.run.setGraphic(new ImageView(SVGImageUtils.getSvgImage("icon/run.svg", RUN_WIDTH, RUN_HEIGHT)));
        this.run.setText(RESOURCES.getString("run"));
        this.run.setDisable(true);
        this.folder = SVGImageUtils.getSvgImage("icon/folder.svg", TREE_ICON_WIDTH, TREE_ICON_HEIGHT);
        this.file = SVGImageUtils.getSvgImage("icon/file.svg", TREE_ICON_WIDTH, TREE_ICON_HEIGHT);
    }

    /**
     * 
     * @param main Main class.
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @return path of project folder
     */
    public String getPathFolder() {
        return this.pathFolder;
    }

    /**
     * 
     * @return path of selected file
     */
    public String getSelectedFilePath() {
        return this.selectedFile;
    }

    /**
     * 
     * @param ctrlCenter A controller of CenterLayout
     */
    public void setCtrlCenter(final CenterLayoutController ctrlCenter) {
        this.ctrlCenter = ctrlCenter;
    }

    /**
     * 
     * @param dir Selected directory
     */
    public void setTreeView(final File dir) {
        this.pathFolder = dir.getAbsolutePath();
        final TreeItem<String> root = new TreeItem<>(dir.getName(), new ImageView(SVGImageUtils.getSvgImage("icon/project.svg", TREE_ICON_WIDTH, TREE_ICON_HEIGHT)));
        root.setExpanded(true);
        this.treeView = new TreeView<>(root);
        displayProjectContent(dir, root);
        this.pane.getChildren().add(this.treeView);
        this.treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            @Override
            public void changed(final ObservableValue<? extends TreeItem<String>> observable, 
                    final TreeItem<String> oldVal,
                    final TreeItem<String> newVal) {
                final TreeItem<String> selectedItem = (TreeItem<String>) newVal;
                TreeItem<String> parent = selectedItem.getParent();
                String path = File.separator + selectedItem.getValue();
                while (parent != null)  {
                    if (parent.getParent() != null) {
                        path = File.separator + parent.getValue() + path;
                    }
                    parent = parent.getParent();
                }
                selectedFile = pathFolder + path;
            }

        });
        this.treeView.setOnMouseClicked(mouseEv -> {
            final File target = new File(selectedFile);
            if (mouseEv.getClickCount() == 2 && target.exists() && target.isFile()) {
                final Desktop desk = Desktop.getDesktop();
                if (desk.isSupported(Action.OPEN)) {
                    new Thread(() -> {
                        try {
                            desk.open(target);
                        } catch (IOException e) {
                            L.error("Error opening file.", e);
                        }
                    }).start();
                }
            }
        });
        final ContextMenu menu = new ContextMenu();
        final MenuItem newFolder = new MenuItem(RESOURCES.getString("new_folder"));
        newFolder.setOnAction(e -> loadLayout(true));
        final MenuItem newFile = new MenuItem(RESOURCES.getString("new_file"));
        newFile.setOnAction(e -> loadLayout(false));
        menu.getItems().addAll(newFolder, newFile);
        this.treeView.setContextMenu(menu);
    }

    /**
     * 
     */
    @FXML
    public void clickRun() {
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final Project project = ProjectIOUtils.loadFrom(this.pathFolder);
        if (project != null) {
           final Thread thread = new Thread(Unchecked.runnable(() -> {
               ResourceLoader.setDefault();
               project.runAlchemistSimulation(false);
           }), "SingleRunGUI");
           URLManager.getInstance().setupThreadClassLoader(thread);
           thread.setDaemon(true);
           thread.start();
        } else {
            final Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(RESOURCES.getString("error_running"));
            alert.setHeaderText(RESOURCES.getString("error_running_header"));
            alert.setContentText(RESOURCES.getString("error_running_content"));
            alert.showAndWait();
        }
    }

    /**
     * 
     */
    public void setEnableRun() {
        this.run.setDisable(false);
    }

    private void displayProjectContent(final File dir, final TreeItem<String> root) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file: files) {
                if (!file.getName().equals(".alchemist_project_descriptor.json")) {
                    final TreeItem<String> singleFile; 
                    if (file.isDirectory()) {
                        singleFile = new TreeItem<>(file.getName(), new ImageView(this.folder));
                        displayProjectContent(file, singleFile);
                        root.getChildren().add(singleFile);
                    } else {
                        singleFile = new TreeItem<>(file.getName(), new ImageView(this.file));
                        root.getChildren().add(singleFile);
                    }
                    root.setExpanded(true);
                }
            }
        }
    }

    private void loadLayout(final boolean isFolder) {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourceLoader.getResource(ProjectGUI.RESOURCE_LOCATION + "/view/NewFolderOrFileDialog.fxml"));
        AnchorPane pane;
        try {
            pane = (AnchorPane) loader.load();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final Stage stage = new Stage();
        if (isFolder) {
            stage.setTitle(RESOURCES.getString("folder_name_title"));
        } else {
            stage.setTitle(RESOURCES.getString("file_name_title"));
        }
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(this.main.getStage());
        stage.setResizable(false);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final double width = screenSize.getWidth() * 20.83 / 100;
        final double height = screenSize.getHeight() * 13.89 / 100;
        final Scene scene = new Scene(pane, width, height);
        stage.setScene(scene);
        final NewFolderOrFileDialogController controller = loader.getController();
        controller.initialize(isFolder);
        controller.setSelectedItem(this.selectedFile);
        controller.setStage(stage);
        stage.showAndWait();
    }

}
