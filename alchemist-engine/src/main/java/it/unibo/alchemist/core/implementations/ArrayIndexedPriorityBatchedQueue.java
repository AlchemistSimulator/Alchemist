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

public class ArrayIndexedPriorityBatchedQueue<T> extends ArrayIndexedPriorityQueue<T> implements BatchedScheduler<T> {

    @Override
    public List<Actionable<T>> getNext(final int batchSize) {
        List<Actionable<T>> result = new ArrayList<>();
        if (!tree.isEmpty()) {
            result = tree.subList(0, Math.min(tree.size(), batchSize));
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
