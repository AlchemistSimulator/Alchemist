package it.unibo.alchemist.boundary.gui.effects;

import java.util.Stack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EffectStack implements EffectGroup {
    private final Stack<Effect> effects = new Stack<>();

    @Override
    public Effect getEffect(final int position) {
        return this.effects.get(position);
    }

    @Override
    public ObservableList<Effect> getEffects() {
        return FXCollections.observableArrayList(effects);
    }

    @Override
    public Effect popEffect() {
        return this.effects.pop();
    }

    @Override
    public Effect peekEffect() {
        return this.effects.peek();
    }

    @Override
    public void pushEffect(final Effect effect) {
        this.effects.push(effect);
    }

    @Override
    public boolean isEmpty() {
        return effects.isEmpty();
    }

    @Override
    public int search(final Effect effect) {
        return effects.search(effect);
    }

}
