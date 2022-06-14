/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.EnvironmentNode;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * @param <P> Position type
 */
public final class BiomolPresentInEnv<P extends Position<? extends P>> extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 1L;

    private final Environment<Double, P> environment;

    /**
     * Initialize condition for extra-cellular environment, implemented as a set
     * of {@link EnvironmentNode}.
     * 
     * @param biomolecule
     *            the {@link Biomolecule} which the condition is about.
     * @param concentration
     *            the requested concentration.
     * @param node
     *            the node where this condition is located;
     * @param environment
     *            the {@link Environment} where the node is located.
     */
    public BiomolPresentInEnv(
        final Environment<Double, P> environment,
        final Node<Double> node,
        final Biomolecule biomolecule,
        final Double concentration
    ) {
        super(node, biomolecule, concentration);
        this.environment = environment;
    }

    @Override
    public double getPropensityContribution() {
        final double totalQuantity = getTotalQuantity();
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
    protected List<Node<Double>> getEnviromentNodesSurrounding() {
        return environment.getNeighborhood(getNode()).getNeighbors().stream()
                .parallel()
                .filter(n -> n instanceof EnvironmentNode)
                .collect(Collectors.toList());
    }

    @Override 
    public BiomolPresentInEnv<P> cloneCondition(final Node<Double> node, final Reaction<Double> r) {
        return new BiomolPresentInEnv<>(environment, node, getBiomolecule(), getQuantity());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public boolean isValid() {
        return getTotalQuantity() >= getQuantity();
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }

    private double getTotalQuantity() {
        double quantityInEnvNodes = 0;
        if (!getEnviromentNodesSurrounding().isEmpty()) {
            quantityInEnvNodes = getEnviromentNodesSurrounding().stream()
                    .parallel()
                    .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                    .sum();
        }
        double quantityInLayers = 0;
        final Optional<Layer<Double, P>> layer = environment.getLayer(getBiomolecule());
        if (layer.isPresent()) {
            quantityInLayers = layer.get().getValue(environment.getPosition(getNode()));
        }
        return quantityInEnvNodes + quantityInLayers;
    }

}
