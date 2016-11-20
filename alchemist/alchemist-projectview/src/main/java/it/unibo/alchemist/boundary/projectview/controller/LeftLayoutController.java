package it.unibo.alchemist.boundary.projectview.controller;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
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

    @FXML
    private Button run;
    @FXML
    private StackPane pane;
    @FXML
    private TreeView<String> treeView;

    private final Image folder = new Image(ProjectGUI.class.getResource("/icon/folder.png").toExternalForm());
    private final Image file = new Image(ProjectGUI.class.getResource("/icon/file.png").toExternalForm());
    private ProjectGUI main;
    private String pathFolder;
    private String selectedFile;
    private CenterLayoutController ctrlCenter;

    /**
     * 
     */
    public void initialize() {
        this.run.setText(RESOURCES.getString("run"));
        this.run.setDisable(true);
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
        final TreeItem<String> root = new TreeItem<>(dir.getName(), new ImageView(new Image(ProjectGUI.class.getResource("/icon/project.png").toExternalForm())));
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
                loadLayout(true);
            }
        });
        final MenuItem newFile = new MenuItem(RESOURCES.getString("new_file"));
        newFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                loadLayout(false);
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
        if (this.ctrlCenter.getProject() != null) {
            this.ctrlCenter.checkChanges();
        }
        final Project project = ProjectIOUtils.loadFrom(this.pathFolder);
        if (project != null) {
           final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        project.runAlchemistSimulation(false);
                    } catch (FileNotFoundException e) {
                        L.error("Error loading simulation file.", e);
                    }
                }
            }, "SingleRunGUI");
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
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ProjectGUI.class.getResource("view/NewFolderOrFileDialog.fxml"));
            final AnchorPane pane = (AnchorPane) loader.load();
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
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

}
