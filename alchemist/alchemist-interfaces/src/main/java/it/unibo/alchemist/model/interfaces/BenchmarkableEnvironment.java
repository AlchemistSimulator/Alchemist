package it.unibo.alchemist.model.interfaces;

/**
 * An environment which provides a mean to get infos about its performances.
 *
 * @param <T>
 */
public interface BenchmarkableEnvironment<T> extends Environment<T> {

    /**
     * Call this method to tell this environment that it should record its performances.
     * Please note that some environments might ignore this message if this method is not called before
     * starting using the environment itself.
     * 
     */
    void doBenchmark();

    /**
     * @return a double which is a index of the performances
     */
    double getBenchmarkResult();
}
