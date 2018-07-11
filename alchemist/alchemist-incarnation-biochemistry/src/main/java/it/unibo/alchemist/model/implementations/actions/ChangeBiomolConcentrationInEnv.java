/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
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
import java.util.stream.Stream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Action implementing the changing of the concentration of a given biomolecule in environment.
 */
public class ChangeBiomolConcentrationInEnv extends AbstractRandomizableAction<Double> {

    private static final long serialVersionUID = 1L;
    private final double delta;
    private final Biomolecule biomolecule;
    private final Environment<Double, ?> env;

    /**
     * Initialize a new {@link Action} that change concentration of the given
     * {@link Biomolecule} of a "deltaCon" quantity.
     * 
     * @param node the {@link Node} where this action is located.
     * @param biomol the {@link Biomolecule} which concentration will be changed.
     * @param deltaCon the quantity to add to actual concentration of {@link Biomolecule}
     * @param environment the {@link Environment} where the node is located.
     * @param randomGen 
     */
    public ChangeBiomolConcentrationInEnv(final Environment<Double, ?> environment, final Node<Double> node, final Biomolecule biomol, 
            final double deltaCon, final RandomGenerator randomGen) {
        super(node, randomGen);
        if (node instanceof EnvironmentNode || node instanceof CellNode) {
            biomolecule = biomol;
            delta = deltaCon;
            env = environment;
        } else {
            throw  new UnsupportedOperationException("This condition can be set only in EnvironmentNode and CellNode");
        }
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
            final Environment<Double, ?> environment, final RandomGenerator randomGen) {
        this(environment, node, biomol, -1, randomGen);
    }

    @Override
    public Action<Double> cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInEnv(n, biomolecule, env, getRandomGenerator());
    }

    @Override
    public void execute() {
     // declaring a variable for the node where this action is set, to have faster access
        final Node<Double> thisNode = getNode();
        // get the environment surrounding
        final List<EnvironmentNode> environmentNodesSurrounding = getEnvironmentNodesSurrounding();
        // if the node is an EnvironmentNode...
        if (thisNode instanceof EnvironmentNode) {
            // sort the env node randomly
            changeConcentrationInRandomNodes(environmentNodesSurrounding);
        } else {
            // if getNode() instanceof CellNode, check if all nodes are at the same distance
            final boolean areAllEnvNodesAtTheSameDistance = environmentNodesSurrounding.stream()
                    .mapToDouble(n -> env.getDistanceBetweenNodes(thisNode, n))
                    .distinct()
                    .count() == 1;
            if (areAllEnvNodesAtTheSameDistance) {
                // if they are, check if they have all the same concentration of the biomolecule
                final boolean haveAllNodeTheSameConcentration = environmentNodesSurrounding.stream()
                        .mapToDouble(n -> n.getConcentration(biomolecule))
                        .distinct()
                        .count() == 1;
                if (haveAllNodeTheSameConcentration) {
                    // if they have, pick up from the list randomly
                    changeConcentrationInRandomNodes(environmentNodesSurrounding);
                } else {
                    // else, sort the list by the concentration of the biomolecule
                    environmentNodesSurrounding.sort(
                            (n1, n2) -> Double.compare(
                                    n1.getConcentration(biomolecule), 
                                    n2.getConcentration(biomolecule)
                                    )
                            );
                    changeConcentrationInSortedNodes(environmentNodesSurrounding);
                }
            } else {
                // else, sort the list by the distance from the node
                environmentNodesSurrounding.sort(
                        (n1, n2) -> Double.compare(
                                env.getDistanceBetweenNodes(thisNode, n1), 
                                env.getDistanceBetweenNodes(thisNode, n2)
                                )
                        );
                changeConcentrationInSortedNodes(environmentNodesSurrounding);
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
    protected List<EnvironmentNode> getEnvironmentNodesSurrounding() {
        return (List<EnvironmentNode>) env.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .flatMap(n -> n instanceof EnvironmentNode ? Stream.of((EnvironmentNode) n) : Stream.empty())
                .collect(Collectors.toList());
    }

    private void changeConcentrationInSortedNodes(final List<EnvironmentNode> envNodesSurrounding) {
        if (delta < 0) {
            double deltaTemp = delta;
            for (final EnvironmentNode n : envNodesSurrounding) {
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
            final Node<Double> target = envNodesSurrounding.get(0);
            target.setConcentration(biomolecule, target.getConcentration(biomolecule) + delta);
        }
    }

    private void changeConcentrationInRandomNodes(final List<EnvironmentNode> envNodesSurrounding) {
        if (delta < 0) {
            double deltaTemp = delta;
            while (deltaTemp < 0) {
                final int index = getRandomGenerator().nextInt(envNodesSurrounding.size());
                final EnvironmentNode pickedNode = envNodesSurrounding.get(index);
                final double nodeConcentration = pickedNode.getConcentration(biomolecule);
                // if nodeConcentration >= |deltaTemp|, remove the a delta quantity of the biomol only from this node
                if (nodeConcentration >= FastMath.abs(deltaTemp)) {
                    pickedNode.setConcentration(biomolecule, nodeConcentration + deltaTemp);
                    break;
                    // else, remove all molecule of that species from that node and go on till deltaTemp is smaller than nodeConcetration
                } else {
                    deltaTemp = deltaTemp + nodeConcentration;
                    pickedNode.removeConcentration(biomolecule);
                }
                envNodesSurrounding.remove(index);
            }
        } else {
            // if delta > 0, simply add delta to the first node of the list (which has been sorted randomly)
            final Node<Double> target = envNodesSurrounding.get(getRandomGenerator().nextInt(envNodesSurrounding.size()));
            target.setConcentration(biomolecule, target.getConcentration(biomolecule) + delta);
        }
    }
}
