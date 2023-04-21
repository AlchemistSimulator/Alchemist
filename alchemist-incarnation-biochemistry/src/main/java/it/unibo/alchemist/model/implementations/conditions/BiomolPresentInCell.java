/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
/**
 *
 */
public class BiomolPresentInCell extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = -5772829360637946655L;

    /**
     * 
     * @param biomol the molecule
     * @param concentration the concentration
     * @param node the node
     */
    public BiomolPresentInCell(final Node<Double> node, final Molecule biomol, final Double concentration) {
        super(node, biomol, concentration);
    }

}
