package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * An output monitor that supports zooming on bidimensional environments.
 * 
 * @param <T>
 */
public interface Graphical2DOutputMonitor<T, P extends Position<? extends P>> extends GraphicalOutputMonitor<T, P> {

    /**
     * @param center
     *            the point where to zoom
     * @param zoomLevel
     *            the desired zoom level
     */
    void zoomTo(P center, double zoomLevel);

}
