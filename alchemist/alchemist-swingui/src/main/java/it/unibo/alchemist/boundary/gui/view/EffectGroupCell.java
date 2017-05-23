package it.unibo.alchemist.boundary.gui.view;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DataFormat;
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
    private static final String DEFAULT_NAME = "Effect group without name";

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
        /*
         * this.getLabel().setOnMouseClicked(click -> { if
         * (click.getClickCount() == 2) {
         * 
         * } });
         */
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
