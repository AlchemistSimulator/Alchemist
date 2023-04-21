/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.linkingrules;

import it.unibo.alchemist.model.LinkingRule;
import it.unibo.alchemist.model.Position;

/**
 * @param <T>
 *            Concentration type
 * @param <P>
 *            {@link Position} type
 */
public abstract class AbstractLocallyConsistentLinkingRule<T, P extends Position<? extends P>> implements LinkingRule<T, P> {

    private static final long serialVersionUID = 1L;

    @Override
    public final boolean isLocallyConsistent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
