/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.implementations;

import it.unibo.alchemist.core.interfaces.BatchedScheduler;
import it.unibo.alchemist.model.interfaces.Actionable;

import java.util.ArrayList;
import java.util.List;

/**
 * Batched extension for ArrayIndexedPriorityQueue.
 * This implementation presents fixed size batches.
 *
 * @param <T> concentration type
 */
public final class ArrayIndexedPriorityFixedBatchQueue<T> extends ArrayIndexedPriorityQueue<T> implements BatchedScheduler<T> {

    private final int batchSize;

    /**
     * Construct a new ArrayIndexedPriorityFixedBatchQueue.
     * @param batchSize batch size
     */
    public ArrayIndexedPriorityFixedBatchQueue(final int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public List<Actionable<T>> getNextBatch() {
        List<Actionable<T>> result = new ArrayList<>();
        if (!getTree().isEmpty()) {
            result = getTree().subList(0, Math.min(getTree().size(), batchSize));
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
