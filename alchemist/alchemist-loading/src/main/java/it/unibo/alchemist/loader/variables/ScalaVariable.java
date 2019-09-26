/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.variables;

import it.unibo.alchemist.scala.ScalaInterpreter;

/**
 * Interpreter that relies on the Scala Toolbox for interpreting the formula
 * String.
 *
 * @param <R> return type
 */
public final class ScalaVariable<R> extends ScriptVariable<R> {

    private static final long serialVersionUID = 1L;

    /**
     * @param formula the Scala script
     */
    public ScalaVariable(final String formula) {
        super(formula);
    }

    @Override
    protected R interpret(final String s) {
        try {
            return ScalaInterpreter.apply(s);
        } catch (Throwable t) { // NOPMD
            throw new IllegalStateException("«" + s + "» is not a valid Scala script.", t);
        }
    }

}
