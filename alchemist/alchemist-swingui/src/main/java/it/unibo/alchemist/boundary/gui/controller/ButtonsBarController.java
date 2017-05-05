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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

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

    // Icons
    private final IconNode play;
    private final IconNode pause;
    private final IconNode pan;
    private final IconNode select;
    private final IconNode fullscreen;

    private PopOver controlTypePopOver;
    private ControlTypePopoverController controlTypePopoverController;

    public ButtonsBarController() {
        super();
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

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
        assert startStopButton != null : FXResourceLoader.getInjectionErrorMessage("startStopButton", BUTTONS_BAR_LAYOUT);
        assert timeLabel != null : FXResourceLoader.getInjectionErrorMessage("timeLabel", BUTTONS_BAR_LAYOUT);
        assert stepLabel != null : FXResourceLoader.getInjectionErrorMessage("stepLabel", BUTTONS_BAR_LAYOUT);
        assert speedSlider != null : FXResourceLoader.getInjectionErrorMessage("speedSlider", BUTTONS_BAR_LAYOUT);
        assert controlType != null : FXResourceLoader.getInjectionErrorMessage("controlType", BUTTONS_BAR_LAYOUT);
        assert fullscreenToggle != null : FXResourceLoader.getInjectionErrorMessage("fullscreenToggle", BUTTONS_BAR_LAYOUT);

        startStopButton.setText("");
        startStopButton.setGraphic(play);
        startStopButton.setOnMouseClicked(e -> {
            if (startStopButton.getGraphic().equals(play)) {
                startStopButton.setGraphic(pause);
                // TODO start the simulation
            } else {
                startStopButton.setGraphic(play);
                // TODO stop the simulation
            }
        });

        fullscreenToggle.setText("");
        fullscreenToggle.setGraphic(fullscreen);

        controlType.setText("");
        controlType.setGraphic(pan);

        controlTypePopoverController = new ControlTypePopoverController(e -> {
            getControlTypePopOver().hide();
            getControlTypeButton().setGraphic(pan);
            // TODO change control type to pan mode
        }, e -> {
            getControlTypePopOver().hide();
            getControlTypeButton().setGraphic(select);
            // TODO change control type to select mode
        });

        try {
            controlTypePopOver = new PopOver();
            controlTypePopOver.setDetachable(false);
            controlTypePopOver.setDetached(false);
            controlTypePopOver.setHeaderAlwaysVisible(false);
            controlTypePopOver.setContentNode(new FXResourceLoader(FXResourceLoader.DefaultLayout.CONTROL_TYPE_POPOVER_LAYOUT.getName())
                    .getLayout(AnchorPane.class, controlTypePopoverController));
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

        } catch (NoLayoutSpecifiedException e1) {
            // TODO This should not happen
        } catch (IOException e1) {
            // TODO I can't throw up the exception from here, but I should
        }
    }

    private JFXButton getControlTypeButton() { // TODO Maybe the getter should be public
        return this.controlType;
    }

    private PopOver getControlTypePopOver() { // TODO Maybe the getter should be public
        return this.controlTypePopOver;
    }

}
