/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks;

import it.unibo.alchemist.model.Neighborhood;
import it.unibo.alchemist.model.NetworkModel;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.observation.Observable;

import javax.annotation.Nonnull;

import java.util.Set;

import static it.unibo.alchemist.model.observables.util.MutableObservables.observe;

/**
 * This model guarantees that no links are created at all.
 *
 * @param <T>
 *            concentration type
 */
public class NoLinks<T> implements NetworkModel<T> {

    @Nonnull
    @Override
    public final Observable<Neighborhood<T>> neighborhoodOf(@Nonnull final Node<T> center) {
        return observe(Neighborhood.Companion.invoke(center, Set.of()));
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName();
    }
}
