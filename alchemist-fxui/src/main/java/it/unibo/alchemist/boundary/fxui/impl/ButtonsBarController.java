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
import it.unibo.alchemist.boundary.fxui.effects.api.EffectGroup;
import it.unibo.alchemist.boundary.fxui.util.FXResourceLoader;
import it.unibo.alchemist.boundary.fxui.monitors.api.FXOutputMonitor;
import it.unibo.alchemist.model.Position2D;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import tornadofx.FX;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class models a JavaFX controller for ButtonsBarLayout.fxml.
 *
 * @param <P> the position type
 */
@SuppressFBWarnings(
        value = { "NP_NULL_ON_SOME_PATH", "EI_EXPOSE_REP2" },
        justification = "Null checks are performed in the initialize function"
)
public class ButtonsBarController<P extends Position2D<? extends P>> implements Initializable {
    /**
     * Layout path.
     */
    public static final String BUTTONS_BAR_LAYOUT = "ButtonsBarLayout";
    private static final double DEFAULT_DRAWER_FRACTION = 4;
    // Icons
    private final IconNode pan;
    private final IconNode select;
    // FXML components
    @FXML
    @Nullable
    private BorderPane controlPane; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private ButtonBar controlBar; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton effectsButton; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton keysButton; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXSlider framerateSlider; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton controlType; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXDrawersStack drawerStack; // Value injected by FXMLLoader
    // Other
    @Nullable
    private Button startStopButton;
    @Nullable
    private Label timeLabel;
    @Nullable
    private Label stepLabel;
    private EffectsGroupBarController<P> effectsGroupBarController;
    private Optional<PopOver> controlTypePopOver = Optional.empty();
    private Optional<FXOutputMonitor<?, ?>> displayMonitor = Optional.empty();

