package it.unibo.alchemist.grid.simulation;
import org.apache.ignite.lang.IgniteCallable;

/**
 * Alchemist simulation that will be executed in remote cluster's nodes.
 *
 * @param <T>
 */
public interface RemoteSimulation<T> extends IgniteCallable<RemoteResult> {

}
