/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.implementations;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unibo.alchemist.core.interfaces.Scheduler;
import it.unibo.alchemist.model.interfaces.GlobalReaction;
import it.unibo.alchemist.model.interfaces.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the indexed priority queue through an Array.
 * 
 * @param <T> concentration type
 */
public final class ArrayIndexedPriorityQueue<T> implements Scheduler<T> {

    private static final long serialVersionUID = 8064379974084348391L;

    private final TObjectIntMap<GlobalReaction<T>> indexes =
            new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final List<Time> times = new ArrayList<>();
    private final List<GlobalReaction<T>> tree = new ArrayList<>();

    private static int getParent(final int i) {
        if (i == 0) {
            return -1;
        }
        return (i - 1) / 2;
    }

    @Override
    public void addReaction(final GlobalReaction<T> reaction) {
        tree.add(reaction);
        times.add(reaction.getTau());
        final int index = tree.size() - 1;
        indexes.put(reaction, index);
        updateEffectively(reaction, index);
    }

    private void down(final GlobalReaction<T> r, final int i) {
        int index = i;
        final Time newTime = r.getTau();
        while (true) {
            int minIndex = 2 * index + 1;
            if (minIndex > tree.size() - 1) {
                return;
            }
            Time minTime = times.get(minIndex);
            GlobalReaction<T> min = tree.get(minIndex);
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
                swap(index, r, minIndex, min);
                index = minIndex;
            } else {
                return;
            }
        }
    }

    @Override
    public GlobalReaction<T> getNext() {
        GlobalReaction<T> res = null;
        if (!tree.isEmpty()) {
            res = tree.get(0);
        }
        return res;
    }

    @Override
    public void removeReaction(final GlobalReaction<T> r) {
        final int index = indexes.get(r);
        final int last = tree.size() - 1;
        if (index == last) {
            tree.remove(index);
            indexes.remove(r);
            times.remove(index);
        } else {
            final GlobalReaction<T> swapped = tree.get(last);
            indexes.put(swapped, index);
            tree.set(index, swapped);
            times.set(index, swapped.getTau());
            tree.remove(last);
            times.remove(last);
            indexes.remove(r);
            updateEffectively(swapped, index);
        }
    }

    private void swap(final int i1, final GlobalReaction<T> r1, final int i2, final GlobalReaction<T> r2) {
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

    private boolean up(final GlobalReaction<T> reaction, final int i) {
        int index = i;
        int parentIndex = getParent(index);
        final Time newTime = reaction.getTau();
        if (parentIndex == -1) {
            return false;
        } else {
            GlobalReaction<T> parent = tree.get(parentIndex);
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

    private void updateEffectively(final GlobalReaction<T> reaction, final int index) {
        if (!up(reaction, index)) {
            down(reaction, index);
        }
    }

    @Override
    public void updateReaction(final GlobalReaction<T> reaction) {
        final int index = indexes.get(reaction);
        if (index != indexes.getNoEntryValue()) {
            times.set(index, reaction.getTau());
            updateEffectively(reaction, index);
        }
    }

}
