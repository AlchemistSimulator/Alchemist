/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gui.isolines;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Some utilies for null checking.
 */
public final class Utilities {

    private Utilities() {
    }

    /**
     * Checks if a variable number of objects are null, in that case an IllegalArgumentException will be thrown.
     * @throws IllegalArgumentException
     *              with NULL_ARGUMENT message
     * @param objects
     *              the objects to check
     */
    public static void requireNonNull(final Object...objects) {
        if (Arrays.stream(objects).anyMatch(Utilities::isOrContainsNull)) {
            throw new NullPointerException();
        }
    }

    /**
     * Checks if an object is null or, in case it is a Collection or an Array, contains null elements.
     * Note that if you are using your own made collection class, this won't work.
     * @param obj
     *              the object to check
     * @return a boolean
     *              true if the object is or contains null element, false otherwise
     */
    public static boolean isOrContainsNull(final Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj.getClass().isArray()) {
            return Arrays.stream((Object[]) obj).anyMatch(Utilities::isOrContainsNull);
        }
        if (obj.getClass().isAssignableFrom(Collections.class)) {
            return ((Collection<?>) obj).stream().anyMatch(Utilities::isOrContainsNull);
        }
        return false;
    }
}
