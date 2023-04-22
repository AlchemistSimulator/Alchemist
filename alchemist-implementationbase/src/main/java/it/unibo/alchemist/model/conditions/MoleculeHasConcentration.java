/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.conditions;

import it.unibo.alchemist.model.Condition;
import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

import java.util.Objects;

/**
 * A condition that is valid iff a molecule has exactly the desired concentration.
 * 
 * @param <T> concentration type
 */
public final class MoleculeHasConcentration<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = 1L;
    private final Molecule mol;
    private final T value;

    /**
     * @param node
     *            the node
     * @param molecule
     *            the target molecule
     * @param value
     *            the desired concentration
     */
    public MoleculeHasConcentration(final Node<T> node, final Molecule molecule, final T value) {
        super(node);
        this.mol = Objects.requireNonNull(molecule);
        this.value = Objects.requireNonNull(value);
        declareDependencyOn(this.mol);
    }

    @Override
    public Condition<T> cloneCondition(final Node<T> node, final Reaction<T> reaction) {
        return new MoleculeHasConcentration<>(node, mol, value);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public double getPropensityContribution() {
        return isValid() ? 1 : 0;
    }

    @Override
    public boolean isValid() {
        return value.equals(getNode().getConcentration(mol));
    }

    @Override
    public String toString() {
        return mol + "=" + value + "?[" + isValid() + "]";
    }

}
