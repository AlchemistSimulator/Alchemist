/**
 * 
 */
package it.unibo.alchemist.model.implementations.strategies.speed;

import java.util.Collection;

import org.danilopianini.lang.LangUtils;

import it.unibo.alchemist.model.interfaces.MapEnvironment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy;

/**
 * This strategy slows down nodes depending on how many "interacting" nodes are
 * found in the surroundings. It is an attempt at modeling crowding slow-downs.
 * 
 * @param <T>
 */
public class InteractWithOthers<T> implements SpeedSelectionStrategy<T> {

    private static final long serialVersionUID = -1900168887685703120L;
    private static final double MINIMUM_DISTANCE_WALKED = 1;
    private final MapEnvironment<T> env;
    private final Node<T> node;
    private final Molecule interacting;
    private final double rd, in, sp;

    /**
     * @param environment
     *            the environment
     * @param n
     *            the node
     * @param reaction
     *            the reaction
     * @param inter
     *            the molecule that identifies an interacting node
     * @param speed
     *            the normal speed of the node
     * @param radius
     *            the radius where to search for interacting nodes
     * @param interaction
     *            the interaction factor. This will be multiplied by a crowd
     *            factor dynamically computed, and the speed will be divided by
     *            the number obtained
     */
    public InteractWithOthers(final MapEnvironment<T> environment, final Node<T> n, final Reaction<T> reaction,
            final Molecule inter, final double speed, final double radius, final double interaction) {
        LangUtils.requireNonNull(environment, n, inter);
        env = environment;
        node = n;
        interacting = inter;
        sp = speed / reaction.getRate();
        rd = radius;
        in = interaction;

    }

    @Override
    public double getCurrentSpeed(final Position target) {
        double crowd = 0;
        final Collection<? extends Node<T>> neighs = env.getNodesWithinRange(node, rd);
        if (neighs.size() > 1 / in) {
            for (final Node<T> neigh : neighs) {
                if (neigh.contains(interacting)) {
                    crowd += 1 / env.getDistanceBetweenNodes(node, neigh);
                }
            }
        }
        return Math.max(sp / (crowd * in + 1), MINIMUM_DISTANCE_WALKED);
    }

}
