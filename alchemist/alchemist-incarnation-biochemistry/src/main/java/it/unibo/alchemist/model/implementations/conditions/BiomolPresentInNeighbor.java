/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.util.FastMath;

import java.util.Optional;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble;

/**
 * 
 *
 */
public final class BiomolPresentInNeighbor extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 499903479123400111L;

    private final Biomolecule mol;
    private final Double conc;

    /**
     * 
     * @param molecule the molecule to check
     * @param concentration the minimum concentration
     * @param node the local node
     * @param env the environment
     */
    public BiomolPresentInNeighbor(
            final Environment<Double, ?> env,
            final Node<Double> node,
            final Biomolecule molecule,
            final Double concentration) {
        super(env, node);
        declareDependencyOn(molecule);
        mol = molecule;
        conc = concentration;
    }

    @Override
    public boolean isValid() {
        if (getValidNeighbors().isEmpty()) {
            return false;
        } else {
            final Neighborhood<Double> neighborhood = getEnvironment().getNeighborhood(getNode());
            return getValidNeighbors().entrySet().stream()
                    .filter(n -> n.getKey() instanceof CellNode)
                    .allMatch(n -> neighborhood.contains(n.getKey()) 
                            && n.getKey().getConcentration(mol) >=  conc);
        }
    }

    @Override
    public BiomolPresentInNeighbor cloneCondition(final Node<Double> n, final Reaction<Double> r) {
        return new BiomolPresentInNeighbor(getEnvironment(), n, mol, conc);
    }

    @Override
    protected double getNeighborPropensity(final Node<Double> neighbor) {
        // the neighbor is eligible, its propensity is computed using the concentration of the biomolecule
        return Optional.of(neighbor)
                .filter(it -> it instanceof CellNode)
                .map(it -> it.getConcentration(mol))
                .filter(it -> it >= conc)
                .map(it -> binomialCoefficientDouble(it.intValue(), (int) FastMath.ceil(conc)))
                .orElse(0d);
    }

    @Override
    public String toString() {
        return mol.toString() + " >= " + conc + " in neighbor";
    }

}
