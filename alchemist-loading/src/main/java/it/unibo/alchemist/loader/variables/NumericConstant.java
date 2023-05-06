/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.variables;

import it.unibo.alchemist.model.loading.DependentVariable;

import java.util.Map;

/**
 * A numeric constant.
 */
public final class NumericConstant implements DependentVariable<Number> {

    private static final long serialVersionUID = 1L;
    private final Number internal;

    /**
     * @param n the number
     */
    public NumericConstant(final Number n) {
        internal = n;
    }

    @Override
    public Number getWith(final Map<String, Object> variables) {
        return internal;
    }

}
