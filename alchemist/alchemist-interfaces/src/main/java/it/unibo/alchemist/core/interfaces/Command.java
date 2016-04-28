package it.unibo.alchemist.core.interfaces;

/**
 * This interface represents a command which will be executed by the simulation
 * itself. The concept of command is very loose, as it can be everything
 * affecting the simulation, such as adding a node or a reaction, moving a node,
 * pausing or resuming the simulation, etc. The {@link #execute(Simulation)}
 * will effectively do what the command must do, and it will be typically
 * invoked by an {@link Simulation}.
 * 
 * @param <T>
 *            the type of concentration
 */

public interface Command<T> {

    /**
     * Invoking this method will cause this command to be executed.
     * 
     * @param s
     *            the {@link Simulation} which will execute this command
     */
    void execute(Simulation<T> s);
}
