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
 */
public final class NumTreeNode extends ATreeNode<Double> {

    private static final long serialVersionUID = 6624916497764658902L;
    private String s;

    /**
     * @param data
     *            the number to embed
     */
    public NumTreeNode(final Double data) {
        super(data, null, null);
    }

    /**
     * This constructor is provided for better compatibility.
     * 
     * @param data
     *            the number to embed, integer format
     */
    public NumTreeNode(final int data) {
        this(Double.valueOf(data));
    }

    @Override
    public Double getValue(final Map<HashString, ITreeNode<?>> mp) {
        return getData();
    }

    @Override
    public Type getType() {
        return Type.NUM;
    }

    @Override
    public String toString() {
        if (s == null) {
            final double d = getData();
            if (d == Math.floor(d) && !Double.isInfinite(d) && d <= Integer.MAX_VALUE && d >= Integer.MIN_VALUE) {
                s = Integer.toString(getData().intValue());
            } else {
                s = getData().toString();
            }
        }
        return s;
    }

}
