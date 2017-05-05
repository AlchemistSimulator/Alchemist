package it.unibo.alchemist.boundary.gui.view;

import javafx.scene.control.Label;

public class EffectGroupCell extends AbstractEffectCell {

    public class EffectCell extends AbstractEffectCell {
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
