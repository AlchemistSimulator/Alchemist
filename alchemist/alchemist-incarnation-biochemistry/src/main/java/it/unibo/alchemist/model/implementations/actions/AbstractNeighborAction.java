package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Represents an action on a neighbor.
 * @param <T> the concentration type.
 */
public abstract class AbstractNeighborAction<T> extends AbstractAction<T> {

    private static final long serialVersionUID = -2287346030993830896L;
    private final Environment<T> env;
    private final Node<T> node;
    private final RandomGenerator rand;

    /**
     * 
     * @param node the current node
     * @param environment the environment
     * @param randomGenerator the random generator
     */
    protected AbstractNeighborAction(final Node<T> node, final Environment<T> environment, final RandomGenerator randomGenerator) {
        super(node);
        this.node = node;
        env = environment;
        rand = randomGenerator;
    }

    /**
     * Execute the action on a random neighbor if the node has a neighborhood. Otherwise do nothing.
     */
    @Override
    public void execute() {
        final Neighborhood<T> neighborhood = env.getNeighborhood(node);
        if (!neighborhood.isEmpty()) {
            execute(neighborhood.getNeighborByNumber(rand.nextInt(neighborhood.size())));
        }
    }

    /**
     * Execute the action on the given target node.
     * NOTE, it is NOT guaranteed that this method checks if the target node is in the actual neighborhood 
     * of the node.
     * @param targetNode the node where the action will be execute
     */
    public abstract void execute(Node<T> targetNode);

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public abstract AbstractNeighborAction<T> cloneOnNewNode(final Node<T> node, final Reaction<T> reaction);

}
