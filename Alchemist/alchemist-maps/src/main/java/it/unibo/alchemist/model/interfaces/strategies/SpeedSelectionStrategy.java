/**
 * 
 */
package it.unibo.alchemist.model.interfaces.strategies;

import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

import java.io.Serializable;

/**
 * Given the current target {@link Position}, this strategy interface computes
 * the current {@link Node}'s speed.
 * 
 * @param <T>
 */
@FunctionalInterface
public interface SpeedSelectionStrategy<T> extends Serializable {

    /**
     * @param target
     *            the {@link Position} describing where the {@link Node} is
     *            directed
     * @return the current node's speed
     */
    double getCurrentSpeed(Position target);

}
