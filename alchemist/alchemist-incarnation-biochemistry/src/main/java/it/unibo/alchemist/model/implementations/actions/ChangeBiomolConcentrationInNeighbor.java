/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class ChangeBiomolConcentrationInNeighbor extends AbstractNeighborAction<Double> {

    private static final long serialVersionUID = -6262967512444676061L;

    private final Biomolecule mol;
    private final double delta;
    private final Environment<Double> env;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;

    /**
     * 
     * @param biomol 
     * @param deltaConcentration 
     * @param node 
     * @param environment 
     * @param randGen 
     */
    public ChangeBiomolConcentrationInNeighbor(final Biomolecule biomol,
            final Double deltaConcentration,
            final Node<Double> node,
            final Environment<Double> environment,
            final RandomGenerator randGen) {
        super(node, environment, randGen);
        addModifiedMolecule(biomol);
        mol = biomol;
        delta = deltaConcentration;
        env = environment;
        rand = randGen;
    }

    @Override
    public ChangeBiomolConcentrationInNeighbor cloneOnNewNode(final Node<Double> n, final Reaction<Double> r) {
        return new ChangeBiomolConcentrationInNeighbor(mol, delta, n, env, rand);
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
