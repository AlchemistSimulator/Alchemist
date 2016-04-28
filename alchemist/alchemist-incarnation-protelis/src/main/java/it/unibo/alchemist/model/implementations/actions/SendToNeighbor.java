/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

import java.util.Objects;

/**
 */
public class SendToNeighbor extends AbstractLocalAction<Object> {

    private static final long serialVersionUID = -8826563176323247613L;
    private final ProtelisNode myNode;
    private final RunProtelisProgram prog;

    /**
     * @param node
     *            the local node
     * @param program
     *            the reference {@link RunProtelisProgram}
     */
    public SendToNeighbor(final ProtelisNode node, final RunProtelisProgram program) {
        super(node);
        Objects.requireNonNull(program);
        prog = program;
        myNode = node;
    }

    @Override
    public SendToNeighbor cloneOnNewNode(final Node<Object> n, final Reaction<Object> r) {
        return new SendToNeighbor((ProtelisNode) n, prog);
    }

    @Override
    public void execute() {
        final AlchemistNetworkManager mgr = myNode.getNetworkManager(prog);
        Objects.requireNonNull(mgr);
        mgr.simulateMessageArrival();
        prog.prepareForComputationalCycle();
    }

    @Override
    public ProtelisNode getNode() {
        return myNode;
    }

    /**
     * @return the {@link RunProtelisProgram} whose data will be sent
     */
    public RunProtelisProgram getProtelisProgram() {
        return prog;
    }

}
