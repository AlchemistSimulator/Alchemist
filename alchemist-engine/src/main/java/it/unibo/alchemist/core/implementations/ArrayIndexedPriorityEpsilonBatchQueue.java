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
import java.util.Collections;
import java.util.List;

public class ArrayIndexedPriorityEpsilonBatchQueue<T> extends ArrayIndexedPriorityQueue<T> implements BatchedScheduler<T> {

    private final double epsilon;

    public ArrayIndexedPriorityEpsilonBatchQueue(final double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public List<Actionable<T>> getNextBatch() {
        if(tree.size() == 0) {
            return Collections.emptyList();
        }
        if (tree.size() == 1) {
            List<Actionable<T>> result = new ArrayList<>();
            result.add(tree.get(0));
            return result;
        }

        List<Actionable<T>> result = new ArrayList<>();
        var prev = tree.get(0);
        result.add(prev);
        for(final var next : tree.subList(1, tree.size())) {
            if(Math.abs(next.getTau().toDouble() - prev.getTau().toDouble()) >= epsilon) {
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
