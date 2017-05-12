package it.unibo.alchemist.boundary.gui.view;

import com.jfoenix.controls.JFXSlider;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
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
     * Default constructor.
     * 
     * @param groupName
     *            the name of the EffectGroup
     */
    public EffectGroupCell(final String groupName) {
        super(new Label(groupName != null ? groupName : ""), new JFXSlider(0, 100, 100));
        // this.groupName = (Label) super.getInjectedNodeAt(0);
    }

    @Override
    public DataFormat getDataFormat() {
        return new DataFormat(EffectGroup.class.getName());
    }
}
