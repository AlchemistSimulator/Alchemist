/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
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

    /**
     * This method provides support to determine if the current Context is less
     * wide than the one passed.
     * 
     * @param c
     *            The Context to compare with
     * @return true if this context is more strict than the one passed
     */
    public boolean isMoreStrict(final Context c) {
        return !(this.equals(GLOBAL) || c.equals(LOCAL) || this.equals(NEIGHBORHOOD) && c.equals(LOCAL));
    }
}
