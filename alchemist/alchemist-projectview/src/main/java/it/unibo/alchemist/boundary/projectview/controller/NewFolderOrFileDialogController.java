package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewFolderOrFileDialogController {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

    private boolean isFolder;
    @FXML
    private Button okBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label label;
    private Stage stage;
    private String selectedItem;
    @FXML
    private TextField textField;

    /**
     * 
     * @param isFolder True if the layout is to create a new folder, otherwise false. 
     */
    public void initialize(final boolean isFolder) {
        this.isFolder = isFolder;
        if (this.isFolder) {
            setField(RESOURCES.getString("folder_name"), RESOURCES.getString("enter_folder_name"));
        } else {
            setField(RESOURCES.getString("new_file_name"), RESOURCES.getString("enter_file_name_ext"));
        }
    }

    /**
     * 
     * @param selectedItem A selected item of tree view.
     */
    public void setSelectedItem(final String selectedItem) {
        this.selectedItem = selectedItem;
    }

    /**
     * 
     * @param stage The stage of layout.
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    /**
     * 
     */
    @FXML
    public void clickOk() {
        if (this.textField.getText().isEmpty() && this.isFolder) {
            setAlert(RESOURCES.getString("empty_folder_name"), RESOURCES.getString("empty_folder_name_header"), RESOURCES.getString("empty_folder_name_content"));
        } else if (this.textField.getText().isEmpty() && !this.isFolder) {
            setAlert(RESOURCES.getString("empty_file_name"), RESOURCES.getString("empty_file_name_header"), RESOURCES.getString("empty_file_name_content"));
        } else {
            File newFile;
            if (new File(this.selectedItem).isDirectory()) {
                newFile = new File(this.selectedItem + File.separator + this.textField.getText());
            } else {
                final String fileName = new File(this.selectedItem).getName();
                newFile = new File(this.selectedItem.replace(fileName, "") + File.separator + this.textField.getText());
            }
            if (this.isFolder) {
                if (newFile.exists()) {
                    setAlert(RESOURCES.getString("folder_name_exist"), RESOURCES.getString("folder_name_exist_header"), RESOURCES.getString("folder_name_exist_content"));
                } else {
                    newFile.mkdir();
                    this.stage.close();
                }
            } else {
                final String filename = newFile.getName();
                final int dot = filename.lastIndexOf('.');
                String extension = "";
                if (dot != -1) {
                    extension = filename.substring(dot);
                }
                if (extension.isEmpty()) {
                    setAlert(RESOURCES.getString("file_wrong"), RESOURCES.getString("not_file_header"), RESOURCES.getString("not_file_content"));
                } else if (newFile.exists()) {
                    setAlert(RESOURCES.getString("file_name_exists"), RESOURCES.getString("new_file_name_exist_header"), RESOURCES.getString("new_file_name_exist_content"));
                } else {
                    try {
                        newFile.createNewFile();
                    } catch (IOException e) {
                        L.error("I/O error during the creation of new file.", e);
                    }
                    this.stage.close();
                }
            }
        }
    }

    /**
     * 
     */
    @FXML
    public void clickCancel() {
        this.stage.close();
    }

    private void setField(final String label, final String textField) {
        this.label.setText(label);
        this.textField.setPromptText(textField);
        this.okBtn.setText(RESOURCES.getString("ok"));
        this.cancelBtn.setText(RESOURCES.getString("cancel"));
    }

    private void setAlert(final String title, final String header, final String content) {
        final Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
