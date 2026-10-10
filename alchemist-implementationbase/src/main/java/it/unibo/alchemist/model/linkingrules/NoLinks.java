/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules;

import it.unibo.alchemist.model.Environment;
import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Position;
import it.unibo.alchemist.model.neighborhoods.Neighborhoods;

import javax.annotation.Nonnull;

/**
 * This rule guarantees that no links are created at all.
 *
 * @param <T>
 *            concentration type
 * @param <P>
 *            position type
 */
public class NoLinks<T, P extends Position<P>> extends AbstractLocallyConsistentLinkingRule<T, P> {

    @Nonnull
    @Override
    public final Neighborhood<T> computeNeighborhood(
        @Nonnull final Node<T> center,
        @Nonnull final Environment<T, P> environment
    ) {
        return Neighborhoods.make(environment, center);
    }

}
