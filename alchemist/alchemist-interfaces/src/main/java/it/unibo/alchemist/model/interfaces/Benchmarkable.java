package it.unibo.alchemist.model.interfaces;

/**
 * A class implementing this interface provides a mean to get infos about its performances.
 *
 */
public interface Benchmarkable {

    /**
     * Call this method to tell the implementing object that it should record its performances.
     * Please note that some classes might ignore this message if this method is not called before
     * starting using the object itself.
     */
    void doBenchmark();

    /**
     * @return a double representing the performances. Refer to the implementing class to get more
     * infos about the meaning of this result.
     */
    double getBenchmarkResult();
}
