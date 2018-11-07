/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.conditions;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * A condition that is valid iff a molecule has exactly the desired concentration.
 * 
 * @param <T> concentration type
 */
public class MoleculeHasConcentration<T> extends AbstractCondition<T> {

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
    public Condition<T> cloneCondition(final Node<T> n, final Reaction<T> r) {
        return new MoleculeHasConcentration<>(n, mol, value);
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
