package it.unibo.alchemist.model.interfaces;

/**
 * 
 * Route with total trip time to cross it.
 * 
 * @param <P> type of position in the route
 */
public interface TimedRoute<P extends Position<?>> extends Route<P> {

    /**
     * 
     * @return the total trip time 
     */
    double getTripTime();

}
