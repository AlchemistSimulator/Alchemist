/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;


import java.util.List;
import java.util.stream.Collectors;

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
        System.out.println("entrato1, delta = " + delta);
        // add delta to the nearest node.
        if (delta < 0) {
            double deltaTemp = delta;
            final List<Node<Double>> l = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .filter(n -> n.contains(getBiomolecule()))
                    .sorted((n1, n2) -> Double.compare(
                            env.getPosition(n1).getDistanceTo(env.getPosition(getNode())), 
                            env.getPosition(n2).getDistanceTo(env.getPosition(getNode()))
                            ))
                    .collect(Collectors.toList());
            for(Node<Double> n : l) {
                if (n.getConcentration(getBiomolecule()) > FastMath.abs(delta)) {
                    n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta);
                    deltaTemp = 0;
                } else {
                    n.removeConcentration(getBiomolecule());
                    deltaTemp = n.getConcentration(getBiomolecule()) + deltaTemp;
                    if (deltaTemp == 0) {
                        break;
                    }
                }
            }
        } else {
            System.out.println("entrato2, delta = " + delta);
            final boolean allEnvNodesAreAtTheSameDistance = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .mapToDouble(n -> env.getDistanceBetweenNodes(n, getNode()))
                    .count() == 1;
            System.out.println("allEnvNodesAreAtTheSameDistance = " + allEnvNodesAreAtTheSameDistance);
            if (allEnvNodesAreAtTheSameDistance && getEnviromentNodesSurrounding().size() != 1) {
                System.out.println("Sono entrato nel primo");
                getEnviromentNodesSurrounding().stream()
                .parallel()
                .min((n1, n2) -> Double.compare(
                        n1.getConcentration(getBiomolecule()), 
                        n2.getConcentration(getBiomolecule())
                        ))
                .ifPresent(n -> n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta));
            } else {
                System.out.println("Sono entrato nel secondo");
                getEnviromentNodesSurrounding().stream()
                .parallel()
                .min((n1, n2) -> Double.compare(
                        env.getPosition(n1).getDistanceTo(env.getPosition(getNode())), 
                        env.getPosition(n2).getDistanceTo(env.getPosition(getNode()))
                        ))
                .ifPresent(n -> { 
                    System.out.println("conA = " + n.getConcentration(getBiomolecule()));
                    n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta);
                    System.out.println("conA = " + n.getConcentration(getBiomolecule()));
                });
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * 
     * @return a list of EnvironmentNodes near to the node where this condition is located.
     */
    protected final List<Node<Double>> getEnviromentNodesSurrounding() {
        return env.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .collect(Collectors.toList());
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }
}
