package it.unibo.alchemist.model.interfaces;

/**
 * 
 * Interface for static layer, containing a substance or a molecule with a spatial distribution.
 *
 * @param <T> the value that measure the substance in a point.
 */
public interface Layer<T> {

    /**
     * 
     * @param p the {@link Position}.
     * @return the value in the requested {@link Position}.
     */
    T getValue(Position p);

}
