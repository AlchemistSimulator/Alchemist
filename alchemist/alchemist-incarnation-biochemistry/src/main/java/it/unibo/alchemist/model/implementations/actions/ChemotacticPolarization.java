/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 */
public final class ChemotacticPolarization<P extends Position<? extends P>> extends AbstractAction<Double> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Environment<Double, P> env;
    private final Biomolecule biomol;
    private final boolean ascend;

    /**
     * 
     * @param environment 
     * @param node 
     * @param biomol 
     * @param ascendGrad 
     */
    public ChemotacticPolarization(final Environment<Double, P> environment, final CellNode<P> node, final Biomolecule biomol, final String ascendGrad) {
        super(node);
        this.env = Objects.requireNonNull(environment);
        this.biomol = Objects.requireNonNull(biomol);
        if (ascendGrad.equalsIgnoreCase("up")) {
            this.ascend = true;
        } else if (ascendGrad.equalsIgnoreCase("down")) {
            this.ascend = false;
        } else {
            throw new IllegalArgumentException("Possible imput string are only up or down");
        }
    }

    /**
     * Initialize a polarization activity regulated by environmental concentration of a molecule.
     * @param environment 
     * @param node 
     * @param biomol biomolecule's name
     * @param ascendGrad if that parameter is true, the polarization versor of the cell will be directed in direction of the greates concentration of biomolecule in neighborhood; if it's false, the versor will be directed in the exactly the opposite direction.
     */
    @SuppressWarnings("unchecked")
    public ChemotacticPolarization(final Environment<Double, P> environment, final Node<Double> node, final String biomol, final String ascendGrad) {
        this(environment, (CellNode<P>) node, new Biomolecule(biomol), ascendGrad);
    }


    @Override
    public ChemotacticPolarization<P> cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new ChemotacticPolarization<>(env, n, biomol.toString(), ascend ? "up" : "down");
    }

    @Override
    public void execute() {
        // declaring a variable for the node where this action is set, to have faster access
        final CellNode<P> thisNode = getNode();
        final List<Node<Double>> l = env.getNeighborhood(thisNode).getNeighbors().stream()
                .filter(n -> n instanceof EnvironmentNode && n.contains(biomol))
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            thisNode.addPolarization(env.makePosition(0, 0));
        } else {
            final boolean isNodeOnMaxConc = env.getPosition(l.stream()
                    .max((n1, n2) -> Double.compare(n1.getConcentration(biomol), n2.getConcentration(biomol)))
                    .get()).equals(env.getPosition(thisNode));
            if (isNodeOnMaxConc) {
                thisNode.addPolarization(env.makePosition(0, 0));
            } else {
                P newPolVer = weightedAverageVectors(l, thisNode);
                final double newPolVerModule = FastMath.sqrt(FastMath.pow(
                        newPolVer.getCoordinate(0), 2) + FastMath.pow(newPolVer.getCoordinate(1), 2)
                        );
                if (newPolVerModule == 0) {
                    thisNode.addPolarization(newPolVer);
                } else {
                    newPolVer = env.makePosition(newPolVer.getCoordinate(0) / newPolVerModule, newPolVer.getCoordinate(1) / newPolVerModule);
                    if (ascend) {
                        thisNode.addPolarization(newPolVer);
                    } else {
                        thisNode.addPolarization(env.makePosition(
                                -newPolVer.getCoordinate(0), 
                                -newPolVer.getCoordinate(1))
                                );
                    }
                }
            }
        }
    }

    private P weightedAverageVectors(final List<Node<Double>> list, final CellNode<P> thisNode) {
        P res = env.makePosition(0, 0);
        final P thisNodePos = env.getPosition(thisNode);
        for (final Node<Double> n : list) {
            final P nPos = env.getPosition(n);
            P vecTemp = env.makePosition(
                    nPos.getCoordinate(0) - thisNodePos.getCoordinate(0),
                    nPos.getCoordinate(1) - thisNodePos.getCoordinate(1));
            final double vecTempModule = FastMath.sqrt(FastMath.pow(vecTemp.getCoordinate(0), 2) + FastMath.pow(vecTemp.getCoordinate(1), 2));
            vecTemp = env.makePosition(
                    n.getConcentration(biomol) * (vecTemp.getCoordinate(0) / vecTempModule), 
                    n.getConcentration(biomol) * (vecTemp.getCoordinate(1) / vecTempModule));
            res = env.makePosition(
                    res.getCoordinate(0) + vecTemp.getCoordinate(0),
                    res.getCoordinate(1) + vecTemp.getCoordinate(1));
        }
        return res;
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CellNode<P> getNode() {
        return (CellNode<P>) super.getNode();
    }

}
