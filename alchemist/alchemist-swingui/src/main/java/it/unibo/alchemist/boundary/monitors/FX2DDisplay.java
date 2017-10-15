package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@inheritDoc}
 */
public class FX2DDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /**
     * Default logger for this class. It hides {@link AbstractFXDisplay}'s one.
     */
    private static final Logger L = LoggerFactory.getLogger(FX2DDisplay.class);

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

    @Override
    protected Logger getLogger() {
        return FX2DDisplay.L;
    }
}
