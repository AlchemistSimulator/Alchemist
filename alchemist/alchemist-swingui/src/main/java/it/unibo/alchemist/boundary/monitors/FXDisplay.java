package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FXDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /** Default logger for this class. It hides {@link AbstractFXDisplay}'s one. */
    private static final Logger L = LoggerFactory.getLogger(FXDisplay.class);

    // TODO

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        // TODO
    }
}
