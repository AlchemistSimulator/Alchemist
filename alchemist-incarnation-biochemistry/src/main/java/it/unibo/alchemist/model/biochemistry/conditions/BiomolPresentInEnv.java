/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Layer;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.biochemistry.EnvironmentNode;
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule;
import it.unibo.alchemist.model.observation.Observable;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import javax.annotation.Nullable;
import java.io.Serial;

/**
 * @param <P> Position type
 */
public final class BiomolPresentInEnv<P extends Position<? extends P>> extends GenericMoleculePresent<Double> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Environment<Double, P> environment;

    /**
     * Initialize condition for extracellular environment, implemented as a set
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
        setUpObservability();
    }

    @Override
    public BiomolPresentInEnv<P> cloneCondition(final Node<Double> newNode, final Reaction<Double> newReaction) {
        return new BiomolPresentInEnv<>(environment, newNode, getBiomolecule(), getQuantity());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    private void setUpObservability() {
        final Observable<Double> totalQuantity = observeTotalQuantity();
        addObservableDependency(totalQuantity);
        setValidity(totalQuantity.map(totalQty -> totalQty >= getQuantity()));
        setPropensity(totalQuantity.map(totalQty -> {
            if (totalQty < getQuantity()) {
                return 0d;
            }
            return CombinatoricsUtils.binomialCoefficientDouble(
                (int) FastMath.round(totalQty),
                (int) FastMath.round(getQuantity())
            );
        }));
    }

    private Observable<Double> observeTotalQuantity() {
        return environment.getNeighborhood(getNode()).mergeWith(
            environment.observePosition(getNode()),
            (neighborhood, position) -> {
                final double quantityInEnvNodes = neighborhood.getNeighbors().stream()
                    .parallel()
                    .filter(EnvironmentNode.class::isInstance)
                    .mapToDouble(n -> n.getConcentration(getBiomolecule()))
                    .sum();
                double quantityInLayers = 0;
                final @Nullable Layer<Double, P> layer = environment.getLayer(getBiomolecule());
                if (layer != null) {
                    quantityInLayers = layer.getValue(position);
                }
                return quantityInEnvNodes + quantityInLayers;
            }
        );
    }

    private Biomolecule getBiomolecule() {
        return (Biomolecule) getMolecule();
    }
}
