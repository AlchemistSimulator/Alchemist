/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.capabilities.ProtelisCapability;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 */
public final class SendToNeighbor extends AbstractAction<Object> {

    private static final long serialVersionUID = -8826563176323247613L;
    private final RunProtelisProgram<?> program;
    private final Reaction<Object> reaction;

    /**
     * @param node
     *            the local node
     * @param reaction
     *            the reaction
     * @param program
     *            the reference {@link RunProtelisProgram}
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "This is intentional")
    public SendToNeighbor(final Node<Object> node, final Reaction<Object> reaction, final RunProtelisProgram<?> program) {
        super(node);
        this.reaction = Objects.requireNonNull(reaction);
        this.program = Objects.requireNonNull(program);
        declareDependencyTo(program.asMolecule());
    }

    @Override
    public SendToNeighbor cloneAction(final Node<Object> node, final Reaction<Object> reaction) {
        if (node.asCapabilityOrNull(ProtelisCapability.class) != null) {
            final List<RunProtelisProgram<?>> possibleRefs = node.getReactions().stream()
                    .map(Reaction::getActions)
                    .flatMap(List::stream)
                    .filter(a -> a instanceof RunProtelisProgram)
                    .map(a -> (RunProtelisProgram<?>) a)
                    .collect(Collectors.toList());
            if (possibleRefs.size() == 1) {
                return new SendToNeighbor(node, this.reaction, possibleRefs.get(0));
            }
            throw new IllegalStateException(
                    "There must be one and one only unconfigured " + RunProtelisProgram.class.getSimpleName()
            );
        }
        throw new IllegalStateException(getClass().getSimpleName() + " cannot get cloned on a node with a missing "
                + ProtelisCapability.class.getSimpleName());
    }

    @Override
    public Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public void execute() {
        final AlchemistNetworkManager mgr = getNode().asCapability(ProtelisCapability.class).getNetworkManager(program);
        Objects.requireNonNull(mgr);
        mgr.simulateMessageArrival(reaction.getTau().toDouble());
        program.prepareForComputationalCycle();
    }

    @Override
    public Node<Object> getNode() {
        return super.getNode();
    }

    /**
     * @return the {@link RunProtelisProgram} whose data will be sent
     */
    public RunProtelisProgram<?> getProtelisProgram() {
        return program;
    }

    @Override
    public String toString() {
        return "broadcast " + program.asMolecule().getName() + " data";
    }
}
