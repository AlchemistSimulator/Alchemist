package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.Optional;
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
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
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

        this.load.setText("");
        this.load.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FOLDER_OPEN));

        this.addGroup.setText("");
        this.addGroup.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.addGroup.setOnAction(e -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.initModality(Modality.WINDOW_MODAL); // TODO check
            dialog.setTitle("Effect group name");
            dialog.setContentText("Choose a name for the group of effects:");

            // Traditional way to get the response value.
            final Optional<String> result = dialog.showAndWait();

            result.ifPresent(name -> addGroupToList(name));
        });

    }

    private void addGroupToList(final String name) {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            this.effectGroupsList.setItems(observableList);
            this.effectGroupsList.setCellFactory(lv -> new EffectGroupCell(name)); // TODO check
        }
        this.observableList.add(new EffectStack());

    }

}
