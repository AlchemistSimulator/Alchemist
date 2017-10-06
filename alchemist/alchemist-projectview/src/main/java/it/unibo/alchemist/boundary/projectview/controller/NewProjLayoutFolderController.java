package it.unibo.alchemist.boundary.projectview.controller;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class models a JavaFX controller for NewProjLayoutFolder.fxml.
 */
public class NewProjLayoutFolderController implements Initializable {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

    @FXML
    private Button next;
    @FXML
    private Button selectFolder;
    @FXML
    private Label folderPath;

    private ProjectGUI main;
    private Stage stage;
    private String path;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.next.setText(RESOURCES.getString("next"));
        this.next.setDisable(true);
        this.selectFolder.setText(RESOURCES.getString("select_folder"));
    }

    /**
     * @param main main
     */
    public void setMain(final ProjectGUI main) {
        this.main = main;
    }

    /**
     * @param stage stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * @return Folder path of new project.
     */
    public String getFolderPath() {
        return this.path;
    }

    /**
     * @param path Folder path
     */
    public void setFolderPath(final String path) {
        this.path = path;
        this.folderPath.setText(this.path);
        this.next.setDisable(false);
    }

    /**
     *
     */
    @FXML
    public void clickSelect() {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(RESOURCES.getString("select_folder_proj"));
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());
        if (dir != null) {
            if (dir.listFiles().length == 0) {
                setSelectedFolder(dir);
            } else {
                final Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle(RESOURCES.getString("select_folder_full"));
                alert.setHeaderText(RESOURCES.getString("select_folder_full_header"));
                alert.setContentText(RESOURCES.getString("select_folder_full_content"));
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        try {
                            FileUtils.cleanDirectory(dir);
                            setSelectedFolder(dir);
                        } catch (final IOException e) {
                            final Alert alertCancel = new Alert(AlertType.ERROR);
                            alertCancel.setTitle(RESOURCES.getString("error_building_project"));
                            alertCancel.setHeaderText(RESOURCES.getString("error_building_project_header"));
                            alertCancel.setContentText(RESOURCES.getString("error_building_project_content"));
                            alertCancel.showAndWait();
                        }
                    } else {
                        final Alert alertCancel = new Alert(AlertType.WARNING);
                        alertCancel.setTitle(RESOURCES.getString("select_folder_full_cancel"));
                        alertCancel.setHeaderText(RESOURCES.getString("select_folder_full_cancel_header"));
                        alertCancel.setContentText(RESOURCES.getString("select_folder_full_cancel_content"));
                        alertCancel.showAndWait();
                    }
                });
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
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final double width = screenSize.getWidth() * 20.83 / 100;
            final double height = screenSize.getHeight() * 13.89 / 100;
            final Scene scene = new Scene(pane, width, height);
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
