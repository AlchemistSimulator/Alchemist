/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position2D;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.properties.CellularProperty;
import org.apache.commons.math3.util.FastMath;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @param <P> {@link Position2D} type
 */
public final class ChemotacticPolarization<P extends Position2D<P>> extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double, P> env;
    private final Biomolecule biomol;
    private final boolean ascend;

    /**
     * 
     * @param environment the environment
     * @param node the node
     * @param biomolecule biomolecule's name
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction of
     *                  the highest concentration of biomolecule in neighborhood; if it's false, the versor will be
     *                   directed in the exactly the opposite direction.
     */
    public ChemotacticPolarization(
            final Environment<Double, P> environment,
            final Node<Double> node,
            final Biomolecule biomolecule,
            final String ascendGrad
    ) {
        super(node);
        this.env = Objects.requireNonNull(environment);
        this.biomol = Objects.requireNonNull(biomolecule);
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
            final Environment<Double, P> environment,
            final Node<Double> node,
            final String biomolecule,
            final String ascendGrad
    ) {
        this(environment, node, new Biomolecule(biomolecule), ascendGrad);
    }


    @Override
    public ChemotacticPolarization<P> cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new ChemotacticPolarization<>(env, node, biomol.toString(), ascend ? "up" : "down");
    }

    @Override
    public void execute() {
        // declaring a variable for the node where this action is set, to have faster access
        final Node<Double> thisNode = getNode();
        final List<Node<Double>> l = env.getNeighborhood(thisNode).getNeighbors().stream()
                .filter(n -> n instanceof EnvironmentNode && n.contains(biomol))
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            thisNode.asCapability(CellularProperty.class).addPolarizationVersor(env.makePosition(0, 0));
        } else {
            final boolean isNodeOnMaxConc = env.getPosition(l.stream()
                    .max(Comparator.comparingDouble(n -> n.getConcentration(biomol)))
                    .get()).equals(env.getPosition(thisNode));
            if (isNodeOnMaxConc) {
                thisNode.asCapability(CellularProperty.class)
                        .addPolarizationVersor(env.makePosition(0, 0));
            } else {
                P newPolVer = weightedAverageVectors(l, thisNode);
                final double newPolX = newPolVer.getX();
                final double newPolY = newPolVer.getY();
                final double newPolVerModule = FastMath.sqrt(newPolX * newPolX + newPolY * newPolY);
                if (newPolVerModule == 0) {
                    thisNode.asCapability(CellularProperty.class).addPolarizationVersor(newPolVer);
                } else {
                    newPolVer = env.makePosition(newPolVer.getX() / newPolVerModule,
                            newPolVer.getY() / newPolVerModule);
                    if (ascend) {
                        thisNode.asCapability(CellularProperty.class).addPolarizationVersor(newPolVer);
                    } else {
                        thisNode.asCapability(CellularProperty.class)
                                .addPolarizationVersor(env.makePosition(-newPolVer.getX(), -newPolVer.getY()));
                    }
                }
            }
        }
    }

    private P weightedAverageVectors(final List<Node<Double>> list, final Node<Double> thisNode) {
        P res = env.makePosition(0, 0);
        final P thisNodePos = env.getPosition(thisNode);
        for (final Node<Double> n : list) {
            final P nPos = env.getPosition(n);
            P vecTemp = env.makePosition(
                    nPos.getX() - thisNodePos.getX(),
                    nPos.getY() - thisNodePos.getY());
            final double vecTempModule = FastMath.sqrt(FastMath.pow(vecTemp.getX(), 2)
                    + FastMath.pow(vecTemp.getY(), 2));
            vecTemp = env.makePosition(
                    n.getConcentration(biomol) * (vecTemp.getX() / vecTempModule),
                    n.getConcentration(biomol) * (vecTemp.getY() / vecTempModule));
            res = env.makePosition(
                    res.getX() + vecTemp.getX(),
                    res.getY() + vecTemp.getY());
        }
        return res;
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public Node<Double> getNode() {
        return super.getNode();
    }

}
