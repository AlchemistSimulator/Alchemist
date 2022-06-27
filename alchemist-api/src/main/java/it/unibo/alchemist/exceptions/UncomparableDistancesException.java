/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.exceptions;

import it.unibo.alchemist.model.interfaces.Position;

/**
 * An exception meant to be thrown when trying to compare incompatible
 * distances.
 * 
 * 
 */
public final class UncomparableDistancesException extends IllegalArgumentException {

    /**
     * Inherited.
     */
    private static final long serialVersionUID = -3794795114548794654L;
    /**
     * The first position.
     */
    private final Position<?> p1;
    /**
     * The second position.
     */
    private final Position<?> p2;

    /**
     * Builds the exception given two positions claimed to be not compatible.
     * 
     * @param pos1
     *            the first position
     * @param pos2
     *            the second position
     * @param cause
     *            the cause for this exception
     */
    public UncomparableDistancesException(final Position<?> pos1, final Position<?> pos2, final Throwable cause) {
        super(cause);
        this.p1 = pos1;
        this.p2 = pos2;
    }

    /**
     * Builds the exception given two positions claimed to be not compatible.
     * 
     * @param pos1
     *            the first position
     * @param pos2
     *            the second position
     */
    public UncomparableDistancesException(final Position<?> pos1, final Position<?> pos2) {
        super();
        this.p1 = pos1;
        this.p2 = pos2;
    }

    @Override
    public String getMessage() {
        return "Impossible to compare: " + p1 + " and " + p2;
    }

}
