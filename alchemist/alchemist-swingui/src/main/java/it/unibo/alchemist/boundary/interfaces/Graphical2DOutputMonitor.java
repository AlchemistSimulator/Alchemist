package it.unibo.alchemist.boundary.interfaces;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * An output monitor that supports zooming on bidimensional environments.
 * 
 * @param <T>
 */
public interface Graphical2DOutputMonitor<T> extends GraphicalOutputMonitor<T> {

    /**
     * @param center
     *            the point where to zoom
     * @param zoomLevel
     *            the desired zoom level
     */
    void zoomTo(Position center, double zoomLevel);

}
