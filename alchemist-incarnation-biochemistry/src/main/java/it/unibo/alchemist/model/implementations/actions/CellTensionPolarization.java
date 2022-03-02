/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellularProperty;
import it.unibo.alchemist.model.interfaces.properties.CircularCellularProperty;
import it.unibo.alchemist.model.interfaces.properties.CircularDeformableCellularProperty;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.MathUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @param <P> {@link Position2D} type
 */
public final class CellTensionPolarization<P extends Position2D<P>> extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<P> env;
    private final CircularDeformableCellularProperty<P> deformableCell;

    /**
     * 
     * @param node the node
     * @param environment the environment
     */
    public CellTensionPolarization(
            final EnvironmentSupportingDeformableCells<P> environment,
            final Node<Double> node
    ) {
        super(node);
        this.env = environment;
        this.deformableCell = Objects.requireNonNull(
            node.asCapabilityOrNull(CircularDeformableCellularProperty.class),
            "The node must be a " + CircularDeformableCellularProperty.class.getSimpleName()
        );
    }

    @Override
    public CellTensionPolarization<P> cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new CellTensionPolarization<>(env, node);
    }

    @Override
    public void execute() {
        // get node position as array
        final double[] nodePos = env.getPosition(getNode()).getCoordinates();
        // initializing resulting versor
        final double[] resVersor = new double[nodePos.length];
        // declaring a variable for the node where this action is set, to have faster access
        final Node<Double> thisNode = getNode();
        // transforming each node around in a vector (Position) 
        final List<P> pushForces = env.getNodesWithinRange(
                thisNode,
                env.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .filter(n -> { // only cells overlapping this cell are selected
                    final CircularCellularProperty<P> circularCell = n.asCapabilityOrNull(CircularCellularProperty.class);
                    if (circularCell != null) {
                        // computing for each cell the max distance among which can't be overlapping
                        double maxDist;
                        if (n.asCapabilityOrNull(CircularDeformableCellularProperty.class) != null) {
                            // for deformable cell is maxRad + maxRad
                             maxDist = thisNode
                                     .asCapability(CircularDeformableCellularProperty.class).getMaximumRadius()
                                     + n.asCapability(CircularDeformableCellularProperty.class).getMaximumRadius();
                        } else {
                            // for simple cells is maxRad + rad
                             maxDist = thisNode
                                     .asCapability(CircularDeformableCellularProperty.class).getMaximumRadius()
                                     + n.asCapability(CircularCellularProperty.class).getRadius();
                        }
                        // check
                        return env.getDistanceBetweenNodes(thisNode, n) < maxDist;
                    } else {
                        // only CellWithCircularArea are selected.
                        return false;
                    }
                })
                .map(n -> {
                    // position of node n as array
                    final double[] nPos =  env.getPosition(n).getCoordinates();
                    // max radius of n
                    final double localNodeMaxRadius;
                    // min radius of n
                    final double localNodeMinRadius;
                    // max radius of this node (thisNode)
                    final double nodeMaxRadius = thisNode.asCapability(CircularDeformableCellularProperty.class)
                            .getMaximumRadius();
                    // min radius of this node (thisNode)
                    final double nodeMinRadius = thisNode.asCapability(CircularDeformableCellularProperty.class)
                            .getRadius();
                    // intensity of tension between n and this node (thisNode), measured as value between 0 and 1
                    final double intensity;
                    if (n.asCapabilityOrNull(CircularDeformableCellularProperty.class) != null) {
                        final Node<Double> localNode = n;
                        localNodeMaxRadius = localNode.asCapability(CircularDeformableCellularProperty.class)
                                .getMaximumRadius();
                        localNodeMinRadius = localNode.asCapability(CircularDeformableCellularProperty.class)
                                .getRadius();
                    } else {
                        localNodeMaxRadius = n.asCapabilityOrNull(CircularCellularProperty.class).getRadius();
                        localNodeMinRadius = localNodeMaxRadius;
                    }
                    // if both cells has no difference between maxRad and minRad intensity must be 1
                    if (MathUtils.fuzzyEquals(localNodeMaxRadius, localNodeMinRadius)
                            && MathUtils.fuzzyEquals(nodeMaxRadius, nodeMinRadius)
                    ) {
                        intensity = 1;
                    } else {
                        final double maxRadiusSum = localNodeMaxRadius + nodeMaxRadius;
                        intensity = (maxRadiusSum - env.getDistanceBetweenNodes(n, thisNode))
                                / (maxRadiusSum - localNodeMinRadius - nodeMinRadius);
                    }
                    if (intensity != 0) {
                        double[] propensityVector = {nodePos[0] - nPos[0], nodePos[1] - nPos[1]};
                        final double module = FastMath.sqrt(FastMath.pow(propensityVector[0], 2)
                                + FastMath.pow(propensityVector[1], 2));
                        if (module == 0) {
                            return env.makePosition(0, 0);
                        }
                        propensityVector = new double[]{
                                intensity * (propensityVector[0] / module),
                                intensity * (propensityVector[1] / module)
                        };
                        return env.makePosition(propensityVector[0], propensityVector[1]);
                    } else {
                        return env.makePosition(0, 0);
                    } 
                })
                .collect(Collectors.toList());
        if (pushForces.isEmpty()) {
            thisNode.asCapability(CellularProperty.class).addPolarizationVersor(env.makePosition(0,0));
        } else {
            for (final P p : pushForces) {
                resVersor[0] = resVersor[0] + p.getX();
                resVersor[1] = resVersor[1] + p.getY();
            }
            final double module = FastMath.sqrt(FastMath.pow(resVersor[0], 2) + FastMath.pow(resVersor[1], 2));
            if (module == 0) {
                thisNode.asCapability(CellularProperty.class).addPolarizationVersor(env.makePosition(0,0));
            } else {
                thisNode.asCapability(CellularProperty.class)
                        .addPolarizationVersor(
                                env.makePosition(resVersor[0] / module,
                                        resVersor[1] / module)
                        );
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
