/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core;

import it.unibo.alchemist.model.Actionable;

import java.util.ArrayList;
import java.util.List;

/**
 * Batched extension for ArrayIndexedPriorityQueue.
 * This implementation uses epsilon-sensitivity
 * in order to build the next batch to process. Events will be added to the batch
 * while | tau(e1) - tau(e2) | < epsilon.
 *
 * @param <T> concentration type
 */
public final class ArrayIndexedPriorityEpsilonBatchQueue<T> extends ArrayIndexedPriorityQueue<T> implements BatchedScheduler<T> {

    private final double epsilon;

    /**
     * Construct a new ArrayIndexedPriorityEpsilonBatchQueue.
     *
     * @param epsilon epsilon sensitivity value
     */
    public ArrayIndexedPriorityEpsilonBatchQueue(final double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public List<Actionable<T>> getNextBatch() {
        if (getTree().isEmpty()) {
            return new ArrayList<>();
        }
        if (getTree().size() == 1) {
            final List<Actionable<T>> result = new ArrayList<>();
            result.add(getTree().get(0));
            return result;
        }

        final List<Actionable<T>> result = new ArrayList<>();
        final var prev = getTree().get(0);
        result.add(prev);
        for (final var next : getTree().subList(1, getTree().size())) {
            if (Math.abs(next.getTau().toDouble() - prev.getTau().toDouble()) >= epsilon) {
                break;
            } else {
                result.add(next);
            }
        }
        return result;
    }

    @Override
    public void updateReaction(final Actionable<T> reaction) {
        synchronized (this) {
            super.updateReaction(reaction);
        }
    }

}
