package it.unibo.alchemist.model.interfaces;

import java.io.Serializable;

/**
 * 
 * Interface for static layer, containing a substance or a molecule with a spatial distribution.
 *
 * @param <T> the value that measure the substance in a point.
 */
@FunctionalInterface
public interface Layer<T, P extends Position<? extends P>> extends Serializable {

    /**
     * 
     * @param p the {@link Position}.
     * @return the value in the requested {@link Position}.
     */
    T getValue(P p);

}
