/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.monitors.impl;

import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

import javax.annotation.Nonnull;

/**
 * {@code OutputMonitor} that monitors the current
 * {@link it.unibo.alchemist.core.interfaces.Simulation#getStep() steps} of the {@code Simulation}.
 *
 * @param <T> The type which describes the {@link it.unibo.alchemist.model.interfaces.Concentration} of a molecule
 * @param <P> The position type
 */
public class FXTimeMonitor<T, P extends Position<? extends P>> extends AbstractNumericLabelMonitor<Time, T, P> {
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Default constructor.
     */
    public FXTimeMonitor() {
        super(new DoubleTime(0d), "Time: ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finished(@Nonnull final Environment<T, P> environment, @Nonnull final Time time, final long step) {
        update(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stepDone(
            @Nonnull final Environment<T, P> environment,
            final Reaction<T> reaction,
            @Nonnull final Time time,
            final long step
    ) {
        update(time);
    }
}
