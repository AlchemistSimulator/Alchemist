/**
 * 
 */
package it.unibo.alchemist.model.interfaces.movestrategies;

import java.io.Serializable;

import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.IRoute;

/**
 * Strategy interface describing how the routing between two points happens.
 * 
 * @param <T> Concentration type
 */
@FunctionalInterface
public interface RoutingStrategy<T> extends Serializable {

    /**
     * Computes a route between two positions.
     * 
     * @param currentPos starting {@link Position}
     * @param finalPos ending {@link Position}
     * @return a {@link IRoute} connecting the two points
     */
    IRoute computeRoute(Position currentPos, Position finalPos);

}
