/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.Objects;

/**
 * Base class for actions involving network communication in Protelis.
 */
public abstract class AbstractProtelisNetworkAction extends AbstractAction<Object> {
    private static final long serialVersionUID = 1L;
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
    public AbstractProtelisNetworkAction(final ProtelisNode<?> node, final Reaction<Object> reaction, final RunProtelisProgram<?> program) {
        super(node);
        this.reaction = Objects.requireNonNull(reaction);
        prog = Objects.requireNonNull(program);
        declareDependencyTo(program.asMolecule());
    }

    @Override
    public final Context getContext() {
        return Context.NEIGHBORHOOD;
    }

    @Override
    public final ProtelisNode<?> getNode() {
        return (ProtelisNode<?>) super.getNode();
    }

    /**
     * @return the {@link RunProtelisProgram} whose data will be sent
     */
    public RunProtelisProgram<?> getProtelisProgram() {
        return prog;
    }

    /**
     * @return the {@link Reaction} this action belongs to
     */
    public Reaction<Object> getReaction() {
        return reaction;
    }
}
