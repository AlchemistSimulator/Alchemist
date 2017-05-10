package it.unibo.alchemist.boundary.gui.effects;

import javafx.collections.ObservableList;

/**
 * Models a group of effects.
 */
public interface EffectGroup {

    public Effect getEffect(final int position);

    public ObservableList<Effect> getEffects();

    public Effect popEffect();

    public Effect peekEffect();

    public void pushEffect(final Effect effect);

    public boolean isEmpty();

    public int search(final Effect effect);
}
