/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.isolines.impl;

import it.unibo.alchemist.boundary.swingui.effect.isolines.api.Isoline;
import it.unibo.alchemist.boundary.swingui.effect.isolines.api.IsolinesFactory;
import it.unibo.alchemist.boundary.swingui.effect.isolines.api.IsolinesFinder;
import it.unibo.alchemist.boundary.swingui.effect.isolines.api.Segment2D;

import java.util.Collection;
import java.util.Objects;

/**
 */
public class ConcreteIsolinesFactory implements IsolinesFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Segment2D makeSegment(final Number x1, final Number y1, final Number x2, final Number y2) {
        Objects.requireNonNull(x1);
        Objects.requireNonNull(y1);
        Objects.requireNonNull(x2);
        Objects.requireNonNull(y2);
        return new Segment2DImpl(x1, y1, x2, y2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Isoline makeIsoline(final Number value, final Collection<Segment2D> segments) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(segments);
        return new IsolineImpl(value, segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IsolinesFinder makeIsolinesFinder(final IsolineFinders algorithm) {
        if (algorithm == IsolineFinders.CONREC) {
            return new ConrecIsolinesFinder(this);
        }
        throw new IllegalArgumentException(algorithm + " not available");
    }
}
