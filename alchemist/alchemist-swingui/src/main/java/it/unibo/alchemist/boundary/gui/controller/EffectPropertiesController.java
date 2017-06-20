package it.unibo.alchemist.boundary.gui.controller;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;

import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.view.properties.SerializableEnumProperty;
import it.unibo.alchemist.boundary.gui.view.properties.RangedDoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import jiconfont.icons.GoogleMaterialDesignIcons;

/**
 * This class models a JavaFX controller for EffectProperties.fxml.
 * <p>
 * Using the FXML design it builds the basic components, then using reflection
 * on the effect specified in {@link #EffectPropertiesController(EffectFX)
 * constructor} it builds up the other effect-specific controls.
 */
public class EffectPropertiesController implements Initializable {
    /** Layout path. */
    public static final String EFFECT_PROPERTIES_LAYOUT = "EffectProperties";
    /** Default {@code Logger}. */
    private static final Logger L = LoggerFactory.getLogger(EffectPropertiesController.class);

    @FXML
    private BorderPane effectsPane;
    @FXML
    private ButtonBar topBar;
    @FXML
    private JFXButton backToEffects;
    @FXML
    private Label effectName;
    @FXML
    private VBox mainBox;

    private final EffectFX effect;

    private final Map<Label, Node> dynamicNodes = new HashMap<>();
    private final JFXDrawersStack stack;
    private final JFXDrawer thisDrawer;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert effectsPane != null : FXResourceLoader.getInjectionErrorMessage("effectsPane", EFFECT_PROPERTIES_LAYOUT);
        assert topBar != null : FXResourceLoader.getInjectionErrorMessage("topBar", EFFECT_PROPERTIES_LAYOUT);
        assert backToEffects != null : FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_PROPERTIES_LAYOUT);
        assert effectName != null : FXResourceLoader.getInjectionErrorMessage("effectName", EFFECT_PROPERTIES_LAYOUT);
        assert mainBox != null : FXResourceLoader.getInjectionErrorMessage("mainBox", EFFECT_PROPERTIES_LAYOUT);

        final List<Field> properties = FieldUtils.getAllFieldsList(effect.getClass()).stream()
                .filter(f -> Property.class.isAssignableFrom(f.getType())).collect(Collectors.toList());

        if (!properties.isEmpty()) {
            this.parseRangedDoubleFields(properties);
            this.parseStringFields(properties);
            this.parseEnumFields(properties);

            if (!this.dynamicNodes.isEmpty()) {
                this.dynamicNodes.entrySet().forEach(e -> {
                    final HBox row = new HBox(e.getKey(), e.getValue());
                    row.setAlignment(Pos.CENTER);
                    this.mainBox.getChildren().add(row);
                });
            } else {
                this.showNothing();
            }
        } else {
            this.showNothing();
        }

        this.backToEffects.setText("");
        this.backToEffects.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.ARROW_BACK));
        this.backToEffects.setOnAction(e -> {
            this.stack.toggle(thisDrawer);
        });
    }

    /**
     * Default constructor.
     * 
     * @param effect
     *            to tune with this GUI component
     * @param stack
     *            the stack that contains the drawer that own the layout this
     *            class controls
     * @param thisDrawer
     *            the drawer that own the layout this class controls
     */
    public EffectPropertiesController(final EffectFX effect, final JFXDrawersStack stack, final JFXDrawer thisDrawer) {
        if (effect == null) {
            throw new IllegalArgumentException("Effect cannot be null!");
        }
        if (stack == null) {
            throw new IllegalArgumentException("Drawer Stack cannot be null!");
        }
        if (thisDrawer == null) {
            throw new IllegalArgumentException("Drawer cannot be null!");
        }

        this.effect = effect;
        this.stack = stack;
        this.thisDrawer = thisDrawer;

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

    /**
     * Checks every property in the list for {@link SerializableEnumProperty} and creates a
     * new {@link ComboBox} for each one.
     * 
     * @param fields
     *            the list of fields
     */
    private void parseEnumFields(final List<Field> fields) {
        fields.stream().filter(f -> SerializableEnumProperty.class.isAssignableFrom(f.getType())).forEach(f -> {
            final boolean isAccessible = f.isAccessible();
            try {
                f.setAccessible(true);
                final Object enumProperty = f.get(this.effect);
                if (enumProperty.getClass().isEnum()) {
                    EffectPropertiesController.this.buildComboBox((SerializableEnumProperty<?>) enumProperty);
                }
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                L.error(e.getMessage());
            } finally {
                f.setAccessible(isAccessible);
            }
        });
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
        final SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(doubleProperty.getLowerBound(),
                doubleProperty.getUpperBound(), doubleProperty.get(), 0.01);
        final Spinner<Double> spinner = new Spinner<>(factory);
        spinner.setEditable(true);
        final TextFormatter<Double> formatter = new TextFormatter<Double>(factory.getConverter(), factory.getValue());
        spinner.getEditor().setTextFormatter(formatter);
        factory.valueProperty().bindBidirectional(formatter.valueProperty());
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> doubleProperty.set(newValue));

        this.dynamicNodes.put(new Label(doubleProperty.getName()), spinner);
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

        this.dynamicNodes.put(new Label(stringProperty.getName()), textField);
    }

    /**
     * Builds a new {@link ComboBox} from a {@link SerializableEnumProperty}, binds its
     * {@link ComboBox#valueProperty() valueProperty} to the
     * {@code EnumProperty} and adds it to internal list of nodes.
     * 
     * @param enumProperty
     *            the model of the spinner
     * @param <T>
     *            the enum type wrapped by the enumProperty
     */
    private <T extends Enum<T>> void buildComboBox(final SerializableEnumProperty<T> enumProperty) {
        final ObservableList<T> list = FXCollections.observableArrayList(enumProperty.values());

        final ComboBox<T> comboBox = new ComboBox<>(list);
        comboBox.setValue(list.get(0));

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> enumProperty.setValue(newValue));

        this.dynamicNodes.put(new Label(enumProperty.getName()), comboBox);
    }

    private void showNothing() {
        L.debug("Effect " + effect.toString() + " does not have tunable properties");
        final Label nothingHere = new Label("Nothing tunable here");
        nothingHere.setTextAlignment(TextAlignment.CENTER);
        mainBox.getChildren().add(nothingHere);
    }

}
