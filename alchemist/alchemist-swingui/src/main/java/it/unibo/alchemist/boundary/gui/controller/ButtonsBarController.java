package it.unibo.alchemist.boundary.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXSlider;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
//    private final IconNode fullscreen;

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
    private JFXSlider speedSlider; // Value injected by FXMLLoader
    @FXML
    @Nullable
    private JFXButton controlType; // Value injected by FXMLLoader
//    @FXML
//    @Nullable
//    private JFXButton fullscreenToggle; // Value injected by FXMLLoader
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

    /**
     * Default constructor.
     */
    public ButtonsBarController() {
        super();

        pan = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL);
        select = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED);
//        fullscreen = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FULLSCREEN);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert controlPane != null : FXResourceLoader.getInjectionErrorMessage("controlPane", BUTTONS_BAR_LAYOUT);
        assert controlBar != null : FXResourceLoader.getInjectionErrorMessage("controlBar", BUTTONS_BAR_LAYOUT);
        assert effectsButton != null : FXResourceLoader.getInjectionErrorMessage("effectsButton", BUTTONS_BAR_LAYOUT);
        assert speedSlider != null : FXResourceLoader.getInjectionErrorMessage("speedSlider", BUTTONS_BAR_LAYOUT);
        assert controlType != null : FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT);
//        assert fullscreenToggle != null : FXResourceLoader.getInjectionErrorMessage("fullscreenToggle", BUTTONS_BAR_LAYOUT);
        assert drawerStack != null : FXResourceLoader.getInjectionErrorMessage("drawerStack", BUTTONS_BAR_LAYOUT);

        addMonitors();

        final JFXDrawer effectGroupsDrawer = new JFXDrawer();
        effectsGroupBarController = new EffectsGroupBarController(this.drawerStack);
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

//        fullscreenToggle.setText("");
//        fullscreenToggle.setGraphic(fullscreen);
////        fullscreenToggle.setOnAction(e -> {
////            // TODO toggle fullscreen
////        });

        controlType.setText("");
        controlType.setGraphic(pan);

        final PopOver controlTypePopOver = new PopOver();
        controlTypePopOver.setDetachable(false);
        controlTypePopOver.setDetached(false);
        controlTypePopOver.setHeaderAlwaysVisible(false);
        final ControlTypePopoverController controlTypePopoverController = new ControlTypePopoverController(e -> {
            controlTypePopOver.hide();
            this.controlType.setGraphic(pan);
            // TODO change control type to pan mode
        }, e -> {
            controlTypePopOver.hide();
            this.controlType.setGraphic(select);
            // TODO change control type to select mode
        });
        try {
            controlTypePopOver.setContentNode(FXResourceLoader.getLayout(AnchorPane.class, controlTypePopoverController,
                    ControlTypePopoverController.CONTROL_TYPE_POPOVER_LAYOUT));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not initialize popover for control type change", e);
        }
        controlTypePopOver.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
        controlType.setOnAction(event -> {
            if (controlTypePopOver.isShowing()) {
                controlTypePopOver.hide();
            } else {
                controlTypePopOver.show(controlType);
            }
        });
    }

    /**
     * Sets the play/pause toggle.
     *
     * @param button the play/pause toggle button
     */
    public void setStartStopButton(final Button button) {
        this.startStopButton = button;

        if (this.controlBar != null && this.startStopButton != null) {
            addMonitors();
        }
    }

    /**
     * Sets the time monitor label.
     *
     * @param timeMonitor the time monitor label
     */
    public void setTimeMonitor(final Label timeMonitor) {
        this.timeLabel = timeMonitor;
        addMonitors();
    }

    /**
     * Adds all the monitors to the {@link ButtonBar}.
     */
    private void addMonitors() {
        if (this.controlBar != null) {
            final ObservableList<Node> buttons = this.controlBar.getButtons();

            if (startStopButton != null && !buttons.contains(startStopButton)) {
                ButtonBar.setButtonData(startStopButton, ButtonBar.ButtonData.LEFT);
                buttons.add(startStopButton);
            }

            if (timeLabel != null && !buttons.contains(timeLabel)) {
                ButtonBar.setButtonData(timeLabel, ButtonBar.ButtonData.LEFT);
                buttons.add(timeLabel);
            }

            if (stepLabel != null && !buttons.contains(stepLabel)) {
                ButtonBar.setButtonData(stepLabel, ButtonBar.ButtonData.LEFT);
                buttons.add(stepLabel);
            }
        }
    }

    /**
     * Sets the step monitor label.
     *
     * @param stepMonitor the step monitor label
     */
    public void setStepMonitor(final Label stepMonitor) {
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
