/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.expressions.implementations;

import java.util.Map;

import org.danilopianini.lang.util.FasterString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.expressions.interfaces.ITreeNode;


/**
 */
public class ComparatorTreeNode extends ATreeNode<FasterString> {

    private static final long serialVersionUID = 8646190301867856844L;
    private static final Logger L = LoggerFactory.getLogger(ComparatorTreeNode.class);

    /**
     * Builds a ComparatorTreeNode.
     * 
     * @param c
     *            FasterString representation of the comparator (operator of the
     *            comparison)
     * @param left
     *            the left child (a variable)
     * @param right
     *            the right child (an expression)
     */
    public ComparatorTreeNode(final FasterString c, final ITreeNode<?> left, final ITreeNode<?> right) {
        super(c, left, right);
    }

    @Override
    public Type getType() {
        return Type.COMPARATOR;
    }

    @Override
    public FasterString getValue(final Map<FasterString, ITreeNode<?>> mp) {
        L.error("It makes no sense to evaluate a Comparator.");
        return null;
    }

    @Override
    public String toString() {
        return getLeftChild() + ": (" + getLeftChild() + getData() + getRightChild() + ")";
    }

}
