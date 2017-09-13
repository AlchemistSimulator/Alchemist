package it.unibo.alchemist.model.implementations.actions;

import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.atan2;

import it.unibo.alchemist.model.implementations.movestrategies.speed.ConstantSpeed;
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTarget;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.implementations.routes.PolygonalChain;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Movement towards a target defined as a concentration.
 *
 * @param <T>
 */
public class MoveToTarget<T> extends AbstractConfigurableMoveNode<T> {

    private static final long serialVersionUID = 1L;
    private final Molecule trackMolecule;
    private final double speed;

    /**
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param reaction
     *            the reaction
     * @param trackMolecule
     *            the molecule whose concentration will be intended as
     *            destination
     * @param speed
     *            the speed of the node
     */
    public MoveToTarget(final Environment<T> environment,
            final Node<T> node,
            final Reaction<T> reaction,
            final Molecule trackMolecule,
            final double speed) {
        super(environment, node,
                PolygonalChain::new,
                new FollowTarget<>(environment, node, trackMolecule),
                new ConstantSpeed<>(reaction, speed));
        this.trackMolecule = trackMolecule;
        this.speed = speed;
    }

    @Override
    public MoveToTarget<T> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new MoveToTarget<>(getEnvironment(), n, r, trackMolecule, speed);
    }

    @Override
    protected Position getDestination(final Position current, final Position target, final double maxWalk) {
        final Position vector = target.subtract(current);
        if (current.getDistanceTo(target) < maxWalk) {
            return vector;
        }
        final double angle = atan2(vector.getCoordinate(1), vector.getCoordinate(0));
        return new Continuous2DEuclidean(maxWalk * cos(angle), maxWalk * sin(angle));
    }

}
