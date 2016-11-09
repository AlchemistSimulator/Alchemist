package it.unibo.alchemist.controller;

import it.unibo.alchemist.boundary.l10n.R;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * 
 *
 */
public class NewProjLayoutSelectController {

    @FXML
    private Button backBtn;
    @FXML
    private Button finishBtn;
    @FXML
    private Label select;

    /**
     * 
     */
    public void initialize() {
        this.backBtn.setText(R.getString("back"));
        this.finishBtn.setText(R.getString("finish"));
        this.select.setText(R.getString("select"));
    }

    /**
     * 
     */
    @FXML
    public void clickOk() {
        System.out.println();
    }

}
