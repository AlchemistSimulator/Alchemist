/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.Position;

import java.util.List;

/**
 *         This class provides a chemotaxis implementation for SAPERE, namely,
 *         an agent able to move a molecule towards a specific node.
 *
 * @param <P> position type
 */
public final class SAPEREChemotaxis<P extends Position<P>> extends SAPERENeighborAgent<P> {

    private static final long serialVersionUID = -4845100315774422690L;
    private final int idPosition;
    private final ILsaMolecule response;
    private final ILsaMolecule gradient;

    /**
     * Builds a new SAPEREChemotaxis.
     * 
     * @param environment
     *            the environment
     * @param node
     *            the node
     * @param response
     *            the molecule to move
     * @param gradient
     *            the molecule template that, once matched, will contain the
     *            node ID where move the molecule
     * @param idPosition
     *            the argument number where to search for the node ID
     */
    public SAPEREChemotaxis(
            final Environment<List<ILsaMolecule>, P> environment,
            final ILsaNode node,
            final ILsaMolecule response,
            final ILsaMolecule gradient,
            final int idPosition
    ) {
        super(environment, node, response);
        /*
         * Thanks Java, for not having unsigned primitives! -.-
         */
        assert idPosition >= 0 : "Argument number must be positive";
        this.response = response;
        this.gradient = gradient;
        this.idPosition = idPosition;
    }

    @Override
    public void execute() {
        final int nodeId = getLSAArgumentAsInt(gradient, idPosition);
        final ILsaNode dest = (ILsaNode) getEnvironment().getNodeByID(nodeId);
        allocateAndInject(response, dest);
    }

}
