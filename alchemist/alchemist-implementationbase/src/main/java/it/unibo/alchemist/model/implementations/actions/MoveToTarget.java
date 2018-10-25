/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;

import it.unibo.alchemist.model.implementations.movestrategies.speed.ConstantSpeed;
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTarget;
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
 *            concentration type
 * @param <P>
 *            {@link Position} type
 */
public final class MoveToTarget<T, P extends Position<P>> extends AbstractConfigurableMoveNode<T, P> {

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
    public MoveToTarget(final Environment<T, P> environment,
            final Node<T> node,
            final Reaction<T> reaction,
            final Molecule trackMolecule,
            final double speed) {
        super(environment, node,
                (p1, p2) -> new PolygonalChain<>(p1, p2),
                new FollowTarget<>(environment, node, trackMolecule),
                new ConstantSpeed<>(reaction, speed));
        this.trackMolecule = trackMolecule;
        this.speed = speed;
    }

    @Override
    public MoveToTarget<T, P> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new MoveToTarget<>(getEnvironment(), n, r, trackMolecule, speed);
    }

    @Override
    protected P getDestination(final P current, final P target, final double maxWalk) {
        final P vector = target.minus(current);
        if (current.getDistanceTo(target) < maxWalk) {
            return vector;
        }
        final double angle = atan2(vector.getCoordinate(1), vector.getCoordinate(0));
        return getEnvironment().makePosition(maxWalk * cos(angle), maxWalk * sin(angle));
    }

}
