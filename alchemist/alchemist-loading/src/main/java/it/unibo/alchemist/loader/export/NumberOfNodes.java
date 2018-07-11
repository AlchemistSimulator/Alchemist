package it.unibo.alchemist.loader.export;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Logs the number of nodes in the scenario.
 */
public class NumberOfNodes implements Extractor {

    private static final List<String> COLNAME;
    static {
        final List<String> cName = new LinkedList<>();
        cName.add("nodes");
        COLNAME = Collections.unmodifiableList(cName);
    }

    @Override
    public double[] extractData(final Environment<?, ?> env, final Reaction<?> r, final it.unibo.alchemist.model.interfaces.Time time, final long step) {
        return new double[]{env.getNodesNumber()};
    }

    @Override
    public List<String> getNames() {
        return COLNAME;
    }

}
