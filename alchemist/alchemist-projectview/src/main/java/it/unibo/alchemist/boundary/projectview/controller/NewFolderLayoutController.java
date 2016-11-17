package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.util.ResourceBundle;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
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
public class NewFolderLayoutController {

    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

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
     */
    public void initialize() {
        this.label.setText(RESOURCES.getString("folder_name"));
        this.textField.setPromptText(RESOURCES.getString("enter_folder_name"));
        this.okBtn.setText(RESOURCES.getString("ok"));
        this.cancelBtn.setText(RESOURCES.getString("cancel"));
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
        if (this.textField.getText().isEmpty()) {
            setAlert(RESOURCES.getString("empty_folder_name"), RESOURCES.getString("empty_folder_name_header"), RESOURCES.getString("empty_folder_name_content"));
        } else {
            File newFolder;
            if (new File(this.selectedItem).isDirectory()) {
                newFolder = new File(this.selectedItem + File.separator + this.textField.getText());
            } else {
                final String fileName = new File(this.selectedItem).getName();
                newFolder = new File(this.selectedItem.replace(fileName, "") + File.separator + this.textField.getText());
            }
            if (newFolder.exists()) {
                setAlert(RESOURCES.getString("folder_name_exist"), RESOURCES.getString("folder_name_exist"), RESOURCES.getString("folder_name_exist"));
            } else {
                newFolder.mkdir();
                this.stage.close();
            }
        }
    }

    private void setAlert(final String title, final String header, final String content) {
        final Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
