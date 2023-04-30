/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.biochemistry.actions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.actions.AbstractAction;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @param <T> concentration type
 */
public abstract class AbstractRandomizableAction<T> extends AbstractAction<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification = "All provided RandomGenerator implementations are actually Serializable"
    )
    private final RandomGenerator rand;

    /**
     * 
     * @param node the {@link Node}
     * @param random the {@link RandomGenerator}
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
