package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.interfaces.Node;

/**
 * @param <T>
 */
public abstract class AbstractRandomizableAction<T> extends AbstractAction<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "All provided RandomGenerator implementations are actually Serializable")
    private final RandomGenerator rand;

    /**
     * 
     * @param node 
     * @param random 
     */
    public AbstractRandomizableAction(final Node<T> node, final RandomGenerator random) {
        super(node);
        rand = random;
    }

    /**
     * 
     * @return the random generator to use in this class.
     */
    protected RandomGenerator getRandomGenerator() {
        return rand;
    }

}
