/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * This rule guarantees that no links are created at all.
 *
 * @param <T>
 *            concentration type
 * @param <P>
 *            position type
 */
public class NoLinks<T, P extends Position<P>> extends AbstractLocallyConsistentLinkingRule<T, P> {

    private static final long serialVersionUID = -711043794655618585L;

    @Override
    public final Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> env) {
        return Neighborhoods.make(env, center);
    }

}
