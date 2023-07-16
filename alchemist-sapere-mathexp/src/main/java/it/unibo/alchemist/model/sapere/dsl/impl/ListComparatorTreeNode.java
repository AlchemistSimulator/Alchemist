/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.sapere.dsl.impl;

import it.unibo.alchemist.model.sapere.dsl.ITreeNode;
import org.danilopianini.lang.HashString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 */
public class ListComparatorTreeNode extends ATreeNode<ListComparator> {

    private static final long serialVersionUID = 8646190301867856844L;
    private static final Logger L = LoggerFactory.getLogger(ListComparatorTreeNode.class);

    /**
     * @param s The comparator to use
     * @param left the left ITreeNode (a List or Variable)
     * @param right the right ITreeNode (a List)
     */
    public ListComparatorTreeNode(final ListComparator s, final ITreeNode<?> left, final ITreeNode<?> right) {
        super(s, left, right);
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#getType()
     */
    @Override
    public Type getType() {
        return Type.LISTCOMPARATOR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.expressions.implementations.ATreeNode#getValue(java.util
     * .Map)
     */
    @Override
    public ListComparator getValue(final Map<HashString, ITreeNode<?>> mp) {
        L.error("It makes no sense to evaluate a Comparator.");
        return null;
    }

    /* (non-Javadoc)
     * @see alice.alchemist.expressions.implementations.ATreeNode#toString()
     */
    @Override
    public String toString() {
        switch (getNumberOfChildren()) {
        case 1:
            return getLeftChild() + " " + getData();
        case 2:
            return getLeftChild() + " " + getData() + " " + getRightChild();
        default:
            return "ERROR: " + getData();
        }
    }

}
