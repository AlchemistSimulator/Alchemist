package it.unibo.alchemist.boundary.projectview.controller;

import it.unibo.alchemist.boundary.l10n.LocalizedResourceBundle;
import it.unibo.alchemist.boundary.projectview.ProjectGUI;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 *
 */
public class FileNameDialogController implements Initializable {

    private static final Logger L = LoggerFactory.getLogger(ProjectGUI.class);
    private static final ResourceBundle RESOURCES = LocalizedResourceBundle.get("it.unibo.alchemist.l10n.ProjectViewUIStrings");

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

    private LeftLayoutController ctrlLeft;
    private Stage dialogStage;
    private String extension;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.btnCancel.setText(RESOURCES.getString("cancel"));
        this.btnOk.setText(RESOURCES.getString("ok"));
        this.fileName.setText(RESOURCES.getString("file_name"));
        this.tfNameFile.setPromptText(RESOURCES.getString("enter_file_name"));
    }

    /**
     * Sets the stage.
     *
     * @param dialog dialog stage
     */
    public void setDialogStage(final Stage dialog) {
        this.dialogStage = dialog;
    }

    /**
     * Sets the extension of the file.
     *
     * @param extension File extension
     */
    public void setExtension(final String extension) {
        this.extension = extension;
        this.lbEx.setText(extension);
    }

    /**
     * @param ctrl Left Layout controller
     */
    public void setCtrlLeftLayout(final LeftLayoutController ctrl) {
        this.ctrlLeft = ctrl;
    }

    /**
     *
     */
    @FXML
    protected void clickOK() {
        if (!this.tfNameFile.getText().isEmpty()) {
            final String projPath = this.ctrlLeft.getPathFolder();
            final File file;
            final String path = projPath + File.separator + "src" + File.separator;
            if (this.extension.equals(RESOURCES.getString("yaml_ext"))) {
                file = new File(path + "yaml" + File.separator + tfNameFile.getText() + this.extension);
            } else {
                file = new File(path + "json" + File.separator + tfNameFile.getText() + this.extension);
            }
            final Desktop desk = Desktop.getDesktop();
            try {
                if (!file.exists() && file.createNewFile()) {
                    this.dialogStage.close();
                    this.ctrlLeft.setTreeView(new File(projPath));
                    desk.open(file);
                } else {
                    final Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(RESOURCES.getString("file_name_exists"));
                    alert.setHeaderText(RESOURCES.getString("file_name_exists_header"));
                    alert.setContentText(RESOURCES.getString("file_name_exists_content"));
                    alert.showAndWait()
                            .ifPresent(buttonType -> {
                                if (buttonType == ButtonType.OK) {
                                    this.dialogStage.close();
                                    try {
                                        desk.open(file);
                                    } catch (final IOException e) {
                                        L.warn(e.getMessage(), e);
                                        throw new UncheckedIOException(e);
                                    }
                                }
                            });
                }
            } catch (IOException e) {
                L.error("Error creation new file.", e);
                System.exit(1);
            }
        } else {
            final Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle(RESOURCES.getString("file_name_wrong"));
            alert.setHeaderText(RESOURCES.getString("file_name_wrong_header"));
            alert.setContentText(RESOURCES.getString("file_name_wrong_content"));
            alert.showAndWait();
        }
    }

    /**
     *
     */
    @FXML
    protected void clickCancel() {
        this.dialogStage.close();
    }

}
