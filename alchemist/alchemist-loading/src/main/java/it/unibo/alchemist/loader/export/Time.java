/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.export;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Exports a column with the current time.
 */
public final class Time implements Extractor {

    private static final List<String> COLNAME;
    static {
        final List<String> cName = new LinkedList<>();
        cName.add("time");
        COLNAME = Collections.unmodifiableList(cName);
    }

    @Override
    public double[] extractData(final Environment<?, ?> env, final Reaction<?> r, final it.unibo.alchemist.model.interfaces.Time time, final long step) {
        return new double[]{time.toDouble()};
    }

    @Override
    public List<String> getNames() {
        return COLNAME;
    }

}
