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
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.ns3.AlchemistNs3;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 */
public final class SendToNeighbor extends AbstractProtelisNetworkAction {

    private static final long serialVersionUID = 2L;

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
        final AlchemistNetworkManager mgr = getNode().getNetworkManager(this.getProtelisProgram());
        Objects.requireNonNull(mgr);
        final var incarnation = this.getProtelisProgram().getEnvironment().getIncarnation();
        final boolean realistic = incarnation.isPresent()
                && incarnation.get() instanceof ProtelisIncarnation
                && AlchemistNs3.getInstance() != null;
        mgr.simulateMessageArrival(this.getReaction().getTau().toDouble(), realistic);
        this.getProtelisProgram().prepareForComputationalCycle();
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
                return new SendToNeighbor((ProtelisNode<?>) n, r, possibleRefs.get(0));
            }
            throw new IllegalStateException("There must be one and one only unconfigured " + RunProtelisProgram.class.getSimpleName());
        }
        throw new IllegalStateException(getClass().getSimpleName() + " cannot get cloned on a node of type "
                + n.getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return "broadcast " + this.getProtelisProgram().asMolecule().getName() + " data";
    }
}
