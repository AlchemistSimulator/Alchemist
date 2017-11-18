package it.unibo.alchemist.model.implementations.movestrategies.routing;

import it.unibo.alchemist.model.implementations.routes.PolygonalChain;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy;
import it.unibo.alchemist.model.interfaces.Route;

/**
 * This strategy ignores any information about the map, and connects the
 * starting and ending point with a straight line using
 * {@link PolygonalChain}.
 * 
 */
public class IgnoreStreets implements RoutingStrategy {

    private static final long serialVersionUID = 2678088737744440021L;

    @Override
    public Route<Position> computeRoute(final Position currentPos, final Position finalPos) {
        return new PolygonalChain<>(currentPos, finalPos);
    }

}
