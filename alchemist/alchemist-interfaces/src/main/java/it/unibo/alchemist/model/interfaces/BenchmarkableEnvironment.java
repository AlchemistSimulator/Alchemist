package it.unibo.alchemist.model.interfaces;

/**
 * An environment which provides a mean to get infos about its performances.
 *
 * @param <T>
 * @param <P>
 */
public interface BenchmarkableEnvironment<T, P extends Position<? extends P>> extends Environment<T, P> {

    /**
     * Call this method to tell this environment that it should record its performances.
     * Please note that some environments might ignore this message if this method is not called before
     * starting using the environment itself.
     * 
     */
    void enableBenchmark();

    /**
     * @return a double which is a index of the performances
     */
    double getBenchmarkResult();
}
