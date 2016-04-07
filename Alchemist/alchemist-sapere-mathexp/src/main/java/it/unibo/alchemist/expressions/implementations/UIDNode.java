/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.expressions.implementations;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import org.danilopianini.lang.util.FasterString;

import java.util.Map;


/**
 */
public class UIDNode extends ConstTreeNode {

    private static final String PREFIX = "id";
    /**
     * 
     */
    private static final long serialVersionUID = -2166926230641043532L;
    private FasterString cache;
    private final FasterString original;

    /**
     * Builds a new UUID node.
     * 
     * @param fs
     *            the FasterString representation of the object this UID refers
     *            to
     */
    public UIDNode(final FasterString fs) {
        super(fs);
        original = fs;
    }

    @Override
    public FasterString getValue(final Map<FasterString, ITreeNode<?>> mp) {
        return original;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#toFasterString()
     */
    @Override
    public FasterString toFasterString() {
        if (cache == null) {
            cache = new FasterString(PREFIX + original.hashToString());
        }
        return cache;
    }

    @Override
    public String toString() {
        return toFasterString().toString();
    }

}
