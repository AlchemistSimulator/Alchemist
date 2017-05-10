package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;

public class EffectCell extends AbstractEffectCell<Effect> {
    private Label effectName;

    public EffectCell() {
        this(null);
    }

    public EffectCell(final String effectName) {
        super(new Label(effectName != null ? effectName : ""));
        this.effectName = (Label) super.getNodeAt(DEFAULT_OFFSET);

    }

    @Override
    public DataFormat getDataFormat() {
        // TODO Auto-generated method stub
        return null;
    }

}
