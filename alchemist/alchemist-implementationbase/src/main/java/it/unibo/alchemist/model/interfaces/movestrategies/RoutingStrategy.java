/**
 * 
 */
package it.unibo.alchemist.model.interfaces.movestrategies;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Route;

/**
 * Strategy interface describing how the routing between two points happens.
 * 
 */
@FunctionalInterface
public interface RoutingStrategy extends Serializable {

    /**
     * Computes a route between two positions.
     * 
     * @param currentPos starting {@link Position}
     * @param finalPos ending {@link Position}
     * @return a {@link Route} connecting the two points
     */
    Route<? extends Position> computeRoute(Position currentPos, Position finalPos);

}
