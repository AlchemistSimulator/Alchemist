package it.unibo.alchemist.loader;

import java.util.List;
import java.util.Map;

import it.unibo.alchemist.loader.export.Extractor;
import it.unibo.alchemist.loader.variables.Variable;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * An entity which is able to produce an Alchemist {@link Environment}, possibly
 * with user defined variable values.
 */
public interface Loader {

    /**
     * @param <T>
     *            concentration type
     * @return an {@link Environment} with all the variables set at their
     *         default values
     */
    <T> Environment<T> getDefault();

    /**
     * @return a {@link Map} between variable names and their actual
     *         representation
     */
    Map<String, Variable<?>> getVariables();

    /**
     * @param values
     *            a map specifying name-value bindings for the variables in this
     *            scenario
     * @param <T>
     *            concentration type
     * @return an {@link Environment} with all the variables set at the
     *         specified values. If the value is unspecified, the default is
     *         used instead
     */
    <T> Environment<T> getWith(Map<String, ?> values);

    /**
     * @return The data extractors
     */
    List<Extractor> getDataExtractors();

    /**
     * 
     * @return dependencies files
     */
    List<String> getDependencies();

    /**
     * 
     * @return yaml file content
     */
    String getYamlAsString();
}
