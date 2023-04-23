/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.linkingrules;

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods;
import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;

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
    public final Neighborhood<T> computeNeighborhood(final Node<T> center, final Environment<T, P> environment) {
        return Neighborhoods.make(environment, center);
    }

}
