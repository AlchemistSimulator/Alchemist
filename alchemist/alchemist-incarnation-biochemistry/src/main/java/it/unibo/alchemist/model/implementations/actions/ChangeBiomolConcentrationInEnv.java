/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Action implementing the changing of the concentration of a given biomolecule in environment.
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
    public ChangeBiomolConcentrationInEnv(final Environment<Double> environment, final Node<Double> node, final Biomolecule biomol, 
            final double deltaCon, final RandomGenerator randomGen) {
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
        this(environment, node, biomol, -1, randomGen);
    }

    @Override
    public Action<Double> cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInEnv(n, biomolecule, env, rand);
    }

    @Override
    public void execute() {
        // get the environment surrounding
        final List<EnvironmentNode> environmentNodesSurrounding = getEnvironmentNodesSurrounding();
        // if the node is an EnvironmentNode...
        if (getNode().getClass().equals(EnvironmentNodeImpl.class)) {
            // sort the env node randomly
            Collections.shuffle(environmentNodesSurrounding);
            if (delta < 0) {
                double deltaTemp = delta;
                for (final EnvironmentNode n : environmentNodesSurrounding) {
                    final double nodeConcentration = n.getConcentration(biomolecule);
                    // if nodeConcentration >= |deltaTemp|, remove the a delta quantity of the biomol only from this node
                    if (nodeConcentration >= FastMath.abs(deltaTemp)) {
                        n.setConcentration(biomolecule, nodeConcentration + deltaTemp);
                        break;
                        // else, remove all molecule of that species from that node and go on till deltaTemp is smaller than nodeConcetration
                    } else {
                        deltaTemp = deltaTemp + nodeConcentration;
                        n.removeConcentration(biomolecule);
                    }
                }
            } else {
                // if delta > 0, simply add delta to the first node of the list (which has been sorted randomly)
                final Node<Double> target = environmentNodesSurrounding.get(0);
                target.setConcentration(biomolecule, target.getConcentration(biomolecule) + delta);
            }
        } else {
            // if getNode() instanceof CellNode, check if all nodes are at the same distance
            final boolean areAllEnvNodesAtTheSameDistance = environmentNodesSurrounding.stream()
                    .mapToDouble(n -> {
                        return env.getDistanceBetweenNodes(getNode(), n);
                    })
                    .distinct()
                    .count() == 1;
            if (areAllEnvNodesAtTheSameDistance) {
                // if they are, check if they have all the same concentration of the biomolecule
                final boolean haveAllNodeTheSameConcentration = environmentNodesSurrounding.stream()
                        .mapToDouble(n -> n.getConcentration(biomolecule))
                        .distinct()
                        .count() == 1;
                if (haveAllNodeTheSameConcentration) {
                    // if they have, sort the list randomly
                    Collections.shuffle(environmentNodesSurrounding);
                } else {
                    // else, sort the list by the concentration of the biomolecule
                    environmentNodesSurrounding.sort(
                            (n1, n2) -> Double.compare(
                                    n1.getConcentration(biomolecule), 
                                    n2.getConcentration(biomolecule)
                                    )
                            );
                }
            } else {
                // else, sort the list by the distance from the node
                environmentNodesSurrounding.sort(
                        (n1, n2) -> Double.compare(
                                env.getDistanceBetweenNodes(getNode(), n1), 
                                env.getDistanceBetweenNodes(getNode(), n2)
                                )
                        );
            }
            if (delta < 0) {
                double deltaTemp = delta;
                for (final EnvironmentNode n : environmentNodesSurrounding) {
                    final double nodeConcentration = n.getConcentration(biomolecule);
                    // if nodeConcentration >= |deltaTemp|, remove the a delta quantity of the biomol only from this node
                    if (nodeConcentration >= FastMath.abs(deltaTemp)) {
                        n.setConcentration(biomolecule, nodeConcentration + deltaTemp);
                        break;
                        // else, remove all molecule of that species from that node and go on till deltaTemp is smaller than nodeConcetration
                    } else {
                        deltaTemp = deltaTemp + nodeConcentration;
                        n.removeConcentration(biomolecule);
                    }
                }
            } else {
                // if delta > 0, simply add delta to the first node of the list (which has been sorted randomly)
                final Node<Double> target = environmentNodesSurrounding.get(0);
                target.setConcentration(biomolecule, target.getConcentration(biomolecule) + delta);
            }
        }
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    /**
     * 
     * @return a list containing the environment nodes around
     */
    @SuppressWarnings("unchecked")
    protected List<EnvironmentNode> getEnvironmentNodesSurrounding() {
        return (List<EnvironmentNode>) env.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .collect(Collectors.toList());
    }
}
