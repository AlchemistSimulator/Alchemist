package it.unibo.alchemist.controller;

import java.io.IOException;

import org.controlsfx.control.ToggleSwitch;

import it.unibo.alchemist.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller of CenterLayout view.
 */
public class CenterLayoutController {

    @FXML
    private Button batch;
    @FXML
    private Button btnSet;
    @FXML
    private GridPane gridOut;
    @FXML
    private GridPane gridVar; 
    @FXML
    private Label pathOut;
    @FXML
    private ListView<String> listYaml;

    private Main main;

    private final ToggleSwitch tsOut = new ToggleSwitch();
    private final ToggleSwitch tsVar = new ToggleSwitch();

    /**
     * Sets the main class and adds toggle switch to view.
     * @param main main class.
     */
    public void setMain(final Main main) {
        this.main = main;

        this.gridOut.add(this.tsOut, 0, 0);
        controlSwitch(this.tsOut);
        this.gridVar.add(this.tsVar, 0, 0);
        controlSwitch(this.tsVar);
    }

    private void controlSwitch(final ToggleSwitch ts) {
        if (ts.isSelected()) {
            setComponentVisible(ts, true);
        } else {
            setComponentVisible(ts, false);
        }

        ts.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> ov, 
                    final Boolean t1, final Boolean t2) {
                if (ts.isSelected()) {
                    setComponentVisible(ts, true);
                } else {
                    setComponentVisible(ts, false);
                }
            }
        });
    }

    private void setComponentVisible(final ToggleSwitch ts, final boolean vis) {
        if (ts.equals(this.tsOut)) {
            this.btnSet.setVisible(vis);
            this.pathOut.setVisible(vis);
        } else {
            this.batch.setVisible(vis);
            this.listYaml.setVisible(vis);
        }
    }

    /**
     * Show dialog to create new YAML file.
     */
    @FXML
    protected void clickNewYaml() {
        newFile(".yaml");
    }

    /**
     * Show dialog to create new effect file.
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
