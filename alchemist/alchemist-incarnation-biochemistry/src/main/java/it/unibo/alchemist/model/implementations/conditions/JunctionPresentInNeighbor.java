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
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Node;

/**
 */
public class JunctionPresentInNeighbor extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 2577950013175200416L;

    /**
     * 
     * @param junction 
     * @param node 
     * @param env 
     */
    public JunctionPresentInNeighbor(final Junction junction, final Node<Double> node, final Environment<Double> env) {
        super(null, node, Double.NaN); // TODO This is just a stub
    }
}
