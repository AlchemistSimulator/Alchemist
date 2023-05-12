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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 */
public final class ListTreeNode extends ATreeNode<Set<ITreeNode<?>>> implements Iterable<ITreeNode<?>> {

    private static final long serialVersionUID = -5912038362750826726L;
    private static final CompareElements CMP = new CompareElements();
    private static final Logger L = LoggerFactory.getLogger(AST.class);
    private String stringCache;

    /**
     * @param data
     *            the list which embedded in this node
     */
    public ListTreeNode(final Set<ITreeNode<?>> data) {
        super(data, null, null);
    }

    @Override
    public Type getType() {
        return Type.LIST;
    }

    @Override
    public Set<ITreeNode<?>> getValue(final Map<HashString, ITreeNode<?>> mp) {
        L.error("It makes no sense to evaluate lists (" + getData() + ").");
        return null;
    }

    @Override
    public Iterator<ITreeNode<?>> iterator() {
        return getData().iterator();
    }

    @Override
    public String toString() {
        if (stringCache == null) {
            /*
             * This works because lists cannot contain lists.
             */
            final TreeSet<ITreeNode<?>> orderedSet = new TreeSet<>(CMP);
            orderedSet.addAll(getData());
            stringCache = orderedSet.toString();
        }
        return stringCache;
    }

    private static class CompareElements implements Comparator<ITreeNode<?>>, Serializable {
        private static final long serialVersionUID = 8864314250973076627L;
        @Override
        public int compare(final ITreeNode<?> o1, final ITreeNode<?> o2) {
            return o1.hashCode() - o2.hashCode();
        }
    }

}
