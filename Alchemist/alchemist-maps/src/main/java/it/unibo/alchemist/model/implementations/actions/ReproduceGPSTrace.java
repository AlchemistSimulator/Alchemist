package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.strategies.routing.IgnoreStreets;
import it.unibo.alchemist.model.implementations.strategies.speed.ConstantSpeed;
import it.unibo.alchemist.model.implementations.strategies.speed.StraightLineTraceDependantSpeed;
import it.unibo.alchemist.model.implementations.strategies.target.FollowTrace;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <T>
 */
public class ReproduceGPSTrace<T> extends MoveOnMap<T> {

    private static final long serialVersionUID = -2291955689914046763L;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     */
    public ReproduceGPSTrace(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction) {
        super(environment, node,
                new IgnoreStreets<>(),
                new StraightLineTraceDependantSpeed<>(environment, node, reaction),
                new FollowTrace<>(environment, node, reaction));
    }

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction. Will be used to compute the distance to walk in
     *            every step, relying on {@link Reaction}'s getRate() method.
     * @param speed
     *            the average speed
     */
    public ReproduceGPSTrace(final IMapEnvironment<T> environment, final Node<T> node, final Reaction<T> reaction, final double speed) {
        super(environment, node,
                new IgnoreStreets<>(),
                new ConstantSpeed<>(reaction, speed),
                new FollowTrace<>(environment, node, reaction));
    }

}
