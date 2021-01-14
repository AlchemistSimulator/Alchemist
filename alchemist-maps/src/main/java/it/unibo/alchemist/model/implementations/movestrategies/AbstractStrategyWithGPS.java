/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.movestrategies;

import java.util.Objects;

import it.unibo.alchemist.model.interfaces.GPSTrace;
import it.unibo.alchemist.model.interfaces.ObjectWithGPS;

/**
 * basic move strategy that use a {@link GPSTrace}.
 */
public abstract class AbstractStrategyWithGPS implements ObjectWithGPS {

    private GPSTrace trace;

    /**
     * 
     * @return the {@link GPSTrace} used from this strategy
     */
    protected GPSTrace getTrace() {
        return trace;
    }

    @Override
    public final void setTrace(final GPSTrace trace) {
        if (this.trace == null) {
            this.trace = Objects.requireNonNull(trace);
        } else {
            throw new IllegalStateException("The GPS trace can be set only once");
        }
    }

}
