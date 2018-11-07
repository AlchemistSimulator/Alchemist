/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
/*
 * Copyright (C) 2010-2016, Danilo Pianini and contributors


 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.Biomolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
/**
 *
 */
public final class ChangeBiomolConcentrationInCell extends AbstractActionOnSingleMolecule<Double> {

    private static final long serialVersionUID = 5569613886926615012L;
    private final double deltaC;

    /**
     * 
     * @param biomolecule the molecule
     * @param deltaConcentration the change in concentration
     * @param node the node
     */
    public ChangeBiomolConcentrationInCell(final Node<Double> node, final Biomolecule biomolecule, final double deltaConcentration) {
        super(node, biomolecule);
        this.deltaC = deltaConcentration;
    }

    @Override
    public ChangeBiomolConcentrationInCell cloneAction(final Node<Double> n, final Reaction<Double> r) {
        throw new UnsupportedOperationException("cloneOnNewNode(Node, Reaction) has to be implemented in: " + getClass());
    }

    @Override
    public void execute() {
        super.getNode().setConcentration(getMolecule(), super.getNode().getConcentration(getMolecule()) + deltaC);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL; 
    }

    @Override
    public String toString() {
         if (deltaC > 0) {
             return "add " + deltaC + " of " + getMolecule() + " ";
         }  else {
             return "remove " + (-deltaC) + " of " + getMolecule() + " ";
         }
    }

}
