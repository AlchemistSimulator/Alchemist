/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import it.unibo.alchemist.AlchemistUtil;
import it.unibo.alchemist.model.interfaces.Position2D;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.MathUtils;

import it.unibo.alchemist.model.interfaces.CellWithCircularArea;
import it.unibo.alchemist.model.interfaces.CircularDeformableCell;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * @param <P>
 */
public final class CellTensionPolarization<P extends Position2D<P>> extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<P> env;

    /**
     * 
     * @param node the node
     * @param env the environment
     */
    public CellTensionPolarization(final EnvironmentSupportingDeformableCells<P> env, final CircularDeformableCell<P> node) {
        super(node);
        this.env = env;
    }

    @Override
    public CellTensionPolarization<P> cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new CellTensionPolarization<>(env, AlchemistUtil.cast(new TypeToken<CircularDeformableCell<P>>() { }, n));
    }

    @Override
    public void execute() {
        // get node position as array
        final double[] nodePos = env.getPosition(getNode()).getCartesianCoordinates();
        // initializing resulting versor
        final double[] resVersor = new double[nodePos.length];
        // declaring a variable for the node where this action is set, to have faster access
        final CircularDeformableCell<P> thisNode = getNode();
        // transforming each node around in a vector (Position) 
        final List<P> pushForces = env.getNodesWithinRange(
                thisNode,
                env.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .filter(n -> { // only cells overlapping this cell are selected
                    if (n instanceof CellWithCircularArea) {
                        // computing for each cell the max distance among which cant't be overlapping
                        double maxDist;
                        if (n instanceof CircularDeformableCell) {
                            // for deformable cell is maxRad + maxRad
                             maxDist = thisNode.getMaxRadius() + ((CircularDeformableCell<P>) n).getMaxRadius();
                        } else {
                            // for simple cells is maxRad + rad
                             maxDist = thisNode.getMaxRadius() + ((CellWithCircularArea<P>) n).getRadius();
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
                    final double[] nPos =  env.getPosition(n).getCartesianCoordinates();
                    // max radius of n
                    final double localNodeMaxRadius;
                    // min radius of n
                    final double localNodeMinRadius;
                    // max radius of this node (thisNode)
                    final double nodeMaxRadius = thisNode.getMaxRadius();
                    // min radius of this node (thisNode)
                    final double nodeMinRadius = thisNode.getRadius();
                    // intensity of tension between n and this node (thisNode), measured as value between 0 and 1
                    final double intensity;
                    if (n instanceof CircularDeformableCell) {
                        final CircularDeformableCell<P> localNode = (CircularDeformableCell<P>) n;
                        localNodeMaxRadius = localNode.getMaxRadius();
                        localNodeMinRadius = localNode.getRadius();
                    } else {
                        localNodeMaxRadius = ((CellWithCircularArea<P>) n).getRadius();
                        localNodeMinRadius = localNodeMaxRadius;
                    }
                    // if both cells has no difference between maxRad and minRad intensity must be 1
                    if (MathUtils.fuzzyEquals(localNodeMaxRadius, localNodeMinRadius) && MathUtils.fuzzyEquals(nodeMaxRadius, nodeMinRadius)) {
                        intensity = 1;
                    } else {
                        final double maxRadiusSum = localNodeMaxRadius + nodeMaxRadius;
                        intensity = (maxRadiusSum - env.getDistanceBetweenNodes(n, thisNode)) / (maxRadiusSum - localNodeMinRadius - nodeMinRadius);
                    }
                    if (intensity != 0) {
                        double[] propensityVect = new double[]{nodePos[0] - nPos[0], nodePos[1] - nPos[1]};
                        final double module = FastMath.sqrt(FastMath.pow(propensityVect[0], 2) + FastMath.pow(propensityVect[1], 2));
                        if (module == 0) {
                            return env.makePosition(0, 0);
                        }
                        propensityVect = new double[]{intensity * (propensityVect[0] / module), intensity * (propensityVect[1] / module)};
                        return env.makePosition(propensityVect[0], propensityVect[1]);
                    } else {
                        return env.makePosition(0, 0);
                    } 
                })
                .collect(Collectors.toList());
        if (pushForces.isEmpty()) {
            thisNode.addPolarization(env.makePosition(0, 0));
        } else {
            for (final P p : pushForces) {
                resVersor[0] = resVersor[0] + p.getX();
                resVersor[1] = resVersor[1] + p.getY();
            }
            final double module = FastMath.sqrt(FastMath.pow(resVersor[0], 2) + FastMath.pow(resVersor[1], 2));
            if (module == 0) {
                thisNode.addPolarization(env.makePosition(0, 0));
            } else {
                thisNode.addPolarization(env.makePosition(resVersor[0] / module, resVersor[1] / module));
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public CircularDeformableCell<P> getNode() {
        return (CircularDeformableCell<P>) super.getNode();
    }

}
