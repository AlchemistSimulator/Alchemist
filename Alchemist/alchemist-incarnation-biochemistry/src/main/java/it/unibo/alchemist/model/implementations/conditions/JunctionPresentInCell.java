/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Node;

/**
 */
public class JunctionPresentInCell extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 4213307452790768059L;

    /**
     * 
     * @param junName the name of the junction
     * @param node the node
     */
    public JunctionPresentInCell(final String junName, final Node<Double> node) {
        super(null, Double.NaN, node); // TODO This is just a stub
    }

}
