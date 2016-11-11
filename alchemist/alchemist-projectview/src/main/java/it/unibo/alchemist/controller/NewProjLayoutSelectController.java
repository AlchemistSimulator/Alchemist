package it.unibo.alchemist.controller;

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
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewProjLayoutSelectController {

    private static final Logger L = LoggerFactory.getLogger(Main.class);

    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private Label select;

    private Main main;
    private String folderPath;
    private Stage stage;

    /**
     * 
     */
    public void initialize() {
        this.backBtn.setText(R.getString("back"));
        this.finishBtn.setText(R.getString("finish"));
        this.finishBtn.setDisable(true);
        this.select.setText(R.getString("select"));
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
     * @param stage Stage
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * 
     * @param path Folder path
     */
    public void setFolderPath(final String path) {
        this.folderPath = path;
    }

    /**
     * 
     */
    @FXML
    public void clickBack() {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/NewProjLayoutFolder.fxml"));
        try {
            final AnchorPane pane = (AnchorPane) loader.load();
            final Scene scene = new Scene(pane);
            this.stage.setScene(scene);

            final NewProjLayoutFolderController ctrl = loader.getController();
            ctrl.setMain(this.main);
            ctrl.setStage(this.stage);
            ctrl.setPath(this.folderPath);
        } catch (IOException e) {
            L.error("Error loading the graphical interface. This is most likely a bug.", e);
            System.exit(1);
        }
    }

}
