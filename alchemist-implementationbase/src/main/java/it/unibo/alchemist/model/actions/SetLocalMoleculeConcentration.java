/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Action;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

/**
 *
 * @param <T> concentration type
 */
public final class SetLocalMoleculeConcentration<T> extends AbstractActionOnSingleMolecule<T> {

    private static final long serialVersionUID = -197253027556270645L;
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

    @Override
    public Action<T> cloneAction(final Node<T> node, final Reaction<T> reaction) {
        return new SetLocalMoleculeConcentration<T>(node, getMolecule(), val);
    }

    @Override
    public void execute() {
        getNode().setConcentration(getMolecule(), val);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

}
