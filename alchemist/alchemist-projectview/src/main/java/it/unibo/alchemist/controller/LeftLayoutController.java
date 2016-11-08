package it.unibo.alchemist.controller;

import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * 
 *
 */
public class LeftLayoutController {

    @FXML
    private Button run;

    /**
     * 
     */
    public void initialize() {
        this.run.setText(R.getString("run"));
    }

}
