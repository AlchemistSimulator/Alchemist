/**
 * 
 */
package it.unibo.alchemist.model.interfaces.movestrategies;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * This interface models a strategy for selecting positions where to move.
 * 
 */
@FunctionalInterface
public interface TargetSelectionStrategy<P extends Position<? extends P>> extends Serializable {

    /**
     * @return the next target where the {@link Node} is directed
     */
    P getTarget();

}
