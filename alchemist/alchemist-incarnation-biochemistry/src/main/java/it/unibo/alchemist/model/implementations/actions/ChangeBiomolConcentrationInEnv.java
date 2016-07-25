/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class ChangeBiomolConcentrationInEnv extends AbstractActionOnSingleMolecule<Double> {

    private static final long serialVersionUID = -1442417685847647706L;
    private final double delta;
    private final Environment<Double> env;

    /**
     * Initialize a new {@link Action} that change concentration of the given
     * {@link Biomolecule} of a quantity {@link deltaCon}.
     * 
     * @param node the {@link Node} where this action is located.
     * @param biomol the {@link Biomolecule} which concentration will be changed.
     * @param deltaCon the quantity to add to actual concentriation of {@link biomol}
     * @param environment the {@link Environment} where the node is located.
     */
    public ChangeBiomolConcentrationInEnv(final Node<Double> node, final Biomolecule biomol, 
            final double deltaCon, final Environment<Double> environment) {
        super(node, biomol);
        delta = deltaCon;
        env = environment;
    }

    /**
     * Initialize a ChangeBiomolConcentrationInEnv with delta = -1.
     * 
     * @param node node the {@link Node} where this action is located.
     * @param biomol the {@link Biomolecule} which concentration will be changed.
     * @param environment environment the {@link Environment} where the node is located.
     */
    public ChangeBiomolConcentrationInEnv(final Node<Double> node, final Biomolecule biomol, 
            final Environment<Double> environment) {
        this(node, biomol, -1, environment);
    }

    @Override
    public Action<Double> cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInEnv(n, getBiomolecule(), env);
    }

    @Override
    public void execute() {
        if (delta < 0) {
            // molecules follow concentration gradient. So if a delta of a molecule has to be subtracted, 
            // that will happen in the EnvironmentNode with highest concentration of this molecule.
            getEnviromentNodesSurrounding().stream()
            .max((n1, n2) -> Double.compare(n1.getConcentration(getBiomolecule()), n2.getConcentration(getBiomolecule())))
            .ifPresent(n -> {
                if (n.getConcentration(getBiomolecule()) > FastMath.abs(delta)) {
                    n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta);
                } else {
                    n.removeConcentration(getBiomolecule());
                }
            });
        } else {
            // vice versa, if a delta has to be added, that will be added in the EnvironmentNode
            // with lowest concentration.
            getEnviromentNodesSurrounding().stream()
            .min((n1, n2) -> Double.compare(n1.getConcentration(getBiomolecule()), n2.getConcentration(getBiomolecule())))
            .ifPresent(n -> n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta));
        }
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    private List<Node<Double>> getEnviromentNodesSurrounding() {
        final List<Node<Double>> list = new ArrayList<>();
        env
        .getNeighborhood(getNode())
        .getNeighbors()
        .stream()
        .filter(n -> n instanceof EnvironmentNode)
        .forEach(n -> list.add(n));
        return list;
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }
}
