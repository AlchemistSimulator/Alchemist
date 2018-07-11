/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * This class offers the basic structures to provide operations with numeric
 * concentrations on a single molecule.
 * 
 * @param <T>
 */
public abstract class AbstractActionOnSingleMolecule<T> extends AbstractAction<T> {

    private static final long serialVersionUID = 5506733553861927362L;
    private final Molecule mol;

    /**
     * Call this constructor in the subclasses in order to automatically
     * instance the node, the molecules and the dependency managing facilities.
     * 
     * @param node
     *            the node this action belongs to
     * @param molecule
     *            the molecule which whose concentration will be modified y the
     *            execution of this action
     */
    protected AbstractActionOnSingleMolecule(final Node<T> node,
            final Molecule molecule) {
        super(node);
        this.mol = molecule;
        addModifiedMolecule(molecule);
    }

    /**
     * @return the molecule which whose concentration will be modified y the
     *         execution of this action
     */
    public Molecule getMolecule() {
        return mol;
    }

}
