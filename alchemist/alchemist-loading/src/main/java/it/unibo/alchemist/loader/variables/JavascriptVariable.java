/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.variables;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Interpreter that relies on the Nashorn Javascript engine shipped with the
 * standard JDK. Variables are substituted with their provided values using a
 * simple regular expression, and then the resulting {@link String} is fed to
 * the Javascript interpereter.
 */
public class JavascriptVariable extends ScriptVariable<Object> {

    private static final long serialVersionUID = 1L;
    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    private static final ScriptEngine ENGINE = MANAGER.getEngineByName("nashorn");

    /**
     * @param formula
     *            the formula
     */
    public JavascriptVariable(final String formula) {
        super(formula);
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
