/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;

import java.util.List;


/**
 * The count N of neighbors containing "person" is stored within a tuple of the
 * form "crowd, N".
 * 
 */ 
public final class CrowdSensor extends SAPERELocalAgent {

    private static final long serialVersionUID = -647690735880121675L;
    private static final ILsaMolecule PERSON = new LsaMolecule("person");
    private static final ILsaMolecule CROWD = new LsaMolecule("crowd, Level");
    private final Environment<List<ILsaMolecule>, ?> env;

    /**
     * @param environment
     *            the current environment
     * @param node
     *            the current node
     */
    public CrowdSensor(final Environment<List<ILsaMolecule>, ?> environment, final ILsaNode node) {
        super(node);
        env = environment;
    }

    @Override
    public void execute() {

        final Neighborhood<List<ILsaMolecule>> neigh = env.getNeighborhood(getNode());
        int personNumber = 0;

        for (final Node<List<ILsaMolecule>> node : neigh.getNeighbors()) {
            final ILsaNode n = (ILsaNode) node;
            if (n.getConcentration(PERSON).size() != 0) {
                personNumber++;
            }
        }

        final List<ILsaMolecule> localGradList = getNode().getConcentration(CROWD);
        if (!localGradList.isEmpty()) {
            getNode().removeConcentration(CROWD);
        }

        if (personNumber > 0) {
            final ILsaMolecule molNew = new LsaMolecule("crowd, " + personNumber);
            getNode().setConcentration(molNew);
        }

    }

}
