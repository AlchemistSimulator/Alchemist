package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

/**
 * This class models a JavaFX controller for ControlTypePopoverLayout.fxml.
 */
public class ControlTypePopoverController implements Initializable {
    private static final String CONTROL_TYPE_POPOVER_LAYOUT = "ControlTypePopoverLayout.fxml";

    // FXML components
    @FXML
    private JFXButton panButton; // Value injected by FXMLLoader
    @FXML
    private JFXButton selectButton; // Value injected by FXMLLoader

    private final EventHandler<ActionEvent> panButtonHandler;
    private final EventHandler<ActionEvent> selectButtonHandler;

    /**
     * Default constructor. It initializes the two buttons with the provided
     * handlers.
     * 
     * @param panButtonHandler
     *            the handler for the mouse click on the pan button
     * @param selectButtonHandler
     *            the handler for the mouse click on the select button
     */
    public ControlTypePopoverController(final EventHandler<ActionEvent> panButtonHandler,
            final EventHandler<ActionEvent> selectButtonHandler) {
        super();
        this.panButtonHandler = panButtonHandler;
        this.selectButtonHandler = selectButtonHandler;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        assert panButton != null : FXResourceLoader.getInjectionErrorMessage("panButton", CONTROL_TYPE_POPOVER_LAYOUT);
        assert selectButton != null : FXResourceLoader.getInjectionErrorMessage("selectButton", CONTROL_TYPE_POPOVER_LAYOUT);

        panButton.setText("");
        panButton.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL));
        if (panButtonHandler != null) {
            panButton.setOnAction(panButtonHandler);
        }

        selectButton.setText("");
        selectButton.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED));
        if (selectButtonHandler != null) {
            selectButton.setOnAction(selectButtonHandler);
        }
    }

    /**
     * Getter method for the pan button.
     * 
     * @return the pan button
     */
    public JFXButton getPanButton() {
        return this.panButton;
    }

    /**
     * Getter method for the select button.
     * 
     * @return the select button
     */
    public JFXButton getSelectButton() {
        return this.selectButton;
    }

}
