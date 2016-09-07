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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class ChangeBiomolConcentrationInEnv extends AbstractAction<Double> {

    private static final long serialVersionUID = -1442417685847647706L;
    private final double delta;
    private final Biomolecule biomolecule;
    private final Environment<Double> env;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;

    /**
     * Initialize a new {@link Action} that change concentration of the given
     * {@link Biomolecule} of a quantity {@link deltaCon}.
     * 
     * @param node the {@link Node} where this action is located.
     * @param biomol the {@link Biomolecule} which concentration will be changed.
     * @param deltaCon the quantity to add to actual concentriation of {@link biomol}
     * @param environment the {@link Environment} where the node is located.
     * @param randomGen 
     */
    public ChangeBiomolConcentrationInEnv(final Node<Double> node, final Biomolecule biomol, 
            final double deltaCon, final Environment<Double> environment, final RandomGenerator randomGen) {
        super(node);
        biomolecule = biomol;
        delta = deltaCon;
        env = environment;
        rand = randomGen;
    }

    /**
     * Initialize a ChangeBiomolConcentrationInEnv with delta = -1.
     * 
     * @param node node the {@link Node} where this action is located.
     * @param biomol the {@link Biomolecule} which concentration will be changed.
     * @param environment environment the {@link Environment} where the node is located.
     * @param randomGen 
     */
    public ChangeBiomolConcentrationInEnv(final Node<Double> node, final Biomolecule biomol, 
            final Environment<Double> environment, final RandomGenerator randomGen) {
        this(node, biomol, -1, environment, randomGen);
    }

    @Override
    public Action<Double> cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInEnv(n, getBiomolecule(), env, rand);
    }

    @Override
    public void execute() {
        // add delta to the nearest node.
        if (delta < 0) {
            double deltaTemp = delta;
            final List<Node<Double>> l = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .filter(n -> n.contains(getBiomolecule()))
                    .sorted((n1, n2) -> {
                        if (getNode() instanceof CellNode) {
                            return Double.compare(
                                    env.getPosition(n1).getDistanceTo(env.getPosition(getNode())), 
                                    env.getPosition(n2).getDistanceTo(env.getPosition(getNode())));
                        } else if (getNode().getClass().equals(EnvironmentNodeImpl.class)) {
                            int testVar = rand.nextInt(3) - 1;
                            System.out.println(testVar);
                            return testVar;
                        } else {
                            throw new UnsupportedOperationException(
                                    "Neighborhood can be composed only by CellNodes "
                                    + "or EnvironmentNodes; the node found is of type : "
                                            + getNode().getClass().getName()
                                    );
                        }
                    })
                    .collect(Collectors.toList());
            for (final Node<Double> n : l) {
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
            if (getNode().getClass().equals(EnvironmentNodeImpl.class)) {
                getEnviromentNodesSurrounding().stream()
                .parallel()
                .sorted((n1, n2) -> rand.nextInt(2) - 1)
                .findFirst()
                .ifPresent(n -> n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta));
            } else {
                final boolean allEnvNodesAreAtTheSameDistance = getEnviromentNodesSurrounding().stream()
                        .parallel()
                        .mapToDouble(n -> env.getDistanceBetweenNodes(n, getNode()))
                        .count() == 1;
                if (allEnvNodesAreAtTheSameDistance && getEnviromentNodesSurrounding().size() != 1) {
                    getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .min((n1, n2) -> Double.compare(
                            n1.getConcentration(getBiomolecule()), 
                            n2.getConcentration(getBiomolecule())
                            ))
                    .ifPresent(n -> n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta));
                } else {
                    getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .min((n1, n2) -> Double.compare(
                            env.getPosition(n1).getDistanceTo(env.getPosition(getNode())), 
                            env.getPosition(n2).getDistanceTo(env.getPosition(getNode()))
                            ))
                    .ifPresent(n -> { 
                        n.setConcentration(getBiomolecule(), n.getConcentration(getBiomolecule()) + delta);
                    });
                }
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
        return this.biomolecule;
    }
}
