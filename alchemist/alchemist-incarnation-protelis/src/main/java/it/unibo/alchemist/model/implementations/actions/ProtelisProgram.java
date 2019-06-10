/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to retain backwards compatibility with the Alchemist XML loader.
 * @param <P> position type
 */
@Deprecated
public class ProtelisProgram<P extends Position<P>> extends RunProtelisProgram<P> {

    /**
     * 
     */
    private static final long serialVersionUID = -446495970778249044L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtelisIncarnation.class);

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
     */
    public ProtelisProgram(final Environment<Object, P> env, final ProtelisNode n, final Reaction<Object> r, final RandomGenerator rand,
            final String prog) throws SecurityException {
        super(env, n, r, rand, prog, r.getRate());
        LOGGER.warn("{} is deprecated and should be replaced by {}", getClass(), RunProtelisProgram.class);
    }

}