    /**
     * Default constructor.
     */
    public ButtonsBarController() {
        super();
        pan = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL);
        select = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify an
     *      {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor} to display the effects.
     * <p>
     * Useful to pass to {@link EffectsGroupBarController}, {@link EffectBarController}
     * and {@link EffectPropertiesController}.
     *
     * @param displayMonitor the graphical {@code OutputMonitor}
     */
    public ButtonsBarController(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this();
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify the play/pause
     * {@link Button}, a {@link Label} for the steps and a {@link Label} for the time.
     *
     * @param playPauseButton the play/pause {@code Button}; should probably
     *                        be a {@link it.unibo.alchemist.boundary.fxui.monitors.impl.PlayPauseMonitor}
     * @param timeLabel       the {@code Label} for the steps; should probably
     *                        be a {@link AbstractNumericLabelMonitor}
     * @param stepLabel       the {@code Label} for the time; should probably
     *                        be a {@link AbstractNumericLabelMonitor}
     */
    public ButtonsBarController(final Button playPauseButton, final Label timeLabel, final Label stepLabel) {
        this();
        setStartStopButton(playPauseButton);
        setTimeMonitor(timeLabel);
        setStepMonitor(stepLabel);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify
     * an {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor} to display the effects,
     * the play/pause {@link Button}, a {@link Label} for the steps and a {@link Label} for the time.
     * <p>
     * Useful to pass to {@link EffectsGroupBarController}, {@link EffectBarController}
     * and {@link EffectPropertiesController}.
     *
     * @param displayMonitor  the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}
     * @param playPauseButton the play/pause {@code Button}; should probably
     *                        be a {@link it.unibo.alchemist.boundary.fxui.monitors.impl.PlayPauseMonitor}
     * @param timeLabel       the {@code Label} for the steps; should probably
     *                        be a {@link AbstractNumericLabelMonitor}
     * @param stepLabel       the {@code Label} for the time; should probably
     *                        be a {@link AbstractNumericLabelMonitor}
     */
    public ButtonsBarController(
            final @Nullable FXOutputMonitor<?, ?> displayMonitor,
            final Button playPauseButton,
            final Label timeLabel,
            final Label stepLabel
    ) {
        this(playPauseButton, timeLabel, stepLabel);
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Getter method for the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}.
     *
     * @return the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}, if any
     */
    public final Optional<FXOutputMonitor<?, ?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link it.unibo.alchemist.boundary.interfaces.OutputMonitor}
     *                       to set; if null, it will be {@link Optional#empty() unset}
     */
    public final void setDisplayMonitor(final @Nullable FXOutputMonitor<?, ?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        Objects.requireNonNull(
                controlPane,
                FXResourceLoader.getInjectionErrorMessage("controlPane", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                controlBar,
                FXResourceLoader.getInjectionErrorMessage("controlBar", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                effectsButton,
                FXResourceLoader.getInjectionErrorMessage("effectsButton", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                keysButton,
                FXResourceLoader.getInjectionErrorMessage("keysButton", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                framerateSlider,
                FXResourceLoader.getInjectionErrorMessage("framerateSlider", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                controlType,
                FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT)
        );
        Objects.requireNonNull(
                drawerStack,
                FXResourceLoader.getInjectionErrorMessage("drawerStack", BUTTONS_BAR_LAYOUT)
        );
        addMonitors();
        final JFXDrawer effectGroupsDrawer = new JFXDrawer();
        effectsGroupBarController = new EffectsGroupBarController<>(
                getDisplayMonitor().orElse(null),
                this.drawerStack
        );
        effectGroupsDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);
        try {
            effectGroupsDrawer.setSidePane(FXResourceLoader.getLayout(effectsGroupBarController,
                    EffectsGroupBarController.EFFECT_GROUP_BAR_LAYOUT));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not initialize side pane for effects", e);
        }
        effectGroupsDrawer.setOverLayVisible(false);
        effectGroupsDrawer.setResizableOnDrag(false);
        effectsButton.setOnAction(e -> {
            if (effectGroupsDrawer.isOpened() || effectGroupsDrawer.isOpening()) {
                this.drawerStack.setContent(new JFXDrawer());
                this.drawerStack.toggle(effectGroupsDrawer, false);
                this.drawerStack.setMouseTransparent(true);
            } else {
                effectGroupsDrawer.setDefaultDrawerSize(controlPane.getWidth() / DEFAULT_DRAWER_FRACTION);
                this.drawerStack.toggle(effectGroupsDrawer, true);
                this.drawerStack.setMouseTransparent(false);
            }
        });
        keysButton.setOnAction(e -> FX.find(ListKeybindsView.class).openModal());
        controlType.setText("");
        controlType.setGraphic(pan);
        controlType.setOnAction(event -> togglePopover());
    }

    /**
     * Toggles the {@link PopOver} to change mouse interaction type.
     */
    private void togglePopover() {
        if (controlTypePopOver.isPresent() && controlTypePopOver.get().isShowing()) {
            controlTypePopOver.get().hide();
        } else {
            Objects.requireNonNull(
                    controlType,
                    FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT)
            );
            final ControlTypePopoverController controlTypePopoverController = new ControlTypePopoverController();
            final Node popoverContent;
            try {
                popoverContent = FXResourceLoader.getLayout(controlTypePopoverController,
                        ControlTypePopoverController.CONTROL_TYPE_POPOVER_LAYOUT);
            } catch (final IOException e) {
                throw new IllegalStateException("Could not initialize popover for control type change", e);
            }
            final PopOver pop = new PopOver(popoverContent);
            pop.setAutoHide(true);
            pop.setDetachable(false);
            pop.setDetached(false);
            pop.setHeaderAlwaysVisible(false);
            pop.getRoot().getStylesheets().add(FXResourceLoader.getStyle("popover"));
            pop.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
            Optional.ofNullable(controlTypePopoverController.getSelectButton()).ifPresent(b -> b.setOnAction(e -> {
                Platform.runLater(() -> this.controlType.setGraphic(select));
                this.displayMonitor.ifPresent(d -> d.setViewStatus(FXOutputMonitor.ViewStatus.SELECTING));
            }));
            Optional.ofNullable(controlTypePopoverController.getPanButton()).ifPresent(b -> b.setOnAction(e -> {
                Platform.runLater(() -> this.controlType.setGraphic(pan));
                this.displayMonitor.ifPresent(d -> d.setViewStatus(FXOutputMonitor.ViewStatus.PANNING));
            }));
            controlTypePopOver = Optional.of(pop);
            controlTypePopOver.get().show(controlType);
        }
    }

    /**
     * Sets the play/pause toggle.
     *
     * @param button the play/pause toggle button
     */
    public final void setStartStopButton(final Button button) {
        this.startStopButton = button;
        addMonitors();
    }

    /**
     * Sets the time monitor label.
     *
     * @param timeMonitor the time monitor label
     */
    public final void setTimeMonitor(final Label timeMonitor) {
        this.timeLabel = timeMonitor;
        addMonitors();
    }

    /**
     * Adds all the monitors to the {@link ButtonBar}.
     */
    private void addMonitors() {
        if (this.controlBar != null) {
            final ObservableList<Node> buttons = this.controlBar.getButtons();
            if (stepLabel != null && !buttons.contains(stepLabel)) {
                ButtonBar.setButtonData(stepLabel, ButtonBar.ButtonData.RIGHT);
                buttons.add(stepLabel);
            }
            if (timeLabel != null && !buttons.contains(timeLabel)) {
                ButtonBar.setButtonData(timeLabel, ButtonBar.ButtonData.RIGHT);
                buttons.add(timeLabel);
            }
            if (startStopButton != null && !buttons.contains(startStopButton)) {
                ButtonBar.setButtonData(startStopButton, ButtonBar.ButtonData.RIGHT);
                buttons.add(startStopButton);
            }
        }
    }

    /**
     * Sets the step monitor label.
     *
     * @param stepMonitor the step monitor label
     */
    public final void setStepMonitor(final Label stepMonitor) {
        this.stepLabel = stepMonitor;
        addMonitors();
    }

    /**
     * Getter method for the {@code List} of groups of
     * {@link it.unibo.alchemist.boundary.fxui.effects.api.EffectFX effects} in the side drawer.
     *
     * @return an {@code ObservableList} of {@code EffectGroup}
     */
    public ObservableList<EffectGroup<P>> getObservableEffectsList() {
        return this.effectsGroupBarController.getObservableEffectsList();
    }
}
