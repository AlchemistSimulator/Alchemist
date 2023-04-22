/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.actions;

import com.google.common.collect.ImmutableList;
import it.unibo.alchemist.model.implementations.movestrategies.speed.ConstantSpeed;
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTarget;
import it.unibo.alchemist.model.implementations.routes.PolygonalChain;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position2D;
import it.unibo.alchemist.model.Reaction;

import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;

/**
 * Movement towards a target defined as a concentration.
 *
 * @param <T>
 *            concentration type
 * @param <P>
 *            {@link Position2D} type
 */
public final class MoveToTarget<T, P extends Position2D<P>> extends AbstractConfigurableMoveNode<T, P> {

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
                (p1, p2) -> new PolygonalChain<>(ImmutableList.of(p1, p2)),
                new FollowTarget<>(environment, node, trackMolecule),
                new ConstantSpeed<>(reaction, speed));
        this.trackMolecule = trackMolecule;
        this.speed = speed;
    }

    @Override
    public MoveToTarget<T, P> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        return new MoveToTarget<>(getEnvironment(), node, reaction, trackMolecule, speed);
    }

    @Override
    protected P interpolatePositions(final P current, final P target, final double maxWalk) {
        final P vector = target.minus(current.getCoordinates());
        if (current.distanceTo(target) < maxWalk) {
            return vector;
        }
        final double angle = atan2(vector.getY(), vector.getX());
        return getEnvironment().makePosition(maxWalk * cos(angle), maxWalk * sin(angle));
    }

}
