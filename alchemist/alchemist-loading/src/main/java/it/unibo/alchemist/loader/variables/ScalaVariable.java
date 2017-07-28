package it.unibo.alchemist.loader.variables;

import it.unibo.alchemist.scala.ScalaInterpreter;

/**
 * Interpreter that relies on the Scala Toolbox for interpreting the formula
 * String.
 *
 * @param <R> return type
 */
public class ScalaVariable<R> extends ScriptVariable<R> {

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
            throw new IllegalStateException('«' + s + "» is not a valid Scala script.", t);
        }
    }

}
