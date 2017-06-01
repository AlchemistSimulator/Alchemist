package it.unibo.alchemist.boundary.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawersStack;
import com.jfoenix.controls.JFXSlider;

import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

/**
 * This class models a JavaFX controller for ButtonsBarLayout.fxml.
 */
public class ButtonsBarController implements Initializable {
    /** Layout path. */
    public static final String BUTTONS_BAR_LAYOUT = "ButtonsBarLayout";
    private static final double DEFAULT_DRAWER_FRACTION = 4;

    // FXML components
    @FXML
    private BorderPane controlPane; // Value injected by FXMLLoader
    @FXML
    private ButtonBar controlBar; // Value injected by FXMLLoader
    @FXML
    private JFXButton effectsButton; // Value injected by FXMLLoader
    @FXML
    private JFXButton startStopButton; // Value injected by FXMLLoader
    @FXML
    private Label timeLabel; // Value injected by FXMLLoader
    @FXML
    private Label stepLabel; // Value injected by FXMLLoader
    @FXML
    private JFXSlider speedSlider; // Value injected by FXMLLoader
    @FXML
    private JFXButton controlType; // Value injected by FXMLLoader
    @FXML
    private JFXButton fullscreenToggle; // Value injected by FXMLLoader
    @FXML
    private JFXDrawersStack drawerStack; // Value injected by FXMLLoader

    // Icons
    private final IconNode play;
    private final IconNode pause;
    private final IconNode pan;
    private final IconNode select;
    private final IconNode fullscreen;

    /**
     * Default constructor.
     */
    public ButtonsBarController() {
        super();

        play = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PLAY_ARROW);
        pause = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAUSE);
        pan = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL);
        select = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED);
        fullscreen = FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FULLSCREEN);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        assert controlPane != null : FXResourceLoader.getInjectionErrorMessage("controlPane", BUTTONS_BAR_LAYOUT);
        assert controlBar != null : FXResourceLoader.getInjectionErrorMessage("controlBar", BUTTONS_BAR_LAYOUT);
        assert effectsButton != null : FXResourceLoader.getInjectionErrorMessage("effectsButton", BUTTONS_BAR_LAYOUT);
        assert startStopButton != null : FXResourceLoader.getInjectionErrorMessage("startStopButton", BUTTONS_BAR_LAYOUT);
        assert timeLabel != null : FXResourceLoader.getInjectionErrorMessage("timeLabel", BUTTONS_BAR_LAYOUT);
        assert stepLabel != null : FXResourceLoader.getInjectionErrorMessage("stepLabel", BUTTONS_BAR_LAYOUT);
        assert speedSlider != null : FXResourceLoader.getInjectionErrorMessage("speedSlider", BUTTONS_BAR_LAYOUT);
        assert controlType != null : FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT);
        assert fullscreenToggle != null : FXResourceLoader.getInjectionErrorMessage("fullscreenToggle", BUTTONS_BAR_LAYOUT);
        assert drawerStack != null : FXResourceLoader.getInjectionErrorMessage("drawerStack", BUTTONS_BAR_LAYOUT);

        startStopButton.setText("");
        startStopButton.setGraphic(play);
        startStopButton.setOnAction(e -> {
            if (startStopButton.getGraphic().equals(play)) {
                startStopButton.setGraphic(pause);
                // TODO start the simulation
            } else {
                startStopButton.setGraphic(play);
                // TODO stop the simulation
            }
        });

        final JFXDrawer effectGroupsDrawer = new JFXDrawer();
        final EffectsGroupBarController effectsGroupBarController = new EffectsGroupBarController(this.drawerStack);
        effectGroupsDrawer.setDirection(JFXDrawer.DrawerDirection.LEFT);
        try {
            effectGroupsDrawer.setSidePane(FXResourceLoader.getLayout(BorderPane.class, effectsGroupBarController,
                    EffectsGroupBarController.EFFECT_GROUP_BAR_LAYOUT));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize side pane for effects", e);
        }
        effectGroupsDrawer.setOverLayVisible(false);
        effectGroupsDrawer.setResizableOnDrag(false);

        effectsButton.setOnAction(e -> {
            // Drawer size is modified every time it's opened
            if (effectGroupsDrawer.isHidden() || effectGroupsDrawer.isHidding()) {
                effectGroupsDrawer.setDefaultDrawerSize(controlPane.getWidth() / DEFAULT_DRAWER_FRACTION);
            }
            this.drawerStack.toggle(effectGroupsDrawer);
            if (effectGroupsDrawer.isShown() || effectGroupsDrawer.isShowing()) {
                this.drawerStack.setContent(new JFXDrawer());
            }
        });

        fullscreenToggle.setText("");
        fullscreenToggle.setGraphic(fullscreen);

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
        } catch (IOException e) {
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

}
