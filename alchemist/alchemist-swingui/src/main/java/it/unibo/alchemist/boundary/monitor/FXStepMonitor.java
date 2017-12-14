package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.monitor.generic.NumericLabelMonitor;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * {@code OutputMonitor} that monitors the current {@link Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class FXStepMonitor<T> extends NumericLabelMonitor<Long, T> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public FXStepMonitor() {
        super(0L, "Step: ");
    }

    @Override
    public void finished(final Environment<T> environment, final Time time, final long step) {
        update(step);
    }

    @Override
    public void stepDone(final Environment<T> environment, final Reaction<T> reaction, final Time time, final long step) {
        update(step);
    }
}
