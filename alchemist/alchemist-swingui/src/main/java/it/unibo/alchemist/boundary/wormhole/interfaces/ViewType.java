package it.unibo.alchemist.boundary.wormhole.interfaces;

/**
 * This interface implements an Adapter pattern between a generic view element and the needs of a {@link Wormhole2D}.
 */
public interface ViewType {

    /**
     * Getter method for the width of the adapted view.
     *
     * @return the width
     */
    double getWidth();

    /**
     * Getter method for the height of the adapted view.
     *
     * @return the height
     */
    double getHeight();
}
