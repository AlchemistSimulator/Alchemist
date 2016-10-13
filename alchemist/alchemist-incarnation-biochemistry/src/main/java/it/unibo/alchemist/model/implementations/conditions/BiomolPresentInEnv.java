/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Node;
/**
 *
 */
public class BiomolPresentInEnv extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 1L;

    private final Environment<Double> environment;

    /**
     * Initialize condition for extra-cellular environment, implemented as a set
     * of {@link EnvironmentNode}.
     * 
     * @param biomol
     *            the {@link Biomolecule} which the condition is about.
     * @param conc
     *            the requested concentration.
     * @param node
     *            the node where this condition is located;
     * @param env
     *            the {@link Environment} where the node is located.
     */
    public BiomolPresentInEnv(final Environment<Double> env, final Node<Double> node, final Biomolecule biomol, final Double conc) {
        super(node, biomol, conc);
        environment = env;
    }

    @Override
    public double getPropensityConditioning() {
        double quantityInEnvNodes = 0;
        if (!getEnviromentNodesSurrounding().isEmpty()) {
            quantityInEnvNodes = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                    .sum();
        }
        double quantityInLayers = 0;
        if (environment.getLayer(getBiomolecule()).isPresent()) {
            quantityInLayers = environment.getLayer(getBiomolecule()).get().getValue(environment.getPosition(getNode()));
        }
        final double totalQuantity = quantityInEnvNodes + quantityInLayers;
        if (totalQuantity < getQuantity()) {
            return 0;
        }
        return CombinatoricsUtils.binomialCoefficientDouble(
                (int) FastMath.round(totalQuantity), 
                (int) FastMath.round(getQuantity())
                );
    }

    /**
     * @return a list of EnvironmentNodes near to the node where this condition is located.
     */
    protected final List<Node<Double>> getEnviromentNodesSurrounding() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .collect(Collectors.toList());
    }

    @Override 
    public BiomolPresentInEnv cloneOnNewNode(final Node<Double> n) {
        return new BiomolPresentInEnv(environment, n, getBiomolecule(), getQuantity());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public boolean isValid() {
        double quantityInEnvNodes = 0;
        if (!getEnviromentNodesSurrounding().isEmpty()) {
            quantityInEnvNodes = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                    .sum();
        }
        double quantityInLayers = 0;
        if (environment.getLayer(getBiomolecule()).isPresent()) {
            quantityInLayers = environment.getLayer(getBiomolecule()).get().getValue(environment.getPosition(getNode()));
        }
        final double totalQuantity = quantityInEnvNodes + quantityInLayers;
        return totalQuantity >= getQuantity();
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }

}