package it.unibo.alchemist.controller;

import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * 
 *
 */
public class NewProjLayoutFolderController {

    @FXML
    private Button next;
    @FXML
    private Button selectFolder;

    /**
     * 
     */
    public void initialize() {
        this.next.setText(R.getString("next"));
        this.selectFolder.setText(R.getString("select_folder"));
    }
}
