package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FXDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /**
     * Default logger for this class. It hides {@link AbstractFXDisplay}'s one.
     */
    private static final Logger L = LoggerFactory.getLogger(FXDisplay.class);

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public FXDisplay() {
        super();
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public FXDisplay(final int step) {
        super(step);
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        getWormhole().zoomOnPoint(getWormhole().getViewPoint(center), zoomLevel);
    }

    @Override
    protected Logger getLogger() {
        return FXDisplay.L;
    }
}
