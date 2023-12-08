/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.actions;

import it.unibo.alchemist.model.actions.AbstractAction;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.biochemistry.properties.Cell;
import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import org.apache.commons.math3.util.FastMath;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Models the chemotactic polarization of a {@link Cell}.
 */
public final class ChemotacticPolarization extends AbstractAction<Double> {

    private static final long serialVersionUID = 1L;
    private final Environment<Double, Euclidean2DPosition> environment;
    private final Biomolecule biomolecule;
    private final boolean ascend;
    private final CellProperty<Euclidean2DPosition> cell;

    /**
     * 
     * @param environment the environment
     * @param node the node
     * @param biomolecule biomolecule's name
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction of
     *                  the highest concentration of biomolecule in neighborhood; if it's false, the versor will be
     *                   directed in the exactly opposite direction.
     */
    public ChemotacticPolarization(
            final Environment<Double, Euclidean2DPosition> environment,
            final Node<Double> node,
            final Biomolecule biomolecule,
            final String ascendGrad
    ) {
        super(node);
        this.cell = Objects.requireNonNull(
            node.asPropertyOrNull(CellProperty.class),
            "This action can't be added to nodes with no " + CellProperty.class.getSimpleName()
        );
        this.environment = Objects.requireNonNull(environment);
        this.biomolecule = Objects.requireNonNull(biomolecule);
        if ("up".equalsIgnoreCase(ascendGrad)) {
            this.ascend = true;
        } else if ("down".equalsIgnoreCase(ascendGrad)) {
            this.ascend = false;
        } else {
            throw new IllegalArgumentException("Possible imput string are only up or down");
        }
    }

    /**
     * Initialize a polarization activity regulated by environmental concentration of a molecule.
     * @param environment the environment
     * @param node the node
     * @param biomolecule biomolecule's name
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction
     *                   of the highest concentration of biomolecule in neighborhood; if it's false, the versor will
     *                   be directed in the exactly the opposite direction.
     */
    public ChemotacticPolarization(
            final Environment<Double, Euclidean2DPosition> environment,
            final Node<Double> node,
            final String biomolecule,
            final String ascendGrad
    ) {
        this(environment, node, new Biomolecule(biomolecule), ascendGrad);
    }


    @Override
    public ChemotacticPolarization cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new ChemotacticPolarization(environment, node, biomolecule.toString(), ascend ? "up" : "down");
    }

    @Override
    public void execute() {
        // declaring a variable for the node where this action is set, to have faster access
        final Node<Double> thisNode = getNode();
        final List<Node<Double>> l = environment.getNeighborhood(thisNode).getNeighbors().stream()
                .filter(n -> n instanceof EnvironmentNode && n.contains(biomolecule))
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            cell.addPolarizationVersor(Euclidean2DPosition.Companion.getZero());
        } else {
            final boolean isNodeOnMaxConc = environment.getPosition(l.stream()
                    .max(Comparator.comparingDouble(n -> n.getConcentration(biomolecule)))
                    .get()).equals(environment.getPosition(thisNode));
            if (isNodeOnMaxConc) {
                cell.addPolarizationVersor(environment.makePosition(0, 0));
            } else {
                Euclidean2DPosition newPolVer = weightedAverageVectors(l, thisNode);
                final double newPolX = newPolVer.getX();
                final double newPolY = newPolVer.getY();
                final double newPolVerModule = FastMath.sqrt(newPolX * newPolX + newPolY * newPolY);
                if (newPolVerModule == 0) {
                    cell.addPolarizationVersor(newPolVer);
                } else {
                    newPolVer = environment.makePosition(newPolVer.getX() / newPolVerModule,
                            newPolVer.getY() / newPolVerModule);
                    if (ascend) {
                        cell.addPolarizationVersor(newPolVer);
                    } else {
                        cell.addPolarizationVersor(environment.makePosition(-newPolVer.getX(), -newPolVer.getY()));
                    }
                }
            }
        }
    }

    private Euclidean2DPosition weightedAverageVectors(final List<Node<Double>> list, final Node<Double> thisNode) {
        Euclidean2DPosition res = Euclidean2DPosition.Companion.getZero();
        final Euclidean2DPosition thisNodePos = environment.getPosition(thisNode);
        for (final Node<Double> n : list) {
            final Euclidean2DPosition nPos = environment.getPosition(n);
            Euclidean2DPosition vecTemp = new Euclidean2DPosition(
                nPos.getX() - thisNodePos.getX(),
                nPos.getY() - thisNodePos.getY()
            );
            final double vecTempModule = FastMath.sqrt(
                FastMath.pow(vecTemp.getX(), 2) + FastMath.pow(vecTemp.getY(), 2)
            );
            vecTemp = new Euclidean2DPosition(
                    n.getConcentration(biomolecule) * (vecTemp.getX() / vecTempModule),
                    n.getConcentration(biomolecule) * (vecTemp.getY() / vecTempModule)
            );
            res = new Euclidean2DPosition(
                res.getX() + vecTemp.getX(),
                res.getY() + vecTemp.getY()
            );
        }
        return res;
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
