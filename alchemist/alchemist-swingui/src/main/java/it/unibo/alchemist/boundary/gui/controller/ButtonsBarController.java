package it.unibo.alchemist.boundary.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

/**
 * This class models a JavaFX controller for ButtonsBarLayout.fxml.
 */
public class ButtonsBarController implements Initializable {
    /** Layout path. */
    public static final String BUTTONS_BAR_LAYOUT = "ButtonsBarLayout";

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
    private HBox progressBox; // Value injected by FXMLLoader
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

    // Icons
    private final IconNode play;
    private final IconNode pause;
    private final IconNode pan;
    private final IconNode select;
    private final IconNode fullscreen;

    private PopOver controlTypePopOver;
    private ControlTypePopoverController controlTypePopoverController;

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

        effectsButton.setOnAction(e -> {
            // TODO show effects popup
        });

        fullscreenToggle.setText("");
        fullscreenToggle.setGraphic(fullscreen);

        controlType.setText("");
        controlType.setGraphic(pan);

        controlTypePopoverController = new ControlTypePopoverController(e -> {
            this.controlTypePopOver.hide();
            this.controlType.setGraphic(pan);
            // TODO change control type to pan mode
        }, e -> {
            this.controlTypePopOver.hide();
            this.controlType.setGraphic(select);
            // TODO change control type to select mode
        });

        controlTypePopOver = new PopOver();
        controlTypePopOver.setDetachable(false);
        controlTypePopOver.setDetached(false);
        controlTypePopOver.setHeaderAlwaysVisible(false);
        try {
            controlTypePopOver.setContentNode(FXResourceLoader.getLayout(AnchorPane.class, controlTypePopoverController,
                    ControlTypePopoverController.CONTROL_TYPE_POPOVER_LAYOUT));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // controlTypePopOver.setCornerRadius(0);
        controlTypePopOver.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
        controlType.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(final ActionEvent event) {

                if (controlTypePopOver.isShowing()) {
                    controlTypePopOver.hide();
                } else {
                    controlTypePopOver.show(controlType);
                }
            }
        });
    }

}
