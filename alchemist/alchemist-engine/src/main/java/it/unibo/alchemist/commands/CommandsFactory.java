package it.unibo.alchemist.commands;

import org.slf4j.LoggerFactory;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Command;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * This class provides some useful static methods which help you manipulating a {@link Environment},
 * namely adding, removing, moving or cloning a node.
 * 
 */
public final class CommandsFactory {

    private CommandsFactory() { }

    /**
     * Provides a {@link Command} which adds a node to the selected spawned in the selected position.
     * 
     * @param <T> the type of concentration
     * @param node The {@link Node} to add
     * @param position the {@link Position} of the new node
     * @return a {@link Command} which performs this job
     */
    public static <T> Command<T> newAddNodeCommand(final Node<T> node, final Position position) {
        return s -> s.getEnvironment().addNode(node, position);
    }

    /**
     * Provides a {@link Command} which moves a node belonging to the environment.
     * to the new position
     * 
     * @param <T> the type of concentration
     * @param node The {@link Node} to move
     * @param position the new {@link Position} of the node
     * @return a {@link Command} which performs this job
     */
    public static <T> Command<T> newMoveNodeCommand(final Node<T> node, final Position position) {
        return s -> s.getEnvironment().moveNode(node, position);
    }

    /**
     * Provides a {@link Command} which removes a node from the environment.
     * 
     * @param <T> the type of concentration
     * @param node The {@link Node} to add
     * @return a {@link Command} which performs this job
     */
    public static <T> Command<T> newRemoveNodeCommand(final Node<T> node) {
        return s -> s.getEnvironment().removeNode(node);
    }

    /**
     * Provides a {@link Command} which clones a node of the selected environment.
     * spawning the new node in the selected position
     * 
     * @param <T> the type of concentration
     * @param node The {@link Node} to clone
     * @param direction the {@link Position} of the new node
     * @return a {@link Command} which performs this job
     */
    public static <T> Command<T> newCloneNodeCommand(final Node<T> node, final Position direction) {
        return s -> {
            final Environment<T> env = s.getEnvironment();
            try {
                final Node<T> newNode = env.getNodeByID(node.getId()).cloneNode();
                env.addNode(newNode, direction);
                Engine.nodeAdded(env, newNode);
            } catch (UnsupportedOperationException | AbstractMethodError e) {
                LoggerFactory.getLogger("newCloneNodeCommand Lambda").error("Cannot clone node " + node.getId(), e);
            }
        };
    }

    /**
     * Adds a {@link Command} to the provided {@link Simulation}.
     * 
     * @param <T> the type of concentration
     * @param sim the {@link Simulation} 
     * @param comm the {@link Command}
     */
    public static <T> void addCommandToISimulation(final Simulation<T> sim, final Command<T> comm) {
        sim.addCommand(comm);
    }
}
