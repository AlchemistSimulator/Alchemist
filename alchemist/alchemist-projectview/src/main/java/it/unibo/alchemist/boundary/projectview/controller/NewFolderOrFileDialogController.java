/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.projectview.controller;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * 
 *
 */
public class NewFolderOrFileDialogController {

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
     * @throws IOException 
     * 
     */
    @FXML
    public void clickOk() throws IOException {
        final String name = textField.getText();
        if (name.isEmpty()) {
            if (isFolder) {
                setAlert("empty_folder_name", "empty_folder_name_header", "empty_folder_name_content");
            } else {
                setAlert("empty_file_name", "empty_file_name_header", "empty_file_name_content");
            }
        } else {
            final File newFile = new File((new File(this.selectedItem).isDirectory()
                    ? selectedItem
                    : selectedItem.replaceFirst("\\/([^\\/]+)\\/?$", "")) + File.separator + name);
            if (newFile.exists()) {
                if (isFolder) {
                    setAlert("folder_name_exist", "folder_name_exist_header", "folder_name_exist_content");
                } else {
                    setAlert("file_name_exists", "new_file_name_exist_header", "new_file_name_exist_content");
                }
            } else {
                if (isFolder) {
                    if (newFile.mkdirs()) {
                        this.stage.close();
                    } else {
                        throw new IllegalStateException("Could not create " + newFile);
                    }
                } else {
                    if (newFile.createNewFile()) {
                        this.stage.close();
                    } else {
                        throw new IllegalStateException("Could not create " + newFile);
                    }
                }
            }
        }
        this.stage.close();
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
        alert.setTitle(RESOURCES.getString(title));
        alert.setHeaderText(RESOURCES.getString(header));
        alert.setContentText(RESOURCES.getString(content));
        alert.showAndWait();
    }

}
