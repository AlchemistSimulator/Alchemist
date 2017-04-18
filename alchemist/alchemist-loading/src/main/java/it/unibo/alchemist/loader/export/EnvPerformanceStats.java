package it.unibo.alchemist.loader.export;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import it.unibo.alchemist.model.interfaces.BenchmarkableEnvironment;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Exports the stats about the performance of the environment.
 *
 */
public class EnvPerformanceStats implements Extractor {

    private static final List<String> COLNAME;
    static {
        final List<String> cName = new LinkedList<>();
        cName.add("envPerformance");
        COLNAME = Collections.unmodifiableList(cName);
    }

    @Override
    public double[] extractData(final Environment<?> env, final Reaction<?> r, final Time time, final long step) {
        if (env instanceof BenchmarkableEnvironment) {
            return new double[]{((BenchmarkableEnvironment<?>) env).getBenchmarkResult()};
        }
        return new double[]{};
    }

    @Override
    public List<String> getNames() {
        return COLNAME;
    }

}
