package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;

public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {

    private Label groupName;

    public EffectGroupCell() {
        this(null);
    }

    public EffectGroupCell(final String groupName) {
        super(new Label(groupName != null ? groupName : ""));
        this.groupName = (Label) super.getNodeAt(0);
    }

    @Override
    public DataFormat getDataFormat() {
        // TODO Auto-generated method stub
        return null;
    }

}
