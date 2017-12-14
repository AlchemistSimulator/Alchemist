package it.unibo.alchemist.boundary.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXSlider;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import it.unibo.alchemist.boundary.interfaces.FXOutputMonitor;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.boundary.monitor.PlayPauseMonitor;
import it.unibo.alchemist.boundary.monitor.generic.NumericLabelMonitor;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.jetbrains.annotations.Nullable;

/**
 * This class models a JavaFX controller for ButtonsBarLayout.fxml.
 */
public class ButtonsBarController implements Initializable {
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
    private JFXSlider framerate; // Value injected by FXMLLoader
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

    private EffectsGroupBarController effectsGroupBarController;
    private Optional<PopOver> controlTypePopOver = Optional.empty();
    private Optional<FXOutputMonitor<?>> displayMonitor = Optional.empty();

    /**
     * Default constructor.
     */
    public ButtonsBarController() {
        super();

        pan = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL);
        select = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify an {@link OutputMonitor} to display the effects.
     * <p>
     * Useful to pass to {@link EffectsGroupBarController}, {@link EffectBarController} and {@link EffectPropertiesController}.
     *
     * @param displayMonitor the graphical {@code OutputMonitor}
     */
    public ButtonsBarController(final @Nullable FXOutputMonitor<?> displayMonitor) {
        this();
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify the play/pause {@link Button}, a {@link Label} for the steps and a {@link Label} for the time.
     *
     * @param playPauseButton the play/pause {@code Button}; should probably be a {@link PlayPauseMonitor}
     * @param timeLabel       the {@code Label} for the steps; should probably be a {@link NumericLabelMonitor}
     * @param stepLabel       the {@code Label} for the time; should probably be a {@link NumericLabelMonitor}
     */
    public ButtonsBarController(final Button playPauseButton, final Label timeLabel, final Label stepLabel) {
        this();
        setStartStopButton(playPauseButton);
        setTimeMonitor(timeLabel);
        setStepMonitor(stepLabel);
    }

    /**
     * Same as {@link #ButtonsBarController() default constructor}, but lets specify an {@link OutputMonitor} to display the effects, the play/pause {@link Button}, a {@link Label} for the steps and a {@link Label} for the time.
     * <p>
     * Useful to pass to {@link EffectsGroupBarController}, {@link EffectBarController} and {@link EffectPropertiesController}.
     *
     * @param displayMonitor  the graphical {@link OutputMonitor}
     * @param playPauseButton the play/pause {@code Button}; should probably be a {@link PlayPauseMonitor}
     * @param timeLabel       the {@code Label} for the steps; should probably be a {@link NumericLabelMonitor}
     * @param stepLabel       the {@code Label} for the time; should probably be a {@link NumericLabelMonitor}
     */
    public ButtonsBarController(final @Nullable FXOutputMonitor<?> displayMonitor, final Button playPauseButton, final Label timeLabel, final Label stepLabel) {
        this(playPauseButton, timeLabel, stepLabel);
        setDisplayMonitor(displayMonitor);
    }

    /**
     * Getter method for the graphical {@link OutputMonitor}.
     *
     * @return the graphical {@link OutputMonitor}, if any
     */
    public final Optional<FXOutputMonitor<?>> getDisplayMonitor() {
        return displayMonitor;
    }

    /**
     * Setter method for the graphical {@link OutputMonitor}.
     *
     * @param displayMonitor the graphical {@link OutputMonitor} to set; if null, it will be {@link Optional#empty() unset}
     */
    public final void setDisplayMonitor(final @Nullable FXOutputMonitor<?> displayMonitor) {
        this.displayMonitor = Optional.ofNullable(displayMonitor);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert controlPane != null : FXResourceLoader.getInjectionErrorMessage("controlPane", BUTTONS_BAR_LAYOUT);
        assert controlBar != null : FXResourceLoader.getInjectionErrorMessage("controlBar", BUTTONS_BAR_LAYOUT);
        assert effectsButton != null : FXResourceLoader.getInjectionErrorMessage("effectsButton", BUTTONS_BAR_LAYOUT);
        assert framerate != null : FXResourceLoader.getInjectionErrorMessage("speedSlider", BUTTONS_BAR_LAYOUT);
        assert controlType != null : FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT);
        assert drawerStack != null : FXResourceLoader.getInjectionErrorMessage("drawerStack", BUTTONS_BAR_LAYOUT);

        addMonitors();

        final JFXDrawer effectGroupsDrawer = new JFXDrawer();
        effectsGroupBarController = new EffectsGroupBarController(getDisplayMonitor().get(), this.drawerStack);
        effectGroupsDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);
        try {
            effectGroupsDrawer.setSidePane(FXResourceLoader.getLayout(BorderPane.class, effectsGroupBarController,
                    EffectsGroupBarController.EFFECT_GROUP_BAR_LAYOUT));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not initialize side pane for effects", e);
        }
        effectGroupsDrawer.setOverLayVisible(false);
        effectGroupsDrawer.setResizableOnDrag(false);

        effectsButton.setOnAction(e -> {
            // Drawer size is modified every time it's opened
            if (effectGroupsDrawer.isHidden() || effectGroupsDrawer.isHiding()) {
                effectGroupsDrawer.setDefaultDrawerSize(controlPane.getWidth() / DEFAULT_DRAWER_FRACTION);
            }
            this.drawerStack.toggle(effectGroupsDrawer);
            if (effectGroupsDrawer.isShown() || effectGroupsDrawer.isShowing()) {
                this.drawerStack.setContent(new JFXDrawer());
            }
        });

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
            assert controlType != null;

            final ControlTypePopoverController controlTypePopoverController = new ControlTypePopoverController();

            final Node popoverContent;
            try {
                popoverContent = FXResourceLoader.getLayout(AnchorPane.class, controlTypePopoverController,
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
                this.displayMonitor.ifPresent(d -> {
                    d.setViewStatus(FXOutputMonitor.ViewStatus.SELECTING);
                });
            }));
            Optional.ofNullable(controlTypePopoverController.getPanButton()).ifPresent(b -> b.setOnAction(e -> {
                Platform.runLater(() -> this.controlType.setGraphic(pan));
                this.displayMonitor.ifPresent(d -> {
                    d.setViewStatus(FXOutputMonitor.ViewStatus.PAN);
                });
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
     * Getter method for the {@code List} of groups of {@link EffectFX effects} in the side drawer.
     *
     * @return an {@code ObservableList} of {@code EffectGroup}
     */
    public ObservableList<EffectGroup> getObservableEffectsList() {
        return this.effectsGroupBarController.getObservableEffectsList();
    }
}
