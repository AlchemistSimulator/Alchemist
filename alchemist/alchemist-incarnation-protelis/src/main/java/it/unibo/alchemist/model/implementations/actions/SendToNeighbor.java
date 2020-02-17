/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.ns3.AlchemistNs3;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

import java.util.Objects;

/**
 */
public final class SendToNeighbor extends AbstractProtelisNetworkAction {

    private static final long serialVersionUID = -4202725911872207702L;

    /**
     * @param node
     *            the local node
     * @param reaction
     *            the reaction
     * @param program
     *            the reference {@link RunProtelisProgram}
     */
    public SendToNeighbor(final ProtelisNode<?> node, final Reaction<Object> reaction, final RunProtelisProgram<?> program) {
        super(node, reaction, program);
    }

    @Override
    public void execute() {
        final AlchemistNetworkManager mgr = getNode().getNetworkManager(prog);
        Objects.requireNonNull(mgr);
        boolean realistic = false;
        final var inc = prog.getEnvironment().getIncarnation();
        if (inc.isPresent() && inc.get() instanceof ProtelisIncarnation) {
            if (AlchemistNs3.getInstance() != null) {
                realistic = true;
            }
        }
        mgr.simulateMessageArrival(reaction.getTau().toDouble(), realistic);
        prog.prepareForComputationalCycle();
    }

    @Override
    public String toString() {
        return "broadcast " + prog.asMolecule().getName() + " data";
    }
}
