package it.unibo.alchemist.boundary.gui.controller;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfoenix.controls.JFXButton;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.property.EnumProperty;
import it.unibo.alchemist.boundary.gui.view.property.RangedDoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * This class models a JavaFX controller for EffectProperties.fxml.
 * <p>
 * Using the FXML design it builds the basic components, then using reflection
 * on the effect specified in {@link #EffectPropertiesController(EffectFX)
 * constructor} it builds up the other effect-specific controls.
 */
public class EffectPropertiesController implements Initializable {
    /** Default generated Serial Version UID. */
    private static final String EFFECT_PROPERTIES_LAYOUT = "EffectProperties";
    /** Default {@code Logger}. */
    private static final Logger L = LoggerFactory.getLogger(EffectPropertiesController.class);

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

    private final EffectFX effect;

    private final List<Node> dynamicNodes = new ArrayList<>();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert effectsPane != null : FXResourceLoader.getInjectionErrorMessage("effectsPane", EFFECT_PROPERTIES_LAYOUT);
        assert topBar != null : FXResourceLoader.getInjectionErrorMessage("topBar", EFFECT_PROPERTIES_LAYOUT);
        assert backToGroups != null : FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_PROPERTIES_LAYOUT);
        assert effectName != null : FXResourceLoader.getInjectionErrorMessage("effectName", EFFECT_PROPERTIES_LAYOUT);
        assert mainBox != null : FXResourceLoader.getInjectionErrorMessage("mainBox", EFFECT_PROPERTIES_LAYOUT);

    }

    /**
     * Default constructor.
     * 
     * @param effect
     *            to tune with this GUI component
     */
    public EffectPropertiesController(final EffectFX effect) {
        this.effect = effect;

        final List<Field> properties = Arrays.asList(this.effect.getClass().getFields()).stream()
                .filter(f -> Property.class.isAssignableFrom(f.getType())).collect(Collectors.toList());

        if (!properties.isEmpty()) {
            this.parseRangedDoubleFields(properties);
            this.parseStringFields(properties);
            this.parseEnumFields(properties);

            dynamicNodes.forEach(n -> mainBox.getChildren().add(n)); // TODO
        }

    }

    /**
     * Checks every property in the list for {@link RangedDoubleProperty} and
     * creates a new {@link Spinner} for each one.
     * 
     * @param fields
     *            the list of fields
     */
    private void parseRangedDoubleFields(final List<Field> fields) {
        fields.stream().filter(f -> RangedDoubleProperty.class.isAssignableFrom(f.getType())).forEach(f -> {
            final boolean isAccessible = f.isAccessible();
            try {
                f.setAccessible(true);
                EffectPropertiesController.this.buildSpinner((RangedDoubleProperty) f.get(this.effect));
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                L.error(e.getMessage());
            } finally {
                f.setAccessible(isAccessible);
            }
        });
    }

    /**
     * Checks every property in the list for {@link StringProperty} and creates
     * a new {@link TextField} for each one.
     * 
     * @param fields
     *            the list of fields
     */
    private void parseStringFields(final List<Field> fields) {
        fields.stream().filter(f -> StringProperty.class.isAssignableFrom(f.getType())).forEach(f -> {
            final boolean isAccessible = f.isAccessible();
            try {
                f.setAccessible(true);
                EffectPropertiesController.this.buildTextField((StringProperty) f.get(this.effect));
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                L.error(e.getMessage());
            } finally {
                f.setAccessible(isAccessible);
            }
        });
    }

    private void parseEnumFields(final List<Field> fields) {
        // TODO Auto-generated method stub

    }

    /**
     * Builds a new {@link Spinner} from a {@link RangedDoubleProperty}, binds
     * its {@link Spinner#valueProperty() valueProperty} to the
     * {@code RangedDoubleProperty} and adds it to internal list of nodes.
     * 
     * @param doubleProperty
     *            the model of the spinner
     */
    private void buildSpinner(final RangedDoubleProperty doubleProperty) {

        final Spinner<Double> spinner = new Spinner<>(doubleProperty.getLowerBound(), doubleProperty.getUpperBound(), doubleProperty.get());
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> doubleProperty.set(newValue));

        this.dynamicNodes.add(spinner);
    }

    /**
     * Builds a new {@link TextField} from a {@link StringProperty}, binds its
     * {@link TextField#textProperty() textProperty} to the
     * {@code StringProperty} and adds it to internal list of nodes.
     * 
     * @param doubleProperty
     *            the model of the spinner
     */
    private void buildTextField(final StringProperty stringProperty) {
        final TextField textField = new TextField(stringProperty.get());
        textField.textProperty().bindBidirectional(stringProperty);

        this.dynamicNodes.add(textField);
    }

    private void buildComboBox(final EnumProperty<Enum<?>> stringProperty) {
        // TODO Auto-generated method stub

    }

}
