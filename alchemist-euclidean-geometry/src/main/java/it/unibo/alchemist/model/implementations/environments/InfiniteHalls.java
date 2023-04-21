/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments;

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Incarnation;

/**
 * @param <T> concentration type
 */
public final class InfiniteHalls<T> extends LimitedContinuos2D<T> {

    private static final long serialVersionUID = -7481116157809902856L;
    private static final double DEFAULT_SIZE = 10;
    private static final double[] BORDERS = { 0.89, 0.39, 0.51, 0.09, 0.81 };
    private static final int EXI = 0, CII = 1, CFI = 2, SII = 3, SFI = 4;
    private final double s, ex, ci, cf, si, sf;
    private boolean doorsOpen;

    /**
     * Default builder. Builds halls of size 10 with open doors.
     * @param incarnation the incarnation to be used.
     */
    public InfiniteHalls(final Incarnation<T, Euclidean2DPosition> incarnation) {
        this(incarnation, DEFAULT_SIZE);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param size
     *            the size of a single hall
     */
    public InfiniteHalls(final Incarnation<T, Euclidean2DPosition> incarnation, final double size) {
        this(incarnation, size, true);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param allOpen
     *            sets all the doors to open.
     */
    public InfiniteHalls(final Incarnation<T, Euclidean2DPosition> incarnation, final boolean allOpen) {
        this(incarnation, DEFAULT_SIZE, allOpen);
    }

    /**
     * @param incarnation the incarnation to be used.
     * @param size
     *            the size of a single hall
     * @param allOpen
     *            sets all the doors to open.
     */
    public InfiniteHalls(final Incarnation<T, Euclidean2DPosition> incarnation, final double size, final boolean allOpen) {
        super(incarnation);
        doorsOpen = allOpen;
        s = size;
        ex = size * BORDERS[EXI];
        ci = size * BORDERS[CII];
        cf = size * BORDERS[CFI];
        si = size * BORDERS[SII];
        sf = size * BORDERS[SFI];
    }

    /**
     * @return the hall size
     */
    public double getHallSize() {
        return s;
    }

    /**
     * @return external border margin
     */
    public double getEx() {
        return ex;
    }

    /**
     * @return Central corridor initial margin
     */
    public double getCi() {
        return ci;
    }

    /**
     * @return Central corridor initial margin
     */
    public double getCf() {
        return cf;
    }

    /**
     * @return Other corridor initial margin
     */
    public double getSi() {
        return si;
    }

    /**
     * @return Other corridor initial margin
     */
    public double getSf() {
        return sf;
    }

    /**
     * Checks whether a position is allowed or not.
     * 
     * @param xp
     *            the x coordinate
     * @param yp
     *            the y coordinate
     * @return true if the position is allowed
     */
    public boolean allowed(final double xp, final double yp) {
        final double x = (xp % s + s) % s;
        final double y = (yp % s + s) % s;
        return x >= ex // Right corridor
            || y >= ex // Upper corridor
            || x >= ci && x <= cf // Vertical lane
            || doorsOpen && y >= ci && y <= cf // Horizontal lane
            || x >= si && x <= sf && y >= si && y <= sf; // Room
    }

    @Override
    protected boolean isAllowed(final Euclidean2DPosition p) {
        final double[] coord = p.getCoordinates();
        return allowed(coord[0], coord[1]);
    }

    @Override
    protected Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        if (allowed(nx, ny)) {
            return makePosition(nx - ox, ny - oy);
        }
        final int snx = (int) (ox / s);
        final int sny = (int) (oy / s);
        final double oxm = ox % s;
        final double oym = oy % s;
        final double x = (nx % s + s) % s;
        final double y = (ny % s + s) % s;
        double nxm = nx;
        double nym = ny;
        if (oxm >= ex) {
            // Right corridor
            if (x < ex) {
                nxm = snx * s + ex;
            } else if (x > s) {
                nxm = snx * s + s;
            }
        } else if (oym >= ex) {
            // Upper corridor
            if (y < ex) {
                nym = sny * s + ex;
            } else if (y > s) {
                nym = sny * s + s;
            }
        } else if (oxm >= si && oxm <= sf && oym <= si && oxm <= sf) {
            // Main room
            if (x < si) {
                nxm = snx * s + si;
            } else if (x > sf) {
                nxm = snx * s + sf;
            }
            if (y < si) {
                nym = sny * s + si;
            } else if (y > sf) {
                nym = sny * s + sf;
            }
        } else if (doorsOpen && oym >= ci && oym <= cf) {
            // Horizontal lane passes (room positions and corridors already
            // checked)
            if (y > cf) {
                nym = sny * s + cf;
            } else if (y < ci) {
                nym = sny * s + ci;
            }
        } else if (oxm >= ci && oxm <= cf) {
            // Vertical lane passes (room positions and corridors already
            // checked)
            if (x < ci) {
                nxm = snx * s + ci;
            } else if (x > cf) {
                nxm = snx * s + cf;
            }
        }
        return new Euclidean2DPosition(nxm, nym);
    }

    /**
     * @return true if the room has 4 open accesses, false otherwise
     */
    public boolean isDoorsOpen() {
        return doorsOpen;
    }

    /**
     * @param isOpen
     *            must be true if the user wants all the four exits of the room
     *            to be open. If false, only two passages will be available.
     */
    public void setDoorsOpen(final boolean isOpen) {
        doorsOpen = isOpen;
    }

}
