/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

import java.util.Collection;
import java.util.Objects;

class IsolineImpl implements Isoline {

    private final Number value;
    private final Collection<Segment2D> segments;

    IsolineImpl(final Number value, final Collection<Segment2D> segments) {
        this.value = value;
        this.segments = segments;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public Collection<Segment2D> getSegments() {
        return segments;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IsolineImpl isoline = (IsolineImpl) o;
        return Objects.equals(value, isoline.value)
                && Objects.equals(segments, isoline.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, segments);
    }
}
