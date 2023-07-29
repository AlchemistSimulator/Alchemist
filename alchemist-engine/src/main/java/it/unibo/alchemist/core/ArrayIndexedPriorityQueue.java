/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unibo.alchemist.model.Actionable;
import it.unibo.alchemist.model.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the indexed priority queue through an Array.
 *
 * @param <T> concentration type
 */
public final class ArrayIndexedPriorityQueue<T> implements Scheduler<T> {

    private final TObjectIntMap<Actionable<T>> indexes =
        new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final List<Time> times = new ArrayList<>();
    private final List<Actionable<T>> tree = new ArrayList<>();

    @Override
    public void addReaction(final Actionable<T> reaction) {
        tree.add(reaction);
        times.add(reaction.getTau());
        final int index = tree.size() - 1;
        indexes.put(reaction, index);
        updateEffectively(reaction, index);
    }

    private void down(final Actionable<T> reaction, final int reactionIndex) {
        int index = reactionIndex;
        final Time newTime = reaction.getTau();
        while (true) {
            int minIndex = 2 * index + 1;
            if (minIndex > tree.size() - 1) {
                return;
            }
            Time minTime = times.get(minIndex);
            Actionable<T> min = tree.get(minIndex);
            final int right = minIndex + 1;
            if (right < tree.size()) {
                final Time rr = times.get(right);
                if (rr.compareTo(minTime) < 0) {
                    min = tree.get(right);
                    minIndex = right;
                    minTime = rr;
                }
            }
            if (newTime.compareTo(minTime) > 0) {
                swap(index, reaction, minIndex, min);
                index = minIndex;
            } else {
                return;
            }
        }
    }

    @Override
    public Actionable<T> getNext() {
        Actionable<T> result = null;
        if (!tree.isEmpty()) {
            result = tree.get(0);
        }
        return result;
    }

    @Override
    public void removeReaction(final Actionable<T> reaction) {
        final int index = indexes.get(reaction);
        final int last = tree.size() - 1;
        if (index == last) {
            tree.remove(index);
            indexes.remove(reaction);
            times.remove(index);
        } else {
            final Actionable<T> swapped = tree.get(last);
            indexes.put(swapped, index);
            tree.set(index, swapped);
            times.set(index, swapped.getTau());
            tree.remove(last);
            times.remove(last);
            indexes.remove(reaction);
            updateEffectively(swapped, index);
        }
    }

    private void swap(final int i1, final Actionable<T> r1, final int i2, final Actionable<T> r2) {
        indexes.put(r1, i2);
        indexes.put(r2, i1);
        tree.set(i1, r2);
        tree.set(i2, r1);
        final Time t = times.get(i1);
        times.set(i1, times.get(i2));
        times.set(i2, t);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        int pow = 0;
        int exp = 0;
        for (int i = 0; i < tree.size(); i++) {
            final int tabulars = (int) (
                Math.floor(Math.log(tree.size()) / Math.log(2)) - Math.floor(Math.log(i + 1) / Math.log(2))
            ) + 1;
            sb.append("\t".repeat(Math.max(0, tabulars)));
            sb.append(times.get(i));
            if (i == pow) {
                exp++;
                pow = pow + (int) Math.pow(2, exp);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private boolean up(final Actionable<T> reaction, final int reactionIndex) {
        int index = reactionIndex;
        int parentIndex = getParent(index);
        final Time newTime = reaction.getTau();
        if (parentIndex == -1) {
            return false;
        } else {
            Actionable<T> parent = tree.get(parentIndex);
            if (newTime.compareTo(times.get(parentIndex)) >= 0) {
                return false;
            } else {
                do {
                    swap(index, reaction, parentIndex, parent);
                    index = parentIndex;
                    parentIndex = getParent(index);
                    if (parentIndex == -1) {
                        return true;
                    }
                    parent = tree.get(parentIndex);
                } while (newTime.compareTo(times.get(parentIndex)) < 0);
                return true;
            }
        }
    }

    private void updateEffectively(final Actionable<T> reaction, final int index) {
        if (!up(reaction, index)) {
            down(reaction, index);
        }
    }

    @Override
    public void updateReaction(final Actionable<T> reaction) {
        final int index = indexes.get(reaction);
        if (index != indexes.getNoEntryValue()) {
            times.set(index, reaction.getTau());
            updateEffectively(reaction, index);
        }
    }

    private static int getParent(final int i) {
        if (i == 0) {
            return -1;
        }
        return (i - 1) / 2;
    }

}
