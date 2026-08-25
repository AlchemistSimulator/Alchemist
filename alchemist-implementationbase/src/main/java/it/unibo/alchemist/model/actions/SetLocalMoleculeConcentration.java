/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.NodeReaction;

import javax.annotation.Nonnull;

/**
 * @param <T> concentration type
 */
public final class SetLocalMoleculeConcentration<T> extends AbstractActionOnSingleMolecule<T> {

    private final T val;

    /**
     * @param node
     *            The node to which this action belongs
     * @param target
     *            the molecule whose concentration will be modified
     * @param value
     *            the new concentration value for the molecule
     */
    public SetLocalMoleculeConcentration(final Node<T> node, final Molecule target, final T value) {
        super(node, target);
        this.val = value;
    }

    @Nonnull
    @Override
    public Action<T> cloneAction(@Nonnull final Node<T> node, @Nonnull final NodeReaction<T> reaction) {
        return new SetLocalMoleculeConcentration<>(node, getMolecule(), val);
    }

    @Override
    public void execute() {
        getNode().setConcentration(getMolecule(), val);
    }
}
