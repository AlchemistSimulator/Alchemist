/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.conditions;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;

/**
 * A condition that holds true only if the tracked {@link Molecule} changed its
 * {@link it.unibo.alchemist.model.interfaces.Concentration}.
 * 
 * @param <T>
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
    public ConcentrationChanged<T> cloneCondition(final Node<T> n, final Reaction<T> r) {
        return new ConcentrationChanged<>(n, target);
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
