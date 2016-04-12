/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * 
 * @param <T>
 */
public class GenericMoleculePresent<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = -4341823735340536583L;

//    private final Molecule mol;
//    private final T concentration;
//    private final Node<T> node;

    /**
     * Creates the generic molecule present condition.
     * @param molecule a molecule
     * @param concentration the concentration of the molecule
     * @param node the node
     */
    public GenericMoleculePresent(final Molecule molecule, final T concentration, final Node<T> node) {
        super(node);
        /*this.mol = molecule;
        this.concentration = concentration;
        this.node = node;*/
    }

    @Override
    public Condition<T> cloneOnNewNode(final Node<T> n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Context getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getPropensityConditioning() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }
}
