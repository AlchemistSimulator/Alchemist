package it.unibo.alchemist.boundary.gui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;

import it.unibo.alchemist.boundary.gui.FXResourceLoader;
import it.unibo.alchemist.boundary.gui.NoLayoutSpecifiedException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

public class ButtonsBarController implements Initializable {
    private static final String BUTTONS_BAR_LAYOUT = "ButtonsBarLayout.fxml";

    // FXML components
    @FXML
    private BorderPane controlPane; // Value injected by FXMLLoader
    @FXML
    private ButtonBar controlBar; // Value injected by FXMLLoader
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

    private PopOver controlTypePopOver;
    private ControlTypePopoverController controlTypePopoverController;
    private JFXButton panButton;
    private JFXButton selectButton;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        assert controlPane != null : FXResourceLoader.getInjectionErrorMessage("controlPane", BUTTONS_BAR_LAYOUT);
        assert controlBar != null : FXResourceLoader.getInjectionErrorMessage("controlBar", BUTTONS_BAR_LAYOUT);
        assert startStopButton != null : FXResourceLoader.getInjectionErrorMessage("startStopButton", BUTTONS_BAR_LAYOUT);
        assert timeLabel != null : FXResourceLoader.getInjectionErrorMessage("timeLabel", BUTTONS_BAR_LAYOUT);
        assert stepLabel != null : FXResourceLoader.getInjectionErrorMessage("stepLabel", BUTTONS_BAR_LAYOUT);
        assert speedSlider != null : FXResourceLoader.getInjectionErrorMessage("speedSlider", BUTTONS_BAR_LAYOUT);
        assert controlType != null : FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT);
        assert fullscreenToggle != null : FXResourceLoader.getInjectionErrorMessage("fullscreenToggle", BUTTONS_BAR_LAYOUT);

        startStopButton.setText("");
        startStopButton.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PLAY_ARROW));

        fullscreenToggle.setText("");
        fullscreenToggle.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.FULLSCREEN));

        controlType.setText("");
        controlType.setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL));

        controlTypePopoverController = new ControlTypePopoverController(e -> {
            getControlTypePopOver().hide();
            getControlTypeButton().setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.PAN_TOOL));
            // TODO change control type to pan mode
        }, e -> {
            getControlTypePopOver().hide();
            getControlTypeButton().setGraphic(FXResourceLoader.getWhiteIcon(GoogleMaterialDesignIcons.TAB_UNSELECTED));
            // TODO change control type to select mode
        });
        ButtonsBarController.this.panButton = controlTypePopoverController.getPanButton();
        ButtonsBarController.this.selectButton = controlTypePopoverController.getSelectButton();

        try {
            controlTypePopOver = new PopOver(new FXResourceLoader(FXResourceLoader.DefaultLayout.CONTROL_TYPE_POPOVER_LAYOUT.getName())
                    .getLayout(AnchorPane.class, controlTypePopoverController));
            controlTypePopOver.setArrowLocation(ArrowLocation.BOTTOM_CENTER);
        } catch (NoLayoutSpecifiedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(); // TODO at the time, I can't throw up the
                                 // exception
        }

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

    public JFXButton getControlTypeButton() {
        return this.controlType;
    }

    public PopOver getControlTypePopOver() {
        return this.controlTypePopOver;
    }

}
