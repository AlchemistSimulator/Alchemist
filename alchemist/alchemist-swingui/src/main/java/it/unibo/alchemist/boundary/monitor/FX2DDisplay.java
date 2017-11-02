package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.boundary.monitor.generic.AbstractFXDisplay;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Simple implementation of a monitor that graphically represents a 2D space and simulation.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class FX2DDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public FX2DDisplay() {
        super();
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public FX2DDisplay(final int step) {
        super(step);
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        getWormhole().zoomOnPoint(getWormhole().getViewPoint(center), zoomLevel);
    }
}
