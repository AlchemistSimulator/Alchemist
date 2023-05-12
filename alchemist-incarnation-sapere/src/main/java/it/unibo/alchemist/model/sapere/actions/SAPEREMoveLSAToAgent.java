/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.actions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.sapere.ILsaMolecule;
import it.unibo.alchemist.model.sapere.ILsaNode;

/**
 * This agent matches a template, removes a single instance of it from the
 * current node and moves the LSA to another node (specified at creation time).
 * Please note that, since the destination could be anywhere in the system, this
 * action has a GLOBAL {@link Context}, and thus may trigger a large number of
 * updates, slowing down the whole simulation. Handle with care.
 * 
 */
public final class SAPEREMoveLSAToAgent extends SAPEREAgent {

    private static final long serialVersionUID = -8020706131248061313L;
    private final ILsaNode destination;
    private final ILsaMolecule moleculeTemplate;

    /**
     * This is the constructor that should be called from DSL. Dynamically
     * computes the destination node if an id is given.
     * 
     * @param environment
     *            the current environment
     * @param node
     *            the source node, where this action is programmed
     * @param destinationId
     *            the destination node id
     * @param template
     *            the template LSA to match and move
     */
    public SAPEREMoveLSAToAgent(
            final Environment<?, ?> environment,
            final ILsaNode node,
            final int destinationId,
            final ILsaMolecule template
    ) {
        this(node, (ILsaNode) environment.getNodeByID(destinationId), template);
    }

    /**
     * @param node
     *            the source node, where this action is programmed
     * @param destination
     *            the destination node, where this action will move the matched
     *            intance
     * @param template
     *            the template LSA to match and move
     */
    public SAPEREMoveLSAToAgent(final ILsaNode node, final ILsaNode destination, final ILsaMolecule template) {
        super(node, template);
        moleculeTemplate = template;
        this.destination = destination;
    }

    @Override
    public void execute() {
        final ILsaMolecule instance = allocateVarsAndBuildLSA(moleculeTemplate);
        getNode().removeConcentration(instance);
        destination.setConcentration(instance);
    }

    @Override
    public Context getContext() {
        return Context.GLOBAL;
    }

}
