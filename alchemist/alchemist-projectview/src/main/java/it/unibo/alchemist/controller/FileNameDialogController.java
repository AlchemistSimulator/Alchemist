package it.unibo.alchemist.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class FileNameDialogController {

    @FXML
    private Button btnCancel;
    @FXML
    private Button btnOk;
    @FXML
    private Label fileName;
    @FXML
    private Label lbEx;
    @FXML
    private TextField tfNameFile;

    private Stage dialogStage;
    private String extension;

    /**
     * 
     */
    public void initialize() {
        this.btnCancel.setText(R.getString("cancel"));
        this.btnOk.setText(R.getString("ok"));
        this.fileName.setText(R.getString("file_name"));
    }

    /**
     * Sets the stage.
     * @param dialog dialog stage
     */
    public void setDialogStage(final Stage dialog) {
        this.dialogStage = dialog;
    }

    /**
     * Sets the extension of the file.
     * @param extension file extension
     */
    public void setExtension(final String extension) {
        this.extension = extension;
        this.lbEx.setText(extension);
    }

    @FXML
    private void clickOK() {
        if (!this.tfNameFile.getText().isEmpty()) {
            final String userDirectory = System.getProperty("user.home"); //TODO: get root position from project file
            final File file = new File(userDirectory + File.separator + tfNameFile.getText() + this.extension);
            final Desktop desk = Desktop.getDesktop();
            try {
                if (!file.exists()) {
                    file.createNewFile();
                    this.dialogStage.close();
                    desk.open(file);
                } else {
                    final Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(R.getString("file_name_exists"));
                    alert.setHeaderText("This file name already exists in this location");
                    alert.setContentText("Do you want edit the existing file?");
                    final Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        this.dialogStage.close();
                        desk.open(file);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            final Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("File name wrong");
            alert.setHeaderText("The inserted file name is wrong.");
            alert.setContentText("Please type a file name at least along one character.");
            alert.showAndWait();
        }
    }

    @FXML
    private void clickCancel() {
        this.dialogStage.close();
    }

}
