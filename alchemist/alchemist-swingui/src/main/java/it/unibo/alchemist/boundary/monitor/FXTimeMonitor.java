package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.scene.control.Label;

public class FXTimeMonitor<T> extends Label implements OutputMonitor<T> {
    // TODO

    public FXTimeMonitor() {
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
