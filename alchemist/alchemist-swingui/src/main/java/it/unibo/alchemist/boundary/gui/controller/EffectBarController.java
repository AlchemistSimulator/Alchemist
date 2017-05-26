package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.EffectCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * This class models a JavaFX controller for EffectBar.fxml.
 */
public class EffectBarController implements Initializable {
    /** Layout path. */
    public static final String EFFECT_BAR_LAYOUT = "EffectBar";

    @FXML
    private JFXButton addEffect;
    @FXML
    private ListView<Effect> effectsList;

    private ObservableList<Effect> observableList;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert addEffect != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_BAR_LAYOUT);
        assert effectsList != null : FXResourceLoader.getInjectionErrorMessage("effectsList", EFFECT_BAR_LAYOUT);

        this.addEffect.setText("");
        this.addEffect.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.addEffect.setOnAction(e -> addGroupToList("Effect " + (this.getObservableList().size() + 1)));

    }

    private void addGroupToList(final String name) {
        // TODO
        // this.getObservableList().add(new Effect()); // TODO
        // this.getObservableList().get(this.getObservableList().size() - 1).setName(name); // TODO add setName to effect
        // TODO
        this.effectsList.refresh();
    }

    private ObservableList<Effect> getObservableList() {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            this.effectsList.setItems(observableList);
            this.effectsList.setCellFactory(lv -> new EffectCell());
            // TODO check
        }
        return this.observableList;
    }
}
