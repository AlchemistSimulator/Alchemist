package it.unibo.alchemist.boundary.gui.view;

import java.io.IOException;

import org.controlsfx.control.PopOver;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.controller.EffectBarController;
import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import it.unibo.alchemist.boundary.gui.utility.FXResourceLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectGroup}. It has a name that identifies the EffectGroup and
 * when clicked should open a {@link ListView} to show the {@link Effect}s the
 * group is composed of.
 */
public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {
    private static final String DEFAULT_NAME = "Unnamed effect group";

    /**
     * Default constructor.
     */
    public EffectGroupCell() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor.
     * 
     * @param groupName
     *            the name of the EffectGroup
     */
    public EffectGroupCell(final String groupName) {
        super(new Label(groupName), new JFXToggleButton(), new JFXSlider(0, 100, 100));
        this.getLabel().setTextAlignment(TextAlignment.CENTER);
        this.getLabel().setFont(Font.font(this.getLabel().getFont().getFamily(), FontWeight.BOLD, this.getLabel().getFont().getSize()));

        this.getLabel().textProperty().addListener((observable, oldValue, newValue) -> {
            this.getItem().setName(newValue);
        });
        this.getSlider().valueProperty().addListener((observable, oldValue, newValue) -> {
            this.getItem().setTransparency(newValue.intValue());
        });
        this.getToggle().selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.getItem().setVisibility(newValue);
        });

        this.getLabel().setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                final Object source = click.getSource();
                final Label label;

                if (source instanceof Label) {
                    label = (Label) source;
                } else {
                    throw new IllegalStateException("EventHandler for label rename not associated to a label");
                }

                final TextInputDialog dialog = new TextInputDialog(label.getText());
                dialog.setTitle("Rename the EffectGroup");
                dialog.setHeaderText("Please enter new name:");
                dialog.setContentText(null);

                dialog.showAndWait().ifPresent(name -> {
                    label.setText(name);
                    // this.getItem().setName(name);
                    // TODO ^ Should be unnecessary, check
                });
            }
        });

        final PopOver effectPopOver = new PopOver();
        effectPopOver.setDetachable(/*false*/ true); // TODO check
        effectPopOver.setHeaderAlwaysVisible(true);
        effectPopOver.titleProperty().bindBidirectional(this.getLabel().textProperty());
        final EffectBarController effectPopoverController = new EffectBarController();
        try {
            effectPopOver.setContentNode(FXResourceLoader.getLayout(BorderPane.class, effectPopoverController,
                    EffectBarController.EFFECT_BAR_LAYOUT));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize popover for effect " + getItem().getName(), e);
        }
        // effectPopOver.setArrowLocation(ArrowLocation.LEFT_TOP); // TODO check
        this.setOnMouseClicked(event -> {
            if (effectPopOver.isShowing()) {
                effectPopOver.hide();
            } else {
                effectPopOver.show(EffectGroupCell.this);
                effectPopOver.setDetached(/*false*/ true); // TODO check
            }
        });
    }

    /**
     * Returns the label with the effect name.
     * 
     * @return the label
     */
    protected Label getLabel() {
        return (Label) super.getInjectedNodeAt(0);
    }

    /**
     * Returns the slider of the transparency.
     * 
     * @return the slider
     */
    protected JFXSlider getSlider() {
        return (JFXSlider) super.getInjectedNodeAt(2);
    }

    /**
     * Returns the toggle of the visibility.
     * 
     * @return the toggle
     */
    protected JFXToggleButton getToggle() {
        return (JFXToggleButton) super.getInjectedNodeAt(1);
    }

    @Override
    public DataFormat getDataFormat() {
        final EffectGroup item = this.getItem();

        if (item == null) {
            return EffectGroup.DATA_FORMAT;
        } else {
            return item.getDataFormat();
        }
    }

    @Override
    protected void updateItem(final EffectGroup item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            this.getLabel().setText(item.getName());
            this.getSlider().setValue(item.getTransparency());
            this.getToggle().setSelected(item.isVisible());
        }
    }
}
