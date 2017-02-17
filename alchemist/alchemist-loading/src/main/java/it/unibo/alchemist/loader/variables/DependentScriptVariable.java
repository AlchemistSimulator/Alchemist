package it.unibo.alchemist.loader.variables;

import java.util.Map;
import java.util.stream.Stream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.danilopianini.lang.HashUtils;

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
    private static final String RANDOM = "RANDOM";
    private static final String RANDOM_REGEX = "\\$" + RANDOM;
    private final String script;

    /**
     * @param formula
     *            a valid Javascript expression, where variable names are
     *            prefixed with $, and where $RANDOM can be used to generate
     *            controlled random values
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
        if (script.contains(RANDOM)) {
            final Object[] hash = Stream.concat(Stream.of(script), variables.values().stream()).toArray();
            final double random = Math.abs((double) HashUtils.hash32(hash)) / Integer.MAX_VALUE;
            formula = script.replaceAll(RANDOM_REGEX, Double.toString(random));
        }
        for (final String var : keys) {
            formula = formula.replaceAll("\\$" + var, Double.toString(variables.get(var)));
        }
        try {
            final Object result = ENGINE.eval(formula);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
            if (result instanceof Boolean) {
                return (Boolean) result ? 1 : 0;
            }
            throw new IllegalStateException("The script return value (" + result + ": " + result.getClass().getSimpleName() + ") can't get converted to a Java Number");
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
