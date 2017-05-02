/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.conditions;

import java.util.List;
import java.util.stream.Collectors;

import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 */
public class ComputationalRoundComplete extends AbstractCondition<Object> {

    private static final long serialVersionUID = -4113718948444451107L;
    private final RunProtelisProgram program;

    /**
     * @param node
     *            the local node
     * @param prog
     *            the reference {@link RunProtelisProgram}
     */
    public ComputationalRoundComplete(final ProtelisNode node, final RunProtelisProgram prog) {
        super(node);
        program = prog;
        addReadMolecule(program);
    }

    @Override
    public ComputationalRoundComplete cloneCondition(final Node<Object> n, final Reaction<Object> r) {
        final List<RunProtelisProgram> possibleRefs = n.getReactions().stream()
                .map(Reaction::getActions)
                .flatMap(List::stream)
                .filter(a -> a instanceof RunProtelisProgram)
                .map(a -> (RunProtelisProgram) a)
                .collect(Collectors.toList());
        if (possibleRefs.size() == 1) {
            return new ComputationalRoundComplete((ProtelisNode) n, possibleRefs.get(0));
        }
        throw new IllegalStateException("There must be one and one only unconfigured " + RunProtelisProgram.class.getSimpleName());
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public double getPropensityConditioning() {
        return isValid() ? 1 : 0;
    }

    @Override
    public boolean isValid() {
        return program.isComputationalCycleComplete();
    }

    @Override
    public ProtelisNode getNode() {
        return (ProtelisNode) super.getNode();
    }

}
