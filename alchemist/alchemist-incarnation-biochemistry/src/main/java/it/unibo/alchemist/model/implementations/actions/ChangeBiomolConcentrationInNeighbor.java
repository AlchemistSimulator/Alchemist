/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public final class ChangeBiomolConcentrationInNeighbor extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = -6262967512444676061L;

    private final Biomolecule mol;
    private final double delta;

    /**
     * 
     * @param biomolecule the molecule
     * @param deltaConcentration concentration change
     * @param node the node
     * @param environment the environment
     * @param randGen the random generator
     */
    public ChangeBiomolConcentrationInNeighbor(final Environment<Double, ?> environment,
            final Node<Double> node,
            final Biomolecule biomolecule,
            final RandomGenerator randGen,
            final Double deltaConcentration) {
        super(node, environment, randGen);
        declareDependencyTo(biomolecule);
        mol = biomolecule;
        delta = deltaConcentration;
    }

    @Override
    public ChangeBiomolConcentrationInNeighbor cloneAction(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInNeighbor(getEnvironment(), n, mol, getRandomGenerator(), delta);
    }

    @Override
    public void execute() {
        final Neighborhood<Double> neighborhood = getEnvironment().getNeighborhood(getNode());
        final List<Integer> validTargetsIds = new ArrayList<>();
        if (delta < 0) {
            neighborhood.getNeighbors().stream()
            .filter(n -> n instanceof CellNode && n.getConcentration(mol) >= delta)
            .mapToInt(Node::getId)
            .forEach(validTargetsIds::add);
        } else {
            neighborhood.getNeighbors().stream()
            .filter(n -> n instanceof CellNode && n.getConcentration(mol) >= delta)
            .mapToInt(Node::getId)
            .forEach(validTargetsIds::add);
        }
        if (!validTargetsIds.isEmpty()) {
            final int targetId = validTargetsIds.get(getRandomGenerator().nextInt(validTargetsIds.size()));
            execute(neighborhood.getNeighborByNumber(targetId));
        }
    }

    @Override
    public void execute(final Node<Double> targetNode) {
        targetNode.setConcentration(mol, targetNode.getConcentration(mol) + delta);
    }

    @Override
    public String toString() {
         if (delta > 0) {
             return "add " + delta + " of " + mol + " in neighbor ";
         }  else {
             return "remove " + (-delta) + " of " + mol + " in neighbor ";
         }
    }

}
