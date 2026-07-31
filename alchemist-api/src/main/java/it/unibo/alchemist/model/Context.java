/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model;

/**
 * This enum describes the possible contexts for a given {@link Action} or
 * {@link Condition}. A context represents the elements which are read for a
 * {@link Condition} and the elements that may be subject of modifications for
 * an {@link Action}. Contexts describe model access scope for inspection and
 * external representations; reactive scheduling is driven by observable model
 * state instead.
 *
 */
public enum Context {
    /**
     * The reaction potentially influences every other reaction.
     */
    GLOBAL,
    /**
     * The reaction can influence only the node in which it's placed.
     */
    LOCAL,
    /**
     * The reaction may influence its node and the neighboring ones.
     */
    NEIGHBORHOOD;

    /**
     * @param c1 context to compare
     * @param c2 other context to compare
     * @return the wider (more general) between the two:
     *      if either one is {@link #GLOBAL}, then {@link #GLOBAL} is returned.
     *      Otherwise, if either one is {@link #NEIGHBORHOOD}, {@link #NEIGHBORHOOD} is returned.
     *      Otherwise, {@link #LOCAL} is returned.
     */
    public static Context getWider(final Context c1, final Context c2) {
        if (c1 == GLOBAL || c2 == GLOBAL) {
            return GLOBAL;
        }
        if (c1 == NEIGHBORHOOD || c2 == NEIGHBORHOOD) {
            return NEIGHBORHOOD;
        }
        return LOCAL;
    }
}
