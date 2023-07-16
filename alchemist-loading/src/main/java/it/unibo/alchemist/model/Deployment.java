/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @param <P> position type
 */
@FunctionalInterface
public interface Deployment<P extends Position<? extends P>> extends Iterable<P> {

    /**
     * @return a {@link Stream} over the positions of this {@link Deployment}
     */
    Stream<P> stream();

    @Override
    default Iterator<P> iterator() {
        return stream().iterator();
    }

    /**
     * Optional {@link LinkingRule} associated to the deployment.
     *
     * @param <T> concentration type of the {@link LinkingRule}
     * @return null if the deployment has no associated {@link LinkingRule},
     * and an instance of the {@link LinkingRule} otherwise
     */
    @Nullable
    default <T> LinkingRule<T, P> getAssociatedLinkingRule() {
        return null;
    }
}
