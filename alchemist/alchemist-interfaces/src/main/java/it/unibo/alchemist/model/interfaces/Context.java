/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/

/**
 * 
 */
package it.unibo.alchemist.model.interfaces;

/**
 * This enum describes the possible contexts for a given {@link Action} or
 * {@link Condition}. A context represents the elements which are read for a
 * {@link Condition} and the elements that may be subject of modifications for
 * an {@link Action}. Choosing the right {@link Context} is crucial: if it's too
 * restricted the simulation will be invalid, if it's too wide it WILL impact
 * dramatically on performances. Contexts are used internally to better
 * determine dependencies among reactions. See
 * <a href= "http://apice.unibo.it/xwiki/bin/view/Publications/PianiniMASS11" >
 * this paper</a> for further informations about the usage of contexts.
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

    public static Context getWider(Context c1, Context c2) {
        if (c1 == GLOBAL || c2 == GLOBAL) {
            return GLOBAL;
        }
        if (c1 == NEIGHBORHOOD || c2 == NEIGHBORHOOD) {
            return NEIGHBORHOOD;
        }
        return LOCAL;
    }
}
