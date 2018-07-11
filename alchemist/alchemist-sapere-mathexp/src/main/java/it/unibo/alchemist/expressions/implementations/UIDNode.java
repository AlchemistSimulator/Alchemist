/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.expressions.implementations;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import org.danilopianini.lang.HashString;

import java.util.Map;


/**
 */
public class UIDNode extends ConstTreeNode {

    private static final String PREFIX = "id";
    /**
     * 
     */
    private static final long serialVersionUID = -2166926230641043532L;
    private HashString cache;
    private final HashString original;

    /**
     * Builds a new UUID node.
     * 
     * @param fs
     *            the HashString representation of the object this UID refers
     *            to
     */
    public UIDNode(final HashString fs) {
        super(fs);
        original = fs;
    }

    @Override
    public HashString getValue(final Map<HashString, ITreeNode<?>> mp) {
        return original;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#toHashString()
     */
    @Override
    public HashString toHashString() {
        if (cache == null) {
            cache = new HashString(PREFIX + original.hashToString());
        }
        return cache;
    }

    @Override
    public String toString() {
        return toHashString().toString();
    }

}
