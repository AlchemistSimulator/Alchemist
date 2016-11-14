package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.ResourceAccess;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutFolderController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);

    @FXML
    private Button next;
    @FXML
    private Button selectFolder;
    @FXML
    private Label folderPath;

    private ProjectGUI main;
    private Stage stage;
    private String path;

    /**
     * 
     */
    public void initialize() {
        this.next.setText(ResourceAccess.getString("next"));
        this.next.setDisable(true);
        this.selectFolder.setText(ResourceAccess.getString("select_folder"));
    }

    /**
     * 
     * @param main main
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * 
     * @param stage stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * 
     * @param path Folder path
     */
    public void setPath(final String path) {
        this.path = path;
        this.folderPath.setText(this.path);
    }

    /**
     * 
     */
    @FXML
    public void clickSelect() {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(ResourceAccess.getString("select_folder_proj"));
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());
        if (dir != null) {
            if (dir.listFiles().length == 0) {
                setSelectedFolder(dir);
            } else {
                final Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle(ResourceAccess.getString("select_folder_full"));
                alert.setHeaderText(ResourceAccess.getString("select_folder_full_header"));
                alert.setContentText(ResourceAccess.getString("select_folder_full_content"));
                final Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        FileUtils.cleanDirectory(dir);
                        setSelectedFolder(dir);
                    } catch (IOException e) {
                        L.error("Error cleaning is unsuccessfull.", e);
                        System.exit(1);
                    }
                } else {
                    final Alert alertCancel = new Alert(AlertType.WARNING);
                    alertCancel.setTitle(ResourceAccess.getString("select_folder_full_cancel"));
                    alertCancel.setHeaderText(ResourceAccess.getString("select_folder_full_cancel_header"));
                    alertCancel.setContentText(ResourceAccess.getString("select_folder_full_cancel_content"));
                    alertCancel.showAndWait();
                }
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickNext() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ProjectGUI.class.getResource("view/NewProjLayoutSelect.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();
            final Scene scene = new Scene(pane);
            this.stage.setScene(scene);

            final NewProjLayoutSelectController ctrl = loader.getController();
            ctrl.setFolderPath(this.path);
            ctrl.setMain(this.main);
            ctrl.setStage(this.stage);
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

    private void setSelectedFolder(final File dir) {
        this.path = dir.getAbsolutePath();
        this.folderPath.setText(this.path);
        this.next.setDisable(false);
    }
}
