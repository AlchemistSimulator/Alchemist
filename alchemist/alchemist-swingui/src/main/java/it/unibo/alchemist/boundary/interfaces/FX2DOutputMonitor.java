package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.model.interfaces.Position;

public interface FX2DOutputMonitor<T> extends FXOutputMonitor<T> {
    /**
     * @param center
     *            the point where to zoom
     * @param zoomLevel
     *            the desired zoom level
     */
    void zoomTo(Position center, double zoomLevel);
}
