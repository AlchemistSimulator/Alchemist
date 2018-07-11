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
package it.unibo.alchemist.model.implementations.conditions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.CombinatoricsUtils;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.CellNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * 
 *
 */
public class BiomolPresentInNeighbor extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 499903479123400111L;

    private final Biomolecule mol;
    private final Double conc;
    private double propensity;
    private Map<Node<Double>, Double> neigh = new LinkedHashMap<>();

    /**
     * 
     * @param molecule 
     * @param concentration 
     * @param node 
     * @param env 
     */
    public BiomolPresentInNeighbor(final Environment<Double, ?> env, final Node<Double> node, final Biomolecule molecule, final Double concentration) {
        super(env, node);
        addReadMolecule(molecule);
        mol = molecule;
        conc = concentration;
    }

    @Override
    public double getPropensityConditioning() {
        return propensity;
    }

    @Override
    public boolean isValid() {
        if (neigh.isEmpty()) {
            return false;
        } else {
            final Neighborhood<Double> neighborhood = getEnvironment().getNeighborhood(getNode());
            return neigh.entrySet().stream()
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
    public Map<Node<Double>, Double> getValidNeighbors(final Collection<? extends Node<Double>> neighborhood) {
        propensity = 0;
        neigh = neighborhood.stream()
                .filter(n -> n instanceof CellNode && n.getConcentration(mol) >= conc)
                .collect(Collectors.<Node<Double>, Node<Double>, Double>toMap(
                        n -> n,
                        n -> CombinatoricsUtils.binomialCoefficientDouble(n.getConcentration(mol).intValue(), conc.intValue())));
        if (!neigh.isEmpty()) {
            propensity = neigh.values().stream().max((d1, d2) -> d1.compareTo(d2)).get();
        }
        return new LinkedHashMap<>(neigh);
    }

    @Override
    public String toString() {
        return mol.toString() + " >= " + conc + " in neighbor";
    }

}
