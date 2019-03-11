/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/**
 * 
 */
package it.unibo.alchemist.core.implementations;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unibo.alchemist.core.interfaces.Scheduler;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This class implements the indexed priority queue through an Array.
 * 
 * @param <T>
 */
public final class ArrayIndexedPriorityQueue<T> implements Scheduler<T> {

    private static final long serialVersionUID = 8064379974084348391L;

    private final TObjectIntMap<Reaction<T>> indexes = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final List<Time> times = new ArrayList<>();
    private final List<Reaction<T>> tree = new ArrayList<>();

    private static int getParent(final int i) {
        if (i == 0) {
            return -1;
        }
        return (i - 1) / 2;
    }

    @Override
    public void addReaction(final Reaction<T> r) {
        tree.add(r);
        times.add(r.getTau());
        final int index = tree.size() - 1;
        indexes.put(r, index);
        updateEffectively(r, index);
    }

    private void down(final Reaction<T> r, final int i) {
        int index = i;
        final Time newTime = r.getTau();
        do {
            int minIndex = 2 * index + 1;
            if (minIndex > tree.size() - 1) {
                return;
            }
            Time minTime = times.get(minIndex);
            Reaction<T> min = tree.get(minIndex);
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
        } while (true);
    }

    @Override
    public Reaction<T> getNext() {
        Reaction<T> res = null;
        if (!tree.isEmpty()) {
            res = tree.get(0);
        }
        return res;
    }

    @Override
    public void removeReaction(final Reaction<T> r) {
        final int index = indexes.get(r);
        final int last = tree.size() - 1;
        if (index == last) {
            tree.remove(index);
            indexes.remove(r);
            times.remove(index);
        } else {
            final Reaction<T> swapped = tree.get(last);
            indexes.put(swapped, index);
            tree.set(index, swapped);
            times.set(index, swapped.getTau());
            tree.remove(last);
            times.remove(last);
            indexes.remove(r);
            updateEffectively(swapped, index);
        }
    }

    private void swap(final int i1, final Reaction<T> r1, final int i2, final Reaction<T> r2) {
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
            final int tabulars = (int) (Math.floor(Math.log(tree.size()) / Math.log(2)) - Math.floor(Math.log(i + 1) / Math.log(2))) + 1;
            for (int t = 0; t < tabulars; t++) {
                sb.append('\t');
            }
            sb.append(times.get(i));
            if (i == pow) {
                exp++;
                pow = pow + (int) Math.pow(2, exp);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private boolean up(final Reaction<T> r, final int i) {
        int index = i;
        int parentIndex = getParent(index);
        final Time newTime = r.getTau();
        if (parentIndex == -1) {
            return false;
        } else {
            Reaction<T> parent = tree.get(parentIndex);
            if (newTime.compareTo(times.get(parentIndex)) >= 0) {
                return false;
            } else {
                do {
                    swap(index, r, parentIndex, parent);
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

    private void updateEffectively(final Reaction<T> r, final int index) {
        if (!up(r, index)) {
            down(r, index);
        }
    }

    @Override
    public void updateReaction(final Reaction<T> r) {
        final int index = indexes.get(r);
        if (index != indexes.getNoEntryValue()) {
            times.set(index, r.getTau());
            updateEffectively(r, index);
        }
    }

}
