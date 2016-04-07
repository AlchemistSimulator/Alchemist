/**
 * 
 */
package it.unibo.alchemist.model.interfaces.strategies;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * This interface models a strategy for selecting positions where to move.
 * 
 * @param <T>
 */
@FunctionalInterface
public interface TargetSelectionStrategy<T> extends Serializable {

    /**
     * @return the next target where the {@link Node} is directed
     */
    Position getNextTarget();

}
