package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.effects.EffectStack;
import it.unibo.alchemist.boundary.gui.view.EffectGroupCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * This class models a JavaFX controller for EffectsGroupBar.fxml.
 */
public class EffectsGroupBarController implements Initializable {
    /** Layout path. */
    public static final String EFFECT_GROUP_BAR_LAYOUT = "EffectsGroupBar";

    @FXML
    private JFXButton save;
    @FXML
    private JFXButton load;
    @FXML
    private JFXButton addGroup;
    @FXML
    private ListView<EffectGroup> effectGroupsList;

    private ObservableList<EffectGroup> observableList;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert save != null : FXResourceLoader.getInjectionErrorMessage("save", EFFECT_GROUP_BAR_LAYOUT);
        assert load != null : FXResourceLoader.getInjectionErrorMessage("load", EFFECT_GROUP_BAR_LAYOUT);
        assert addGroup != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_GROUP_BAR_LAYOUT);
        assert effectGroupsList != null : FXResourceLoader.getInjectionErrorMessage("effectGroupsList", EFFECT_GROUP_BAR_LAYOUT);

        this.save.setText("");
        this.save.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.SAVE));
        this.save.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save effect");
            fileChooser.showSaveDialog(this.save.getScene().getWindow());
            // TODO Save the file
        });

        this.load.setText("");
        this.load.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FOLDER_OPEN));
        this.load.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load effect");
            fileChooser.showOpenDialog(this.load.getScene().getWindow());
            // TODO Do something with the loaded file
        });

        this.addGroup.setText("");
        this.addGroup.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.addGroup.setOnAction(e -> {
            addGroupToList("Effect group " + (getObservableList().size() + 1));
        });
    }

    private void addGroupToList(final String name) {
        this.getObservableList().add(new EffectStack());
        this.getObservableList().get(this.getObservableList().size() - 1).setName(name);
        this.effectGroupsList.refresh();
    }

    private ObservableList<EffectGroup> getObservableList() {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            this.effectGroupsList.setItems(observableList);
            this.effectGroupsList.setCellFactory(lv -> new EffectGroupCell());
            // TODO check
        }
        return this.observableList;
    }

}
