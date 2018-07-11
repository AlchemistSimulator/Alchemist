package it.unibo.alchemist.model.interfaces;

/**
 * A bidimensional position.
 *
 * @param <P>
 */
public interface Position2D<P extends Position2D<? extends P>> extends Position<P> {

    /**
     * @return horizontal position
     */
    double getX();

    /**
     * @return vertical position
     */
    double getY();

}
