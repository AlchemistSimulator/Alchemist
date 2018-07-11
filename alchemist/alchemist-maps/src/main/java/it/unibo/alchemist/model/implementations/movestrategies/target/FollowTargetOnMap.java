package it.unibo.alchemist.model.implementations.movestrategies.target;

import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.GeoPosition;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * This strategy reads the value of a "target" molecule and tries to interpret it as a coordinate.
 * 
 * @param <T>
 */
public class FollowTargetOnMap<T> extends FollowTarget<T, GeoPosition> {

    private static final long serialVersionUID = 0L;

    /**
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param targetMolecule
     *            the target molecule
     */
    public FollowTargetOnMap(final Environment<T, GeoPosition> env, final Node<T> n, final Molecule targetMolecule) {
        super(env, n, targetMolecule);
    }

    @Override
    protected GeoPosition createPosition(final double latitude, final double longitude) {
        return new LatLongPosition(latitude, longitude);
    }
}
