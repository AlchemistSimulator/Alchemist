package it.unibo.alchemist.boundary.gui.view;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import java8.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DataFormat;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link EffectGroup}. It has a name that identifies the EffectGroup and
 * when clicked should open a {@link ListView} to show the {@link Effect}s the
 * group is composed of.
 */
public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {
    // private Label groupName;

    /**
     * 
     */
    public EffectGroupCell() {
        this("Effect group without name");
    }

    /**
     * Default constructor.
     * 
     * @param groupName
     *            the name of the EffectGroup
     */
    public EffectGroupCell(final String groupName) {
        super(new Label(Objects.requireNonNull(groupName)), new JFXSlider(0, 100, 100));
        // this.groupName = (Label) super.getInjectedNodeAt(0);
    }

    @Override
    public DataFormat getDataFormat() {
        return new DataFormat(EffectGroup.class.getName());
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
        return (JFXSlider) super.getInjectedNodeAt(1);
    }

    /**
     * Returns the toggle of the visibility.
     * 
     * @return the toggle
     */
    protected JFXToggleButton getToggle() {
        return (JFXToggleButton) super.getNodeAt(DEFAULT_OFFSET + 2);
    }
}
