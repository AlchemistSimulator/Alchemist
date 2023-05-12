/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.conditions;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import it.unibo.alchemist.model.Context;
import it.unibo.alchemist.model.Molecule;
import it.unibo.alchemist.model.Node;
import it.unibo.alchemist.model.Reaction;

/**
 * A condition that holds true only if the tracked {@link Molecule} changed its
 * {@link it.unibo.alchemist.model.Concentration}.
 * 
 * @param <T> concentration type
 */
public final class ConcentrationChanged<T> extends AbstractCondition<T> {

    private static final long serialVersionUID = 1L;
    private final Molecule target;
    @Nonnull
    private Optional<T> previous;
    private boolean hasFlipped;

    /**
     * @param node the node
     * @param target the molecule whose concentration may change
     */
    public ConcentrationChanged(@Nonnull final Node<T> node, @Nonnull final Molecule target) {
        super(node);
        this.target = Objects.requireNonNull(target);
        previous = Optional.fromNullable(node.getConcentration(target));
        hasFlipped = false;
        declareDependencyOn(target);
    }

    @Override
    public ConcentrationChanged<T> cloneCondition(final Node<T> node, final Reaction<T> reaction) {
        return new ConcentrationChanged<>(node, target);
    }

    @Override
    public Context getContext() {
        return Context.LOCAL;
    }

    @Override
    public double getPropensityContribution() {
        return isValid(true) ? 1 : 0;
    }

    @Override
    public boolean isValid() {
        return isValid(false);
    }

    private boolean isValid(final boolean internal) {
        /*
         * If the call is internal, the condition may switch to true. Otherwise,
         * it will return the previous value and switch to false
         */
        if (internal) {
            if (!hasFlipped) {
                @Nonnull
                final Optional<T> curVal = Optional.fromNullable(getNode().getConcentration(target));
                if (!curVal.equals(previous)) {
                    hasFlipped = true;
                    previous = curVal;
                }
            }
        } else {
            final boolean flip = hasFlipped;
            hasFlipped = false;
            return flip;
        }
        return hasFlipped;
    }

    @Override
    public String toString() {
        return target + "changes value";
    }

}
