package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javafx.scene.control.Label;

public class FXStepMonitor<T> extends Label implements OutputMonitor<T> {
    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {

    }

    @Override
    public void initialized(final Environment<T> env) {

    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {

    }
}
