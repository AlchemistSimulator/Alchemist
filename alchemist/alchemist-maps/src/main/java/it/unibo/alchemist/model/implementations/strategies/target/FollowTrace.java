package it.unibo.alchemist.model.implementations.strategies.target;

import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.IRoute;
import it.unibo.alchemist.model.interfaces.strategies.TargetSelectionStrategy;

/**
 * This strategy follows a {@link IRoute}.
 * 
 * @param <T>
 */
public class FollowTrace<T> implements TargetSelectionStrategy<T> {

    private static final long serialVersionUID = -446053307821810437L;
    private final IMapEnvironment<T> environment;
    private final Node<T> node;
    private final Reaction<T> reaction;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     */
    public FollowTrace(final IMapEnvironment<T> env, final Node<T> n, final Reaction<T> r) {
        environment = env;
        node = n;
        reaction = r;
    }

    @Override
    public final Position getNextTarget() {
        return environment.getNextPosition(node, reaction.getTau());
    }

}
