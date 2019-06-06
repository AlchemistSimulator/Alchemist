/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

/**
 */
public final class SendToNeighbor extends AbstractAction<Object> {

    private static final long serialVersionUID = -8826563176323247613L;
    private final RunProtelisProgram<?> prog;
    private final Reaction<Object> reaction;

    /**
     * @param node
     *            the local node
     * @param reaction
     *            the reaction
     * @param program
     *            the reference {@link RunProtelisProgram}
     */
    public SendToNeighbor(final ProtelisNode node, final Reaction<Object> reaction, final RunProtelisProgram<?> program) {
        super(node);
        this.reaction = Objects.requireNonNull(reaction);
        prog = Objects.requireNonNull(program);
        declareDependencyTo(program.asMolecule());
    }

    @Override
    public SendToNeighbor cloneAction(final Node<Object> n, final Reaction<Object> r) {
        if (n instanceof ProtelisNode) {
            final List<RunProtelisProgram<?>> possibleRefs = n.getReactions().stream()
                    .map(Reaction::getActions)
                    .flatMap(List::stream)
                    .filter(a -> a instanceof RunProtelisProgram)
                    .map(a -> (RunProtelisProgram<?>) a)
                    .collect(Collectors.toList());
            if (possibleRefs.size() == 1) {
                return new SendToNeighbor((ProtelisNode) n, reaction, possibleRefs.get(0));
            }
            throw new IllegalStateException("There must be one and one only unconfigured " + RunProtelisProgram.class.getSimpleName());
        }
        throw new IllegalStateException(getClass().getSimpleName() + " cannot get cloned on a node of type "
                + n.getClass().getSimpleName());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public void execute() {
        final AlchemistNetworkManager mgr = getNode().getNetworkManager(prog);
        Objects.requireNonNull(mgr);
        mgr.simulateMessageArrival(reaction.getTau().toDouble());
        prog.prepareForComputationalCycle();
    }

    @Override
    public ProtelisNode getNode() {
        return (ProtelisNode) super.getNode();
    }

    /**
     * @return the {@link RunProtelisProgram} whose data will be sent
     */
    public RunProtelisProgram<?> getProtelisProgram() {
        return prog;
    }

    @Override
    public String toString() {
        return "broadcast " + prog.asMolecule().getName() + " data";
    }
}
