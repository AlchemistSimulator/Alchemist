/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.Position;

import java.util.List;

/**
 *         This class provides a chemotaxis implementation for SAPERE, namely,
 *         an agent able to move a molecule towards a specific node.
 * 
 */
public class SAPEREChemotaxis<P extends Position<? extends P>> extends SAPERENeighborAgent<P> {

    private static final long serialVersionUID = -4845100315774422690L;
    private final int o;
    private final ILsaMolecule resp;
    private final ILsaMolecule grad;

    /**
     * Builds a new SAPEREChemotaxis.
     * 
     * @param env
     *            the environment
     * @param node
     *            the node
     * @param response
     *            the molecule to move
     * @param gradient
     *            the molecule template that, once matched, will contain the
     *            node ID where move the molecule
     * @param oPos
     *            the argument number where to search for the node ID
     */
    public SAPEREChemotaxis(final Environment<List<ILsaMolecule>, P> env, final ILsaNode node, final ILsaMolecule response, final ILsaMolecule gradient, final int oPos) {
        super(env, node, response);
        /*
         * Thanks Java, for not having unsigned primitives! -.-
         */
        assert oPos >= 0 : "Argument number must be positive";
        resp = response;
        grad = gradient;
        o = oPos;
    }

    @Override
    public void execute() {
        final int nodeId = getLSAArgumentAsInt(grad, o);
        final ILsaNode dest = (ILsaNode) getEnvironment().getNodeByID(nodeId);
        allocateAndInject(resp, dest);
    }

}
