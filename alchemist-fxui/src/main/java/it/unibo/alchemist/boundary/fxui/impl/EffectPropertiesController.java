/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXSlider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.fxui.util.RangedDoubleProperty;
import it.unibo.alchemist.boundary.fxui.EffectFX;
import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.util.ResourceLoader;
import it.unibo.alchemist.boundary.fxui.util.SVGImages;
import it.unibo.alchemist.boundary.fxui.properties.RangedIntegerProperty;
import it.unibo.alchemist.boundary.fxui.properties.SerializableBooleanProperty;
import it.unibo.alchemist.boundary.fxui.properties.SerializableEnumProperty;
import it.unibo.alchemist.boundary.fxui.properties.SerializableStringProperty;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * This class models a JavaFX controller for EffectProperties.fxml.
 * <p>
 * Using the FXML design it builds the basic components, then using reflection
 * on the effect specified in {@link #EffectPropertiesController(EffectFX, JFXDrawersStack, JFXDrawer) constructor}
 * it builds up the other effect-specific controls.
 */
@SuppressFBWarnings(
        value = { "NP_NULL_ON_SOME_PATH", "EI_EXPOSE_REP2" },
        justification = "Using assert to null-check avoids the possibility of null references"
)
public class EffectPropertiesController implements Initializable {
    /**
     * Layout path.
     */
    public static final String EFFECT_PROPERTIES_LAYOUT = "EffectProperties";
    /**
     * Default {@code Logger}.
     */
    private static final Logger L = LoggerFactory.getLogger(EffectPropertiesController.class);
    private final EffectFX<?> effect;
    private final Map<Label, Node> dynamicNodes = new HashMap<>();
    private final JFXDrawersStack stack;
    private final JFXDrawer thisDrawer;
    @FXML
    @Nullable
    private BorderPane effectsPane; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private ButtonBar topBar; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton backToEffects; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private Label effectName; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private VBox mainBox; // Value injected by FXMLLoader

    /**
     * Default constructor.
     *
     * @param effect     to tune with this GUI component
     * @param stack      the stack that contains the drawer that own the layout this
     *                   class controls
     * @param thisDrawer the drawer that own the layout this class controls
     */
    public EffectPropertiesController(final EffectFX<?> effect, final JFXDrawersStack stack, final JFXDrawer thisDrawer) {
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
     * {@inheritDoc}
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        Objects.requireNonNull(
                effectsPane,
                FXResourceLoader.getInjectionErrorMessage("effectsPane", EFFECT_PROPERTIES_LAYOUT)
        );
        Objects.requireNonNull(
                topBar,
                FXResourceLoader.getInjectionErrorMessage("topBar", EFFECT_PROPERTIES_LAYOUT)
        );
        Objects.requireNonNull(
                backToEffects,
                FXResourceLoader.getInjectionErrorMessage("backToGroups", EFFECT_PROPERTIES_LAYOUT)
        );
        Objects.requireNonNull(
                effectName,
                FXResourceLoader.getInjectionErrorMessage("effectName", EFFECT_PROPERTIES_LAYOUT)
        );
        Objects.requireNonNull(
                mainBox,
                FXResourceLoader.getInjectionErrorMessage("mainBox", EFFECT_PROPERTIES_LAYOUT)
        );
        final List<Field> properties = FieldUtils.getAllFieldsList(effect.getClass()).stream()
                .filter(f -> Property.class.isAssignableFrom(f.getType())).collect(Collectors.toList());
        if (!properties.isEmpty()) {
            this.parseProperties(properties);
            if (!this.dynamicNodes.isEmpty()) {
                this.dynamicNodes.forEach((key, value) -> {
                    final VBox row = new VBox(key, value);
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
        this.backToEffects.setOnAction(e -> this.stack.toggle(thisDrawer));
        this.effectName.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                final Object source = click.getSource();
                final Label label;
                if (source instanceof Label) {
                    label = (Label) source;
                } else {
                    throw new IllegalStateException("EventHandler for label rename not associated to a label");
                }
                final TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle(ResourceLoader.getStringRes("rename_effect_dialog_title"));
                dialog.setHeaderText(ResourceLoader.getStringRes("rename_effect_dialog_msg"));
                dialog.setContentText(null);
                ((Stage) dialog.getDialogPane()
                        .getScene()
                        .getWindow())
                        .getIcons()
                        .add(SVGImages.getSvgImage(SVGImages.DEFAULT_ALCHEMIST_ICON_PATH));
                dialog.showAndWait().ifPresent(s -> Platform.runLater(() -> label.setText(s)));
            }
        });
        this.topBar.widthProperty().addListener((observable, oldValue, newValue) ->
                this.effectName.setPrefWidth(newValue.doubleValue())
        );
    }

    /**
     * Checks every field in the list for compatible {@link Property Properties} and
     * creates a new GUI component for each one.
     *
     * @param fields the list of fields
     */
    private void parseProperties(final List<Field> fields) {
        for (final Field f : fields) {
            final boolean isAccessible = f.canAccess(this.effect);
            try {
                f.setAccessible(true); // NOPMD TODO: This should get fixed in the future.
                if (RangedDoubleProperty.class.isAssignableFrom(f.getType())) {
                    buildSpinner((RangedDoubleProperty) f.get(this.effect));
                } else if (RangedIntegerProperty.class.isAssignableFrom(f.getType())) {
                    buildSlider((RangedIntegerProperty) f.get(this.effect));
                } else if (SerializableStringProperty.class.isAssignableFrom(f.getType())) {
                    buildTextField((StringProperty) f.get(this.effect));
                } else if (SerializableBooleanProperty.class.isAssignableFrom(f.getType())) {
                    buildCheckBox((BooleanProperty) f.get(this.effect));
                } else if (SerializableEnumProperty.class.isAssignableFrom(f.getType())) {
                    final SerializableEnumProperty<?> enumProperty = (SerializableEnumProperty<?>) f.get(this.effect);
                    if (enumProperty.get().getDeclaringClass().isEnum()) {
                        this.buildComboBox(enumProperty);
                    }
                }
            } catch (final IllegalArgumentException | IllegalAccessException e) {
                L.error(e.getMessage());
            } finally {
                f.setAccessible(isAccessible); // NOPMD TODO: This should get fixed in the future.
            }
        }
    }

    /**
     * Builds a new {@link ComboBox} from a {@link SerializableEnumProperty}, binds its
     * {@link ComboBox#valueProperty() valueProperty} to the
     * {@code EnumProperty} and adds it to internal list of nodes.
     *
     * @param enumProperty the model of the spinner
     * @param <T>          the enum type wrapped by the enumProperty
     */
    private <T extends Enum<T>> void buildComboBox(final SerializableEnumProperty<T> enumProperty) {
        final ObservableList<T> list = FXCollections.observableArrayList(enumProperty.values());
        final ComboBox<T> comboBox = new ComboBox<>(list);
        comboBox.setValue(list.get(0));
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> enumProperty.setValue(newValue));
        this.dynamicNodes.put(buildLabel(enumProperty), comboBox);
    }

    /**
     * Builds a new {@link CheckBox} from a {@link BooleanProperty}, binds its
     * {@link CheckBox#selectedProperty() selectedProperty} to the
     * {@code BooleanProperty} and adds it to internal list of nodes.
     *
     * @param booleanProperty the model of the checkBox
     */
    private void buildCheckBox(final BooleanProperty booleanProperty) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(booleanProperty.get());
        checkBox.selectedProperty().bindBidirectional(booleanProperty);
        this.dynamicNodes.put(buildLabel(booleanProperty), checkBox);
    }

    /**
     * Builds a new {@link javafx.scene.control.Slider} from a {@link RangedIntegerProperty}, binds
     * its {@link javafx.scene.control.Slider#valueProperty() valueProperty} to the
     * {@code RangedIntegerProperty} and adds it to internal list of nodes.
     *
     * @param integerProperty the model of the spinner
     */
    private void buildSlider(final RangedIntegerProperty integerProperty) {
        final JFXSlider slider = new JFXSlider();
        slider.setValue(integerProperty.get());
        slider.setMin(integerProperty.getLowerBound());
        slider.setMax(integerProperty.getUpperBound());
        slider.valueProperty().bindBidirectional(integerProperty);
        this.dynamicNodes.put(buildLabel(integerProperty), slider);
    }

    /**
     * Builds a new {@link Spinner} from a {@link RangedDoubleProperty}, binds
     * its {@link Spinner#valueProperty() valueProperty} to the
     * {@code RangedDoubleProperty} and adds it to internal list of nodes.
     *
     * @param doubleProperty the model of the spinner
     */
    private void buildSpinner(final RangedDoubleProperty doubleProperty) {
        final SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                doubleProperty.getLowerBound(),
                doubleProperty.getUpperBound(),
                doubleProperty.get(),
                0.01
        );
        final Spinner<Double> spinner = new Spinner<>(factory);
        spinner.setEditable(true);
        final TextFormatter<Double> formatter = new TextFormatter<>(factory.getConverter(), factory.getValue());
        spinner.getEditor().setTextFormatter(formatter);
        factory.valueProperty().bindBidirectional(formatter.valueProperty());
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> doubleProperty.set(newValue));
        this.dynamicNodes.put(buildLabel(doubleProperty), spinner);
    }

    /**
     * Builds a new {@link TextField} from a {@link StringProperty}, binds its
     * {@link TextField#textProperty() textProperty} to the
     * {@code StringProperty} and adds it to internal list of nodes.
     *
     * @param stringProperty the model of the spinner
     */
    private void buildTextField(final StringProperty stringProperty) {
        final TextField textField = new TextField(stringProperty.get());
        textField.textProperty().bindBidirectional(stringProperty);
        this.dynamicNodes.put(buildLabel(stringProperty), textField);
    }

    /**
     * Builds a new {@link Label} from a String.
     *
     * @param name the name
     * @return the {@code Label} with the name
     * @see #buildLabel(Property)
     */
    private Label buildLabel(final String name) {
        final Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold;");
        return nameLabel;
    }

    /**
     * Builds a new {@link Label} from the name of a JavaFX {@link Property}.
     *
     * @param namedProperty the {@code Property} to extract name from
     * @return the {@code Label} with the name
     * @see #buildLabel(String)
     */
    private Label buildLabel(final Property<?> namedProperty) {
        return buildLabel(namedProperty.getName());
    }

    /**
     * Shows a {@link Label} that tell the user that there is nothing to tune in that effect.
     */
    private void showNothing() {
        Objects.requireNonNull(
                this.mainBox,
                FXResourceLoader.getInjectionErrorMessage("mainBox", EFFECT_PROPERTIES_LAYOUT)
        );
        L.debug("Effect " + effect + " does not have tunable properties");
        final Label nothingHere = new Label(ResourceLoader.getStringRes("nothing_to_tune"));
        nothingHere.setTextAlignment(TextAlignment.CENTER);
        mainBox.getChildren().add(nothingHere);
    }

    /**
     * The JavaFX {@link Property} that wraps the {@link EffectFX effect} {@link EffectFX#getName() name}.
     *
     * @return the property
     */
    public StringProperty effectNameProperty() {
        Objects.requireNonNull(
                this.effectName,
                FXResourceLoader.getInjectionErrorMessage("effectName", EFFECT_PROPERTIES_LAYOUT)
        );
        return this.effectName.textProperty();
    }
}
