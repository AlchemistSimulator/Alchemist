package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;

/**
 * This ListView cell implements the {@link AbstractEffectCell} for containing
 * an {@link Effect}. It has a name that identifies the Effect and when clicked
 * should open another view to edit effect-specific parameters.
 */
public class EffectCell extends AbstractEffectCell<Effect> {
    // private Label effectName;

    /**
     * Default constructor.
     * 
     * @param effectName
     *            the name of the effect
     */
    public EffectCell(final String effectName) {
        super(new Label(effectName));
        // this.effectName = (Label) super.getInjectedNodeAt(0);
    }

    @Override
    public DataFormat getDataFormat() {
        return new DataFormat(Effect.class.getName());
    }

}
