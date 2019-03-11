/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 *
 * @param <T>
 */
public class SetLocalMoleculeConcentration<T> extends AbstractActionOnSingleMolecule<T> {

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
    public Action<T> cloneAction(final Node<T> n, final Reaction<T> r) {
        return new SetLocalMoleculeConcentration<T>(n, getMolecule(), val);
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
