package it.unibo.alchemist.controller;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.Main;
import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutFolderController {

    private static final Logger L = LoggerFactory.getLogger(Main.class);

    @FXML
    private Button next;
    @FXML
    private Button selectFolder;
    @FXML
    private Label folderPath;

    private Main main;
    private Stage stage;
    private String path;

    /**
     * 
     */
    public void initialize() {
        this.next.setText(R.getString("next"));
        this.next.setDisable(true);
        this.selectFolder.setText(R.getString("select_folder"));
    }

    /**
     * 
     * @param main main
     */
    public void setMain(final Main main) {
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
        dirChooser.setTitle(R.getString("select_folder_proj"));
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        final File dir = dirChooser.showDialog(this.main.getStage());
        if (dir != null) {
            this.path = dir.getAbsolutePath();
            this.folderPath.setText(dir.getAbsolutePath());
            this.next.setDisable(false);
        }
    }

    /**
     * 
     */
    @FXML
    public void clickNext() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/NewProjLayoutSelect.fxml"));
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
}
