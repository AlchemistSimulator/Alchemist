/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

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
}
