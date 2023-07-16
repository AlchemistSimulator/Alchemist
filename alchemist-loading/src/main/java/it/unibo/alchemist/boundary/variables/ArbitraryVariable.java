/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.variables;

import java.io.Serializable;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.danilopianini.util.ImmutableListSet;
import org.danilopianini.util.ListSet;

/**
 * A variable spanning over an arbitrary set of values.
 */
public final class ArbitraryVariable extends PrintableVariable<Serializable> {

    private static final long serialVersionUID = 1L;
    private final Serializable def;
    private final ListSet<? extends Serializable> vals;

    private ArbitraryVariable(final ListSet<? extends Serializable> values, final Serializable def) {
        this.def = def;
        vals = values;
    }

    /**
     * @param def
     *            the default value
     * @param values
     *            all the values this variable may yield
     */
    public ArbitraryVariable(final Serializable def, final double... values) {
        this(ImmutableListSet.of(ArrayUtils.toObject(values)), def);
    }

    /**
     * @param def
     *            the default value
     * @param values
     *            all the values this variable may yield
     */
    public ArbitraryVariable(final Serializable def, final Iterable<? extends Serializable> values) {
        this(ImmutableListSet.copyOf(values), def);
    }

    @Override
    public Serializable getDefault() {
        return def;
    }

    @Override
    public Stream<Serializable> stream() {
        return vals.stream().map(it -> (Serializable) it);
    }

}
