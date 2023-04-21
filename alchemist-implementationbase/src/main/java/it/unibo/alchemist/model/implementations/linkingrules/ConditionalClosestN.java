/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import java.util.Objects;

import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;

/**
 * A {@link ClosestN} rule that also checks that a {@link Molecule} has a
 * specific concentration before allowing the connection.
 * 
 * @param <T> Concentration type
 * @param <P> {@link Position} type
 */
public final class ConditionalClosestN<T, P extends Position<P>> extends ClosestN<T, P> {

    private static final long serialVersionUID = 1L;
    private final Molecule molecule;
    private final T value;

    /**
     * @param n
     *            number of neighbors
     * @param expectedNodes
     *            expected number of nodes (used for optimization purposes)
     * @param mol
     *            the molecule whose concentration will be used to identify
     *            active nodes
     * @param value
     *            the value that identifies an active node
     */
    public ConditionalClosestN(final int n, final int expectedNodes, final Molecule mol, final T value) {
        super(n, expectedNodes);
        this.molecule = Objects.requireNonNull(mol);
        this.value = Objects.requireNonNull(value);
    }

    /**
     * @param n
     *            number of neighbors
     * @param mol
     *            the molecule whose concentration will be used to identify
     *            active nodes
     * @param value
     *            the value that identifies an active node
     */
    public ConditionalClosestN(final int n, final Molecule mol, final T value) {
        this(n,  0, mol, value);
    }

    @Override
    protected boolean nodeIsEnabled(final Node<T> node) {
        return value.equals(node.getConcentration(molecule));
    }

}
