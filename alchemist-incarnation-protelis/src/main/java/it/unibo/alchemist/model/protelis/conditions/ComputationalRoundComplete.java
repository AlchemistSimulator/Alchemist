/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis.conditions;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;
import it.unibo.alchemist.model.conditions.AbstractCondition;
import it.unibo.alchemist.model.observation.Observable;
import it.unibo.alchemist.model.protelis.actions.RunProtelisProgram;
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice;

import java.io.Serial;
import java.util.List;

/**
 */
public final class ComputationalRoundComplete extends AbstractCondition<Object> {

    @Serial
    private static final long serialVersionUID = -4113718948444451107L;

    private final RunProtelisProgram<?> program;

    /**
     * @param node
     *            the local node
     * @param program
     *            the reference {@link RunProtelisProgram}
     */
    public ComputationalRoundComplete(final Node<Object> node, final RunProtelisProgram<?> program) {
        super(node);
        this.program = program;
        declareDependencyOn(this.program.asMolecule());
        addObservableDependency(program.getObserveComputationalCycleComplete());
    }

    @Override
    public Observable<Boolean> observeValidity() {
        return getProgram().getObserveComputationalCycleComplete();
    }

    @Override
    public Observable<Double> observePropensityContribution() {
        return observeValidity().map(it -> it ? 1d : 0d);
    }

    @Override
    public ComputationalRoundComplete cloneCondition(final Node<Object> node, final Reaction<Object> reaction) {
        final ProtelisDevice<?> device = node.asPropertyOrNull(ProtelisDevice.class);
        if (device != null) {
            final List<RunProtelisProgram<?>> possibleRefs = device.allProtelisPrograms();
            if (possibleRefs.size() == 1) {
                return new ComputationalRoundComplete(node, possibleRefs.get(0));
            }
            throw new IllegalStateException(
                "There must be one and one only unconfigured " + RunProtelisProgram.class.getSimpleName()
            );
        }
        throw new IllegalStateException(
            getClass().getSimpleName() + " cannot get cloned on a node with a missing " + ProtelisDevice.class.getSimpleName()
        );
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    /**
     * @return the {@link RunProtelisProgram} action this condition is mapped to
     */
    public RunProtelisProgram<?> getProgram() {
        return program;
    }

    @Override
    public String toString() {
        return program.asMolecule().getName() + " completed round";
    }
}
