/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.variables;

import org.danilopianini.util.Hashes;

import java.util.Map;
import java.util.stream.Stream;

/**
 * This variable can be initialized by providing a formula where other variables
 * are prefixed with the $ symbol.
 * 
 * Subclasses must implement an interpreter able to compute on the string
 * produced by this class.
 *
 * @param <R> Evaluation return type
 */
public abstract class ScriptVariable<R> implements DependentVariable<R> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String RANDOM = "RANDOM";
    private static final String RANDOM_REGEX = "\\$" + RANDOM;
    private final String script;

    /**
     * @param formula
     *            a valid Javascript expression, where variable names are
     *            prefixed with $, and where $RANDOM can be used to generate
     *            controlled random values
     */
    public ScriptVariable(final String formula) {
        this.script = formula;
    }

    @Override
    public final R getWith(final Map<String, Object> variables) {
        /*
         * 1) Sort variable names by decreasing length.
         * 2) Replace each name with with value in the script
         * 3) Run the script in the engine
         */
        final String[] keys = variables.keySet().stream()
            .sorted((v1, v2) -> Integer.compare(v2.length(), v1.length()))
            .toArray(i -> new String[i]);
        String formula = script;
        if (script.contains(RANDOM)) {
            final Object[] hash = Stream.concat(Stream.of(script), variables.values().stream()).toArray();
            final double random = Math.abs((double) Hashes.hash32(hash)) / Integer.MAX_VALUE;
            formula = script.replaceAll(RANDOM_REGEX, Double.toString(random));
        }
        for (final String var : keys) {
            formula = formula.replaceAll("\\$" + var, variables.get(var).toString());
        }
        return interpret(formula);
    }

    /**
     * @param s
     *            the string where variables are replaced by their string value
     * @return the result of the interpretation
     */
    protected abstract R interpret(String s);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return script;
    }

}
