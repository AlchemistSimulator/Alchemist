package it.unibo.alchemist.model.implementations.actions;

import org.apache.commons.math3.random.RandomGenerator;

import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * Class to retain backwards compatibility with the Alchemist XML loader.
 *
 */
@Deprecated
public class ProtelisProgram extends RunProtelisProgram {

    /**
     * 
     */
    private static final long serialVersionUID = -446495970778249044L;

    /**
     * @see RunProtelisProgram
     * 
     * @param env
     *            the environment
     * @param n
     *            the node
     * @param r
     *            the reaction
     * @param rand
     *            the random engine
     * @param prog
     *            the Protelis program
     * @throws SecurityException
     *             if you are not authorized to load required classes
     * @throws ClassNotFoundException
     *             if required classes can not be found
     */
    public ProtelisProgram(final Environment<Object> env, final ProtelisNode n, final Reaction<Object> r, final RandomGenerator rand,
            final String prog) throws SecurityException, ClassNotFoundException {
        super(env, n, r, rand, prog);
    }

}