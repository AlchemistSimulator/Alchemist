package it.unibo.alchemist.controller;

import java.io.IOException;

import it.unibo.alchemist.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class CenterLayoutController {

    private Main main;

    /**
     * Sets the main class.
     * @param main main class.
     */
    public void setMain(final Main main) {
        this.main = main;
    }

    /**
     * 
     */
    @FXML
    protected void clickNewYaml() {
        newFile(".yaml");
    }

    /**
     * 
     */
    @FXML
    protected void clickNewEffect() {
        newFile(".aes");
    }

    private void newFile(final String extension) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/FileNameDialog.fxml"));
            final AnchorPane pane = (AnchorPane) loader.load();

            final Stage stage = new Stage();
            stage.setTitle("File name");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(this.main.getStage());
            stage.setResizable(false);
            final Scene scene = new Scene(pane);
            stage.setScene(scene);

            final FileNameDialogController controller = loader.getController();
            controller.setDialogStage(stage);
            controller.setExtension(extension);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
