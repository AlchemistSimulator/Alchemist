/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions;


import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.CellProperty;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Action implementing the changing of the concentration of a given biomolecule in environment.
 */
public final class ChangeBiomolConcentrationInEnv extends AbstractRandomizableAction<Double> {

    private static final long serialVersionUID = 1L;
    private final double delta;
    private final Biomolecule biomolecule;
    private final Environment<Double, ?> environment;

    /**
     * Initialize a new {@link Action} that change concentration of the given
     * {@link Biomolecule} of a "deltaCon" quantity.
     * 
     * @param node the {@link Node} where this action is located.
     * @param biomolecule the {@link Biomolecule} which concentration will be changed.
     * @param deltaCon the quantity to add to actual concentration of {@link Biomolecule}
     * @param environment the {@link Environment} where the node is located.
     * @param randomGen the random generator
     */
    public ChangeBiomolConcentrationInEnv(
            final Environment<Double, ?> environment,
            final Node<Double> node,
            final Biomolecule biomolecule,
            final double deltaCon,
            final RandomGenerator randomGen
    ) {
        super(node, randomGen);
        if (node instanceof EnvironmentNode || node.asPropertyOrNull(CellProperty.class) != null) {
            this.biomolecule = biomolecule;
            delta = deltaCon;
            this.environment = environment;
        } else {
            throw  new UnsupportedOperationException(
                    "This condition can be set only in Node with nodes with " + CellProperty.class.getSimpleName() + " or "
                            + EnvironmentNode.class.getSimpleName()
            );
        }
    }

    /**
     * Initialize a {@link ChangeBiomolConcentrationInEnv} with delta = -1.
     * 
     * @param node node the {@link Node} where this action is located.
     * @param biomolecule the {@link Biomolecule} which concentration will be changed.
     * @param environment environment the {@link Environment} where the node is located.
     * @param randomGen the random generator
     */
    public ChangeBiomolConcentrationInEnv(
            final Node<Double> node,
            final Biomolecule biomolecule,
            final Environment<Double, ?> environment,
            final RandomGenerator randomGen
    ) {
        this(environment, node, biomolecule, -1, randomGen);
    }

    @Override
    public Action<Double> cloneAction(final Node<Double> node, final Reaction<Double> reaction) {
        return new ChangeBiomolConcentrationInEnv(node, biomolecule, environment, getRandomGenerator());
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
                    .mapToDouble(n -> environment.getDistanceBetweenNodes(thisNode, n))
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
                    environmentNodesSurrounding.sort(Comparator.comparingDouble(n -> n.getConcentration(biomolecule)));
                    changeConcentrationInSortedNodes(environmentNodesSurrounding);
                }
            } else {
                // else, sort the list by the distance from the node
                environmentNodesSurrounding.sort(Comparator
                        .comparingDouble(n -> environment.getDistanceBetweenNodes(thisNode, n)));
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
    private List<EnvironmentNode> getEnvironmentNodesSurrounding() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .flatMap(n -> n instanceof EnvironmentNode ? Stream.of((EnvironmentNode) n) : Stream.empty())
                .collect(Collectors.toList());
    }

    private void changeConcentrationInSortedNodes(final List<EnvironmentNode> envNodesSurrounding) {
        if (delta < 0) {
            double deltaTemp = delta;
            for (final EnvironmentNode n : envNodesSurrounding) {
                final double nodeConcentration = n.getConcentration(biomolecule);
                // if nodeConcentration >= |deltaTemp|, remove the a delta quantity of the biomolecule only from this node
                if (nodeConcentration >= FastMath.abs(deltaTemp)) {
                    n.setConcentration(biomolecule, nodeConcentration + deltaTemp);
                    break;
                    // else, remove all molecules of that species from that node and go on
                    // till deltaTemp is smaller than node concentration
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
                // if nodeConcentration >= |deltaTemp|, remove the a delta quantity of the biomolecule only from this node
                if (nodeConcentration >= FastMath.abs(deltaTemp)) {
                    pickedNode.setConcentration(biomolecule, nodeConcentration + deltaTemp);
                    break;
                    /*
                     * else, remove all molecule of that species from that node
                     * and go on till deltaTemp is smaller than node concentration
                     */
                } else {
                    deltaTemp = deltaTemp + nodeConcentration;
                    pickedNode.removeConcentration(biomolecule);
                }
                envNodesSurrounding.remove(index);
            }
        } else {
            // if delta > 0, simply add delta to the first node of the list (which has been sorted randomly)
            final Node<Double> target = envNodesSurrounding
                    .get(getRandomGenerator().nextInt(envNodesSurrounding.size()));
            target.setConcentration(biomolecule, target.getConcentration(biomolecule) + delta);
        }
    }

    @Override
    public String toString() {
        return "add " + delta + " " + biomolecule + " in env ";
    }
}
