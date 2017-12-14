package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.boundary.monitor.generic.AbstractFXDisplay;
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Simple implementation of a monitor that graphically represents a simulation on a 2D map.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class FXMapDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    // TODO
    // TODO
    // TODO
    // TODO

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public FXMapDisplay() {
        super();
        // TODO
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public FXMapDisplay(final int step) {
        super(step);
        // TODO
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        final BidimensionalWormhole wh = getWormhole();
        if (wh != null) {
            wh.zoomOnPoint(wh.getViewPoint(center), zoomLevel);
        }
    }
}
