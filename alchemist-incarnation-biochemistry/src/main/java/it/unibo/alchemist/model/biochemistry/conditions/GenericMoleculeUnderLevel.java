/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions;

import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

import java.io.Serial;

import static arrow.core.OptionKt.getOrElse;

/**
 * @param <T> the concentration type
 */
public final class GenericMoleculeUnderLevel<T extends Number> extends
    GenericMoleculePresent<T> {

    @Serial
    private static final long serialVersionUID = -5646651431692309010L;

    /**
     * @param mol      the molecule
     * @param n        the node
     * @param quantity how many molecules should be present
     */
    public GenericMoleculeUnderLevel(final Node<T> n, final Molecule mol, final T quantity) {
        super(n, mol, quantity);
        setUpObservability();
    }

    @Override
    public GenericMoleculeUnderLevel<T> cloneCondition(final Node<T> newNode, final Reaction<T> newReaction) {
        return new GenericMoleculeUnderLevel<>(newNode, getMolecule(), getQuantity());
    }

    /**
     * This condition validity is true if the concentration of the molecule is lower the value;
     * the propensity influence computed as max(0, T-[M]), where T is the threshold chosen
     * and [M] is the current concentration of the molecule.
     */
    private void setUpObservability() {
        final var dep = getNode().observeConcentration(getMolecule());
        final double qty = getQuantity().doubleValue();

        addObservableDependency(dep);

        setValidity(dep.map(newValue ->
            getOrElse(newValue, () -> Double.NEGATIVE_INFINITY).doubleValue() < qty
        ));

        setPropensity(dep.map(newValue ->
            Math.max(0, qty - getOrElse(newValue, () -> 0.0).doubleValue())
        ));
    }
}
