/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments;

import it.unibo.alchemist.model.positions.Euclidean2DPosition;
import it.unibo.alchemist.model.Incarnation;
import it.unibo.alchemist.model.Node;

import javax.annotation.Nonnull;

/**
 * @param <T> concentration type
 */
public final class MuseumHall<T> extends Continuous2DEnvironment<T> {

    private static final long serialVersionUID = 585211392057392723L;
    private static final int SIZE = 10;
    private static final int LOWER = 3;
    private static final int LOWROOM = 4;
    private static final int UPPER = 9;
    private static final int CENTRALUP = 7;
    private static final int CENTRALDOWN = 6;

    /**
     * @param incarnation the incarnation
     */
    public MuseumHall(final Incarnation<T, Euclidean2DPosition> incarnation) {
        super(incarnation);
    }

    /**
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @return true if the position is inside an allowed area, false if it's
     *         part of the wall.
     */
    private static boolean isAllowed(final double x, final double y) {
        if (x <= 0 || x >= SIZE || y <= 0 || y >= SIZE) {
            return false;
        }
        if (x <= LOWER && y >= 1 && y <= LOWER) {
            return false;
        }
        if (x >= LOWROOM && x <= UPPER && y >= 1 && y <= LOWROOM) {
            return false;
        }
        if (x >= CENTRALUP && x <= UPPER && y >= 1 && y <= CENTRALDOWN) {
            return false;
        }
        return !(x >= CENTRALUP && x <= UPPER && y >= CENTRALUP);
    }

    /**
     * Given the old position of a node and the requested one, this method
     * computes the nearest allowed point.
     * 
     * @param ox
     *            old x coordinate
     * @param oy
     *            old y coordinate
     * @param nx
     *            requested x coordinate
     * @param ny
     *            requested y coordinate
     * @return the allowed point nearest to the requested one
     */
    private Euclidean2DPosition next(final double ox, final double oy, final double nx, final double ny) {
        return nextAllowed(ox, oy, nx, ny);
    }

    /**
     * Given the old position of a node and the requested one, this method
     * computes the nearest allowed point. Static method.
     * 
     * @param ox
     *            old x coordinate
     * @param oy
     *            old y coordinate
     * @param nx
     *            requested x coordinate
     * @param ny
     *            requested y coordinate
     * @return the allowed point nearest to the requested one
     */
    private Euclidean2DPosition nextAllowed(final double ox, final double oy, final double nx, final double ny) {
        if (isAllowed(nx, ny)) {
            makePosition(nx - ox, ny - oy);
        }
        double nxm = nx;
        double nym = ny;
        // Main room
        if (ox >= 0 && ox <= CENTRALUP && oy <= SIZE && oy >= LOWER) {
            if (nx < 0) {
                nxm = 0;
            } else if (nx > CENTRALUP && ny < CENTRALDOWN || ny > CENTRALUP) {
                nxm = CENTRALUP;
            }
            if (ny > SIZE) {
                nym = SIZE;
            } else if (ny < LOWER && nx < LOWER || nx > LOWROOM) {
                nym = LOWER;
            }
            // Upper corridor
        } else if (oy >= 0 && oy <= 1 && ox >= 0 && ox <= SIZE) {
            if (nx > SIZE) {
                nxm = SIZE;
            }
            if (ny > 1) {
                if (nx < LOWER || nx > LOWROOM) {
                    nym = 1;
                }
            } else if (ny < 0) {
                nym = 0;
                // Right corridor
            }
        } else if (ox >= UPPER && ox <= SIZE && oy >= 0 && oy <= SIZE) {
            if (nx > SIZE) {
                nxm = SIZE;
            } else if (nx < UPPER && ny < CENTRALDOWN || nx > CENTRALUP) {
                nxm = UPPER;
            }
            if (ny < 0) {
                nym = 0;
                // Upper passage
            }
        } else if (ox >= LOWER && ox <= LOWROOM && oy <= LOWER && oy >= 1) {
            if (nx < LOWER) {
                nxm = LOWER;
            } else if (nx > LOWROOM) {
                nxm = LOWROOM;
                // Right passage
            }
        } else if (ox >= CENTRALUP && ox <= UPPER && oy <= CENTRALUP && oy >= CENTRALDOWN) {
            if (ny < CENTRALDOWN) {
                nym = CENTRALDOWN;
            }
            if (ny > CENTRALUP) {
                nym = CENTRALUP;
            }
        }
        return new Euclidean2DPosition(nxm - ox, nym - oy);
    }

    @Override
    public void moveNode(@Nonnull final Node<T> node, final Euclidean2DPosition direction) {
        final Euclidean2DPosition cur = getPosition(node);
        final double ox = cur.getCoordinates()[0];
        final double oy = cur.getCoordinates()[1];
        double nx = direction.getCoordinates()[0] + ox;
        double ny = direction.getCoordinates()[1] + oy;
        if (ox >= 0 && oy <= SIZE) {
            final Euclidean2DPosition next = next(ox, oy, nx, ny);
            nx = next.getCoordinates()[0] + ox;
            ny = next.getCoordinates()[1] + oy;
            if (nx < 1.0 && ny < 1.0 || nx > UPPER && ny > UPPER) {
                removeNode(node);
            } else {
                super.moveNode(node, next);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final Node<T> n : getNodes()) {
            builder.append(getPosition(n)).append(' ').append(n).append('\n');
        }
        return builder.toString();
    }

}
