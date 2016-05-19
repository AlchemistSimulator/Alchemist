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
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.CombinatoricsUtils;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * 
 *
 */
public class BiomolPresentInNeighbor extends AbstractNeighborCondition<Double> {

    private static final long serialVersionUID = 499903479123400111L;

    private final Biomolecule mol;
    private final Double conc;
    private final Environment<Double> environment;
    private boolean valid;
    private double propensity;

    /**
     * 
     * @param molecule 
     * @param concentration 
     * @param node 
     * @param env 
     */
    public BiomolPresentInNeighbor(final Biomolecule molecule, final Double concentration, final Node<Double> node, final Environment<Double> env) {
        super(node, env);
        addReadMolecule(molecule);
        mol = molecule;
        conc = concentration;
        environment = env;
    }

    @Override
    public double getPropensityConditioning() {
        return propensity;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public BiomolPresentInNeighbor cloneOnNewNode(final Node<Double> n) {
        return new BiomolPresentInNeighbor(mol, conc, n, environment);
    }

    @Override
    public Map<Node<Double>, Double> getValidNeighbors(final Collection<? extends Node<Double>> neighborhood) {
        final Map<Node<Double>, Double> map = neighborhood.stream()
                    .filter(n -> n.getConcentration(mol) >= conc)
                    .collect(Collectors.<Node<Double>, Node<Double>, Double>toMap(
                                          n -> n,
                                          n -> CombinatoricsUtils.binomialCoefficientDouble(n.getConcentration(mol).intValue(), conc.intValue())));
        if (map.isEmpty()) {
            valid = false;
            propensity = 0;
        } else {
            valid = true;
            propensity = map.values().stream().max((d1, d2) -> d1.compareTo(d2)).get();
        }
        return map;
    }

    @Override
    public String toString() {
        return mol.toString() + " >= " + conc + " in neighbor";
    }

}
