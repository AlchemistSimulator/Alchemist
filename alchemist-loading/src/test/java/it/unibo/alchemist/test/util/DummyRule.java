/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test.util;

import java.util.List;

import it.unibo.alchemist.model.linkingrules.NoLinks;
import it.unibo.alchemist.model.Position;

/**
 * @param <T>
 *            concentration type
 * @param <P>
 *            position type
 */
public final class DummyRule<T, P extends Position<P>> extends NoLinks<T, P> {

    private static final long serialVersionUID = 1L;

    /**
     * @param param1
     *            p1
     * @param param2
     *            p2
     */
    public DummyRule(final List<Double> param1, final List<Double> param2) {
        check(param1);
        check(param2);
    }

    private static void checkNot(final boolean condition) {
        if (condition) {
            throw new IllegalArgumentException();
        }
    }

    private static void check(final List<Double> l) {
        checkNot(l.isEmpty());
        for (final Object d: l) {
            checkNot(!(d instanceof Number));
        }
    }
}
