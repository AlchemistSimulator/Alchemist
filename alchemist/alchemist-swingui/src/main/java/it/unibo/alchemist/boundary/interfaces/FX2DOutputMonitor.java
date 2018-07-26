package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Position2D;

/**
 * {@link OutputMonitor} that handles the graphical part of a bidimensional zoomable simulation in JavaFX.
 *
 * @param <T> the {@link Concentration} type
 */
public interface FX2DOutputMonitor<T, P extends Position2D<? extends P>> extends FXOutputMonitor<T, P> {

    /**
     * @param center    the point where to zoom
     * @param zoomLevel the desired zoom level
     */
    void zoomTo(P center, double zoomLevel);
}
