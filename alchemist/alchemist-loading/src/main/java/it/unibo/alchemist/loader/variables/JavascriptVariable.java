/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.loader.variables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Interpreter that relies on the Nashorn Javascript engine shipped with the
 * standard JDK. Variables are substituted with their provided values using a
 * simple regular expression, and then the resulting {@link String} is fed to
 * the Javascript interpereter.
 *
 * @deprecated This class uses Nashorn, which is scheduled to be removed in future JDKs
 */
@Deprecated
public class JavascriptVariable extends ScriptVariable<Object> {

    private static final long serialVersionUID = 1L;
    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    private static final ScriptEngine ENGINE = MANAGER.getEngineByName("nashorn");
    private static final Logger L = LoggerFactory.getLogger(JavascriptVariable.class);

    /**
     * @param formula
     *            the formula
     */
    public JavascriptVariable(final String formula) {
        super(formula);
        L.warn("Javascript variables are deprecated and will get dropped from Alchemist as soon as Nashorn gets " +
                "dropped from the JDK as per JEP 335. See https://openjdk.java.net/jeps/335.");
    }

    @Override
    protected Object interpret(final String s) {
        try {
            return ENGINE.eval(s);
        } catch (final ScriptException e) {
            throw new IllegalStateException("«" + s + "» is not a valid Javascript fragment", e);
        }
    }

}
