/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.implementations.molecules.Junction;
import it.unibo.alchemist.model.interfaces.Node;

/**
 */
public class JunctionPresentInCell extends GenericJunctionPresent {

    private static final long serialVersionUID = 4213307452790768059L;

    /**
     * 
     * @param junction the junction
     * @param node the node
     */
    public JunctionPresentInCell(final Junction junction, final Node<Double> node) {
        super(junction, node); // TODO This is just a stub
    }

}
