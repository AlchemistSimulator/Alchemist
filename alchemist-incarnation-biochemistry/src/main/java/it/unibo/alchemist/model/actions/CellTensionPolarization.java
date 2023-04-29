/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.EnvironmentSupportingDeformableCells;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CircularCellProperty;
import it.unibo.alchemist.model.interfaces.properties.CircularDeformableCellProperty;
import org.apache.commons.math3.util.FastMath;
import org.danilopianini.lang.MathUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Models the tension polarization of {@link it.unibo.alchemist.model.implementations.properties.CircularDeformableCell}
 * in an {@link EnvironmentSupportingDeformableCells}.
 */
public final class CellTensionPolarization extends AbstractAction<Double> {

    private static final long serialVersionUID = 1L;
    private final EnvironmentSupportingDeformableCells<Euclidean2DPosition> environment;
    private final CircularDeformableCellProperty deformableCell;

    /**
     * 
     * @param node the node
     * @param environment the environment
     */
    public CellTensionPolarization(
            final EnvironmentSupportingDeformableCells<Euclidean2DPosition> environment,
            final Node<Double> node
    ) {
        super(node);
        this.environment = environment;
        this.deformableCell = Objects.requireNonNull(getDeformableCell(node),
            "The node must have a " + CircularDeformableCellProperty.class.getSimpleName()
        );
    }

    private CircularDeformableCellProperty getDeformableCell(final Node<Double> node) {
        return node.asPropertyOrNull(CircularDeformableCellProperty.class);
    }

    private CircularCellProperty getCircularCell(final Node<Double> node) {
        return node.asPropertyOrNull(CircularCellProperty.class);
    }

    private boolean isDeformableCell(final Node<Double> node) {
        return getDeformableCell(node) != null;
    }

    @Override
    public CellTensionPolarization cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new CellTensionPolarization(environment, node);
    }

    @Override
    public void execute() {
        // get node position as array
        final double[] nodePosistion = environment.getPosition(getNode()).getCoordinates();
        // initializing resulting versor
        final double[] resultingVersor = new double[nodePosistion.length];
        // declaring a variable for the node where this action is set, to have faster access
        final Node<Double> thisNode = getNode();
        // transforming each node around in a vector (Position) 
        final List<Euclidean2DPosition> pushForces = environment.getNodesWithinRange(
                thisNode,
                environment.getMaxDiameterAmongCircularDeformableCells()).stream()
                .parallel()
                .filter(node -> { // only cells overlapping this cell are selected
                    final CircularCellProperty circularCell = getCircularCell(node);
                    if (!Objects.isNull(circularCell)) {
                        // computing for each cell the max distance among which can't be overlapping
                        double maxDistance;
                        if (isDeformableCell(node)) {
                            // for deformable cell is maxRad + maxRad
                             maxDistance = deformableCell.getMaximumRadius() + getDeformableCell(node).getMaximumRadius();
                        } else {
                            // for simple cells is maxRad + rad
                             maxDistance = deformableCell.getMaximumRadius() + circularCell.getRadius();
                        }
                        // check
                        return environment.getDistanceBetweenNodes(thisNode, node) < maxDistance;
                    } else {
                        // only CellWithCircularArea are selected.
                        return false;
                    }
                })
                .map(node -> {
                    // position of node n as array
                    final double[] nPos =  environment.getPosition(node).getCoordinates();
                    // max radius of n
                    final double localNodeMaxRadius;
                    // min radius of n
                    final double localNodeMinRadius;
                    // max radius of this node (thisNode)
                    final double nodeMaxRadius = deformableCell.getMaximumRadius();
                    // min radius of this node (thisNode)
                    final double nodeMinRadius = deformableCell.getRadius();
                    // intensity of tension between n and this node (thisNode), measured as value between 0 and 1
                    final double intensity;
                    if (isDeformableCell(node)) {
                        localNodeMaxRadius = getDeformableCell(node).getMaximumRadius();
                        localNodeMinRadius = getDeformableCell(node).getRadius();
                    } else {
                        localNodeMaxRadius = getCircularCell(node).getRadius();
                        localNodeMinRadius = localNodeMaxRadius;
                    }
                    // if both cells has no difference between maxRad and minRad intensity must be 1
                    if (MathUtils.fuzzyEquals(localNodeMaxRadius, localNodeMinRadius)
                            && MathUtils.fuzzyEquals(nodeMaxRadius, nodeMinRadius)
                    ) {
                        intensity = 1;
                    } else {
                        final double maxRadiusSum = localNodeMaxRadius + nodeMaxRadius;
                        intensity = (maxRadiusSum - environment.getDistanceBetweenNodes(node, thisNode))
                                / (maxRadiusSum - localNodeMinRadius - nodeMinRadius);
                    }
                    if (intensity != 0) {
                        double[] propensityVector = {nodePosistion[0] - nPos[0], nodePosistion[1] - nPos[1]};
                        final double module = FastMath.sqrt(FastMath.pow(propensityVector[0], 2)
                                + FastMath.pow(propensityVector[1], 2));
                        if (module == 0) {
                            return environment.makePosition(0, 0);
                        }
                        propensityVector = new double[]{
                                intensity * (propensityVector[0] / module),
                                intensity * (propensityVector[1] / module)
                        };
                        return environment.makePosition(propensityVector[0], propensityVector[1]);
                    } else {
                        return environment.makePosition(0, 0);
                    } 
                })
                .collect(Collectors.toList());
        if (pushForces.isEmpty()) {
            deformableCell.addPolarizationVersor(environment.makePosition(0, 0));
        } else {
            for (final Euclidean2DPosition p : pushForces) {
                resultingVersor[0] = resultingVersor[0] + p.getX();
                resultingVersor[1] = resultingVersor[1] + p.getY();
            }
            final double module = FastMath.sqrt(FastMath.pow(resultingVersor[0], 2) + FastMath.pow(resultingVersor[1], 2));
            if (module == 0) {
                deformableCell.addPolarizationVersor(environment.makePosition(0, 0));
            } else {
                deformableCell.addPolarizationVersor(
                    environment.makePosition(resultingVersor[0] / module, resultingVersor[1] / module)
                );
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
