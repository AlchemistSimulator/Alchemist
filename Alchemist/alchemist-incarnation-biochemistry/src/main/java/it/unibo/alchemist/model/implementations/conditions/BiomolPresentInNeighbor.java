/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * 
 *
 */
public class BiomolPresentInNeighbor extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 499903479123400111L;

    /**
     * 
     * @param molecule 
     * @param concentration 
     * @param node 
     * @param env 
     */
    public BiomolPresentInNeighbor(final Molecule molecule, final Double concentration, final Node<Double> node, final Environment<Double> env) {
        super(molecule, node, concentration);
        // TODO Auto-generated constructor stub
    }

}
