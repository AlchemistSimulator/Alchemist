package it.unibo.alchemist.boundary.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.PopOver;
import org.reflections.Reflections;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.EffectCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * This class models a JavaFX controller for EffectBar.fxml.
 */
public class EffectBarController implements Initializable {
    /** Layout path. */
    public static final String EFFECT_BAR_LAYOUT = "EffectBar";

    private static final Reflections REFLECTIONS = new Reflections("it.unibo.alchemist");
    private static final Set<Class<? extends Effect>> EFFECTS = REFLECTIONS.getSubTypesOf(Effect.class);

    @FXML
    private JFXButton addEffect;
    @FXML
    private ListView<Effect> effectsList;

    private ObservableList<Effect> observableList;
    private EffectBuilderFXPopover effectBuilder;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert addEffect != null : FXResourceLoader.getInjectionErrorMessage("add", EFFECT_BAR_LAYOUT);
        assert effectsList != null : FXResourceLoader.getInjectionErrorMessage("effectsList", EFFECT_BAR_LAYOUT);

        this.addEffect.setText("");
        this.addEffect.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ADD));

        this.effectBuilder = new EffectBuilderFXPopover();
        this.addEffect.setOnAction(e -> {
            if (this.effectBuilder.isShowing()) {
                this.effectBuilder.hide();
            } else {
                this.effectBuilder.show(this.addEffect);
            }
        });

    }

    private void addEffectToList(final String name) {
        final Optional<Class<? extends Effect>> clazz = this.effectBuilder.getResult();
        if (clazz.isPresent()) {
            this.getObservableList().add(this.effectBuilder.instantiateEffect(clazz.get()));
            // this.getObservableList().get(this.getObservableList().size()
            // -1).setName(name);
            // TODO add setName to effect
            this.effectsList.refresh();
        }
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

    /**
     * PopOver that lets the user choose the effect.
     */
    private class EffectBuilderFXPopover extends PopOver {

        private final List<Class<? extends Effect>> effects;
        private Optional<Class<? extends Effect>> choice = Optional.empty();

        /**
         * Defalut constructor.
         */
        EffectBuilderFXPopover() {
            super(addEffect);
            effects = new ArrayList<>(EFFECTS);

            final BorderPane pane = new BorderPane();
            final JFXComboBox<Class<? extends Effect>> comboBox = new JFXComboBox<>(FXCollections.observableArrayList(effects));
            pane.setCenter(comboBox);
            final ButtonBar bar = new ButtonBar();
            final JFXButton okButton = new JFXButton("OK");
            okButton.setOnAction(e -> {
                choice = Optional.ofNullable(comboBox.getValue());
                EffectBuilderFXPopover.this.hide();
                addEffectToList("Effect " + getObservableList().size() + 1);
            });
            ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
            bar.getButtons().add(okButton);
            pane.setBottom(bar);
            EffectBuilderFXPopover.this.setContentNode(pane);
        }

        /**
         * Returns the class chosen by the user.
         * 
         * @return the class of the effect, or an empty optional if no class was
         *         chosen
         */
        public Optional<Class<? extends Effect>> getResult() {
            final Optional<Class<? extends Effect>> returnChoice = this.choice.isPresent() ? Optional.of(this.choice.get())
                    : Optional.empty();
            this.choice = Optional.empty();
            return returnChoice;
        }

        /**
         * Instantiates the desired effect.
         * 
         * @param clazz
         *            the class of the effect
         * @return the effect instantiated
         */
        public Effect instantiateEffect(final Class<? extends Effect> clazz) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Could not instantiate the effect", e);
            }
        }
    }
}
