package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.gui.effects.Effect;
import it.unibo.alchemist.boundary.gui.effects.EffectFX;
import it.unibo.alchemist.boundary.interfaces.Graphical2DOutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.util.List;

public class Fx2dDisplay<T> implements Graphical2DOutputMonitor<T> {
    /**
     * Initializes a new display with out redrawing the first step.
     */
    public Fx2dDisplay() {
        this(1);
    }

    /**
     * Initializes a new display.
     *
     * @param step number of steps to let pass without re-drawing
     */
    public Fx2dDisplay(final int step) {
        super();
        // TODO
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        // TODO
    }

    @Override
    public int getStep() {
        // TODO
        return 0;
    }

    @Override
    public void setStep(final int step) {
        // TODO
    }

    @Override
    public boolean isRealTime() {
        // TODO
        return false;
    }

    @Override
    public void setRealTime(final boolean rt) {
        // TODO
    }

    @Override
    public void repaint() {
        // TODO
    }

    @Override
    public void setDrawLinks(final boolean b) {
        // TODO
    }

    @Override
    public void setEffectStack(final List<Effect> l) { // TODO remove
        // TODO
    }

    public void setEffectFXStack(final List<EffectFX> l) { // TODO use instead of #setEffectStack
        // TODO
    }

    @Override
    public void setMarkCloserNode(final boolean mark) {
        // TODO
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        // TODO
    }

    @Override
    public void initialized(final Environment<T> env) {
        // TODO
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        // TODO
    }
}
