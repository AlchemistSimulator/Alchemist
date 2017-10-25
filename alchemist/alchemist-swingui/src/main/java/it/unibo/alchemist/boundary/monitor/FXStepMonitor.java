package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * {@code OutputMonitor} that monitors the current {@link Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> the {@link Concentration} type
 */
public class FXStepMonitor<T> extends NumericLabelMonitor<Long, T> {
    private volatile boolean mayRender = true;

    /**
     * Default constructor.
     */
    public FXStepMonitor() {
        super(0L);
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        update(step);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        update(step);
    }

}
