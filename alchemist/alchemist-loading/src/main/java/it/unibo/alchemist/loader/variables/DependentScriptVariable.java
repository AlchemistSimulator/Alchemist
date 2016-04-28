package it.unibo.alchemist.loader.variables;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This variable can be initialized by providing a formula where other variables
 * are prefixed with the $ symbol.
 * 
 * Internally it relies on the Nashorn Javascript engine shipped with the
 * standard JDK. Variables are substituted with their provided values using a
 * simple regular expression, and then the resulting {@link String} is fed to
 * the Javascript interpereter.
 */
public class DependentScriptVariable implements DependentVariable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    private static final ScriptEngine ENGINE = MANAGER.getEngineByName("nashorn");
    private final String script;

    /**
     * @param formula
     *            a valid Javascript expression, where variable names are
     *            prefixed with $
     */
    public DependentScriptVariable(final String formula) {
        this.script = formula;
    }

    @Override
    public double getWith(final Map<String, Double> variables) {
        /*
         * 1) Sort variable names by decreasing length.
         * 2) Replace each name with with value in the script
         * 3) Run the script in the engine
         */
        final String[] keys = variables.keySet().stream()
            .sorted((v1, v2) -> Integer.compare(v2.length(), v1.length()))
            .toArray(i -> new String[i]);
        String formula = script;
        for (final String var : keys) {
            formula = formula.replaceAll("\\$" + var, Double.toString(variables.get(var)));
        }
        try {
            return ((Number) ENGINE.eval(formula)).doubleValue();
        } catch (final ScriptException | ClassCastException e) {
            throw new IllegalStateException("The expression engine could not perform the requested operation. " + script
                    + " got transformed in " + formula
                    + ", but this is either not a valid script, or its return type is not compatible with a Java Number", e);
        }
    }

    @Override
    public String toString() {
        return script;
    }

}
