package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;

import it.unibo.alchemist.boundary.gui.effects.EffectBuilderFX;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.cells.EffectCell;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
    private ListView<EffectFX> effectsList;
    @FXML
    private Label groupName;
    @FXML
    private JFXButton backToGroups;

    private ObservableList<EffectFX> observableList;
    private EffectBuilderFX effectBuilder;

    private final JFXDrawersStack stack;
    private final JFXDrawer thisDrawer;

    /**
     * Default constructor.
     * 
     * @param stack
     *            the stack where to open the effect properties
     * @param thisDrawer
     *            the drawer the layout this controller is assigned to is loaded
     *            into
     */
    public EffectBarController(final JFXDrawersStack stack, final JFXDrawer thisDrawer) {
        this.stack = stack;
        this.thisDrawer = thisDrawer;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert addEffect != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_BAR_LAYOUT);
        assert effectsList != null : FXResourceLoader.getInjectionErrorMessage("effectsList", EFFECT_BAR_LAYOUT);
        assert groupName != null : FXResourceLoader.getInjectionErrorMessage("groupName", EFFECT_BAR_LAYOUT);
        assert backToGroups != null : FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_BAR_LAYOUT);

        this.addEffect.setText("");
        this.addEffect.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.backToGroups.setText("");
        this.backToGroups.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ARROW_BACK));

        this.effectBuilder = new EffectBuilderFX();

        this.addEffect.setOnAction(e -> addEffectToList());

        this.backToGroups.setOnAction(e -> {
            this.stack.toggle(thisDrawer);
        });

    }

    /**
     * Opens a {@link Dialog}, and when user choose an {@link EffectFX effect},
     * adds it to the {@link ObservableList list}.
     */
    private void addEffectToList() {
        final EffectFX choice = effectBuilder.chooseAndLoad();
        if (choice != null) {
            this.getObservableList().add(choice);
            this.getObservableList().get(this.getObservableList().size() - 1).setName(choice.getName() + " " + getObservableList().size());
            this.effectsList.refresh();
        }
    }

    /**
     * Getter method and lazy initializer for the internal
     * {@link ObservableList}.
     * 
     * @return the {@code ObservableList} associated to the controlled
     *         {@link ListView}
     */
    private ObservableList<EffectFX> getObservableList() {
        if (this.observableList == null) {
            this.observableList = FXCollections.observableArrayList();
            this.effectsList.setItems(observableList);
            this.effectsList.setCellFactory(lv -> new EffectCell(stack));
            // TODO check
        }
        return this.observableList;
    }

    /**
     * The name property of this representation of the group.
     * 
     * @return the name property
     */
    public StringProperty groupNameProperty() {
        return this.groupName.textProperty();
    }
}
