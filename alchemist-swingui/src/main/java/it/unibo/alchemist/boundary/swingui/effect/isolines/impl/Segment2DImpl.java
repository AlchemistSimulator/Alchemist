/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.swingui.effect.isolines.impl;

import it.unibo.alchemist.boundary.swingui.effect.isolines.api.Segment2D;

import java.util.Objects;

class Segment2DImpl implements Segment2D {

    private final Number x1;
    private final Number y1;
    private final Number x2;
    private final Number y2;

    Segment2DImpl(final Number x1, final Number y1, final Number x2, final Number y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }


    @Override
    public Number getX1() {
        return x1;
    }

    @Override
    public Number getY1() {
        return y1;
    }

    @Override
    public Number getX2() {
        return x2;
    }

    @Override
    public Number getY2() {
        return y2;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Segment2DImpl segment2D = (Segment2DImpl) o;
        return Objects.equals(x1, segment2D.x1)
                && Objects.equals(y1, segment2D.y1)
                && Objects.equals(x2, segment2D.x2)
                && Objects.equals(y2, segment2D.y2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, x2, y2);
    }
}
