package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class EffectsGroupBarController implements Initializable {
    private static final String EFFECT_BAR_LAYOUT = "EffectBarLayout.fxml";

    @FXML
    private JFXButton save;
    @FXML
    private JFXButton load;
    @FXML
    private JFXButton add;


    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert save != null : FXResourceLoader.getInjectionErrorMessage("save", EFFECT_BAR_LAYOUT);
        assert load != null : FXResourceLoader.getInjectionErrorMessage("load", EFFECT_BAR_LAYOUT);
        assert add != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_BAR_LAYOUT);
    }

}
