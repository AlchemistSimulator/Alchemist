package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import it.unibo.alchemist.boundary.projectview.model.Project;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * 
 *
 */
public class LeftLayoutController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

    @FXML
    private Button run;
    @FXML
    private StackPane pane;
    @FXML
    private TreeView<String> treeView;

    private String pathFolder;
    private String selectedFile;

    /**
     * 
     */
    public void initialize() {
        this.run.setText(RESOURCES.getString("run"));
        this.run.setDisable(true);
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
     * @param dir Selected directory
     */
    public void setTreeView(final File dir) {
        this.pathFolder = dir.getAbsolutePath();
        final TreeItem<String> root = new TreeItem<>(dir.getName());
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
        this.treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent mouseEv) {
                if (mouseEv.getClickCount() == 2 && new File(selectedFile).isFile()) {
                        final Desktop desk = Desktop.getDesktop();
                        try {
                            desk.open(new File(selectedFile));
                        } catch (IOException e) {
                            L.error("Error opening file.", e);
                            System.exit(1);
                        }
                }
            }
        });
        final ContextMenu menu = new ContextMenu();
        final MenuItem newFolder = new MenuItem(RESOURCES.getString("new_folder"));
        newFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                //TODO: create new folder
            }
        });
        final MenuItem newFile = new MenuItem(RESOURCES.getString("new_file"));
        newFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                //TODO: create new file
            }
        });
        menu.getItems().addAll(newFolder, newFile);
        this.treeView.setContextMenu(menu);
    }

    /**
     * 
     */
    @FXML
    public void clickRun() {
        final Project project = ProjectIOUtils.loadFrom(this.pathFolder);
        try {
            project.runAlchemistSimulation(false);
        } catch (FileNotFoundException e) {
            L.error("Error loading simulation file.", e);
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
                    final TreeItem<String> singleFile = new TreeItem<>(file.getName());
                    if (file.isDirectory()) {
                        displayProjectContent(file, singleFile);
                        root.getChildren().add(singleFile);
                    } else {
                        root.getChildren().add(singleFile);
                    }
                    root.setExpanded(true);
                }
            }
        }
    }

}
