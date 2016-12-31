package it.unibo.alchemist.model.implementations.movestrategies.target;

import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * This strategy reads the value of a "target" molecule and tries to interpret it as a coordinate.
 * 
 * @param <T>
 */
public class FollowTargetOnMap<T> extends FollowTarget<T> {

    private static final long serialVersionUID = 0L;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param targetMolecule
     *            the target molecule
     */
    public FollowTargetOnMap(final Environment<T> env, final Node<T> n, final Molecule targetMolecule) {
        super(env, n, targetMolecule);
    }

    @Override
    protected Position createPosition(final double x, final double y) {
        return new LatLongPosition(x, y);
    }
}
