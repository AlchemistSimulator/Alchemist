/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.implementations.nodes.EnvironmentNodeImpl;
/**
 *
 */
public class BiomolPresentInEnv extends GenericMoleculePresent<Double> {

    private static final long serialVersionUID = 6527297022655890961L;

    /**
     * 
     * @param biomol 
     * @param concentration 
     * @param node 
     */
    public BiomolPresentInEnv(final Biomolecule biomol, final Double concentration, final EnvironmentNodeImpl node) {
        super(biomol, node, concentration);
    }

    @Override
    public EnvironmentNodeImpl getNode() {
        return (EnvironmentNodeImpl) super.getNode();
    }

}