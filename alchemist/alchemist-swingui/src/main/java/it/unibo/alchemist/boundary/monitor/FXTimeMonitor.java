package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.monitor.generic.NumericLabelMonitor;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * {@code OutputMonitor} that monitors the current {@link it.unibo.alchemist.core.interfaces.Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> The type which describes the {@link it.unibo.alchemist.model.interfaces.Concentration} of a molecule
 * @param <P> The position type
 */
public class FXTimeMonitor<T, P extends Position<? extends P>> extends NumericLabelMonitor<Time, T, P> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Default constructor.
     */
    public FXTimeMonitor() {
        super(DoubleTime.ZERO_TIME, "Time: ");
    }

    /**
     * @inheritDocs
     */
    @Override
    public void finished(final Environment<T, P> environment, final Time time, final long step) {
        update(time);
    }

    /**
     * @inheritDocs
     */
    @Override
    public void stepDone(final Environment<T, P> environment, final Reaction<T> reaction, final Time time, final long step) {
        update(time);
    }
}
