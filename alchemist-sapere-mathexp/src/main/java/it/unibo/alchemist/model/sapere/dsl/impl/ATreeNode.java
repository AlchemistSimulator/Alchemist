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

import java.util.Map;


/**
 * @param <T> concentration type
 */
public abstract class ATreeNode<T> implements ITreeNode<T> {

    private static final long serialVersionUID = -3565369437007735315L;

    private final T data;
    private HashString fsCache;
    private final ITreeNode<?> left;
    private final ITreeNode<?> right;

    /**
     * Builds the internals of a tree node.
     * 
     * @param dat
     *            the data contained within the node
     * @param l
     *            the left child
     * @param r
     *            the right child
     */
    public ATreeNode(final T dat, final ITreeNode<?> l, final ITreeNode<?> r) {
        this.data = dat;
        this.right = r;
        this.left = l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object t) {
        if (t instanceof ITreeNode<?>) {
            return toHashString().equals(((ITreeNode<?>) t).toHashString());
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#getData()
     */
    @Override
    public T getData() {
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#getLeftChild()
     */
    @Override
    public ITreeNode<?> getLeftChild() {
        return left;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.expressions.interfaces.ITreeNode#getNumberOfChildren()
     */
    @Override
    public int getNumberOfChildren() {
        return left == null ? 0 : right == null ? 1 : 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#getRightChild()
     */
    @Override
    public ITreeNode<?> getRightChild() {
        return right;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * alice.alchemist.expressions.interfaces.ITreeNode#getValue(java.util.Map)
     */
    @Override
    public abstract T getValue(Map<HashString, ITreeNode<?>> mp);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (fsCache == null) {
            fsCache = new HashString(toString());
        }
        return fsCache.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see alice.alchemist.expressions.interfaces.ITreeNode#toHashString()
     */
    @Override
    public HashString toHashString() {
        if (fsCache == null) {
            fsCache = new HashString(toString());
        }
        return fsCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public abstract String toString();

}
