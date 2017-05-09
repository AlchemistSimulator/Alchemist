package it.unibo.alchemist.boundary.gui.view;

import it.unibo.alchemist.boundary.gui.effects.EffectGroup;
import javafx.scene.control.Label;

public class EffectGroupCell extends AbstractEffectCell<EffectGroup> {

    public class EffectCell extends AbstractEffectCell<EffectGroup> {
        private Label effectName;

        public EffectCell() {
            this(null);
        }

        public EffectCell(final String effectName) {
            super(new Label(effectName != null ? effectName : ""));
            this.effectName = (Label) super.getNodeAt(DEFAULT_OFFSET);
        }

    }
}
