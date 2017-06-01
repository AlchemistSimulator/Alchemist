package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class EffectPropertiesController implements Initializable {
    private static final String EFFECT_PROPERTIES_LAYOUT = "EffectProperties";

    @FXML
    private BorderPane effectsPane;
    @FXML
    private ButtonBar topBar;
    @FXML
    private JFXButton backToGroups;
    @FXML
    private Label effectName;
    @FXML
    private VBox mainBox;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert effectsPane != null : FXResourceLoader.getInjectionErrorMessage("effectsPane", EFFECT_PROPERTIES_LAYOUT);
        assert topBar != null : FXResourceLoader.getInjectionErrorMessage("topBar", EFFECT_PROPERTIES_LAYOUT);
        assert backToGroups != null : FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_PROPERTIES_LAYOUT);
        assert effectName != null : FXResourceLoader.getInjectionErrorMessage("effectName", EFFECT_PROPERTIES_LAYOUT);
        assert mainBox != null : FXResourceLoader.getInjectionErrorMessage("mainBox", EFFECT_PROPERTIES_LAYOUT);

    }

}
