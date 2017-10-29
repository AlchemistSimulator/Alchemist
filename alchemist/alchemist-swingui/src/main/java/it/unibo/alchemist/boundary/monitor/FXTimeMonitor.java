package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * {@code OutputMonitor} that monitors the current {@link Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> the {@link Concentration} type
 */
public class FXTimeMonitor<T> extends NumericLabelMonitor<Time, T> {
    private volatile boolean mayRender = true;

    /**
     * Default constructor.
     */
    public FXTimeMonitor() {
        super(DoubleTime.ZERO_TIME);
        setName("Time: ");
    }

    @Override
    public void finished(final Environment<T> env, final Time time, final long step) {
        update(time);
    }

    @Override
    public void stepDone(final Environment<T> env, final Reaction<T> r, final Time time, final long step) {
        update(time);
    }

}
