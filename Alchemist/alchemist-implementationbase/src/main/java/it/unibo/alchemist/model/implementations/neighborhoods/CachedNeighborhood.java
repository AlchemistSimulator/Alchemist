/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.model.implementations.neighborhoods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * A simple implementation of a neighborhood.
 * 
 * @param <T>
 *            The type which describes the concentration of a molecule
 */
public final class CachedNeighborhood<T> implements Neighborhood<T> {

    private static final long serialVersionUID = 2810393824506583972L;
    private final Node<T> c;
    private final Environment<T> env;
    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "False positive, TIntList implements Externalizable that Implements Serializable")
    private final TIntList kCache;
    private final List<Node<T>> k;

    /**
     * Builds a new neighborhood given a central node, its neighbors and the
     * environment.
     * 
     * @param center
     *            the central node
     * @param nodes
     *            the neighbors of the central node
     * @param environment
     *            the environment
     */
    public CachedNeighborhood(final Node<T> center, final Collection<? extends Node<T>> nodes, final Environment<T> environment) {
        this.c = center;
        this.env = environment;
        kCache = new TIntArrayList(nodes.size());
        k = new ArrayList<>(nodes.size());
        for (final Node<T> n : nodes) {
            kCache.add(n.getId());
            k.add(n);
        }
        Collections.sort(k);
        kCache.sort();
    }

    private CachedNeighborhood(final Node<T> center, final TIntList map, final List<Node<T>> l, final Environment<T> environment) {
        this.c = center;
        this.env = environment;
        kCache = map;
        k = l;
    }

    @Override
    public void addNeighbor(final Node<T> neigh) {
        if (!contains(neigh)) {
            int low = 0;
            int high = kCache.size() - 1;
            final int value = neigh.getId();
            while (low <= high) {
                final int mid = (low + high) >>> 1;
                final int midVal = kCache.get(mid);
                if (midVal < value) {
                    low = mid + 1;
                } else if (midVal > value) {
                    high = mid - 1;
                } else {
                    break;
                }
            }
            kCache.insert(low, value);
            k.add(low, neigh);
        }
    }

    @Override
    public CachedNeighborhood<T> clone() throws CloneNotSupportedException {
        return new CachedNeighborhood<T>(c, new TIntArrayList(kCache), new ArrayList<>(k), env);
    }

    @Override
    public boolean contains(final Node<T> n) {
        return contains(n.getId());
    }

    @Override
    public boolean contains(final int n) {
        return kCache.binarySearch(n) >= 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Neighborhood<?>) {
            final Neighborhood<?> n = (Neighborhood<?>) obj;
            return c.equals(n.getCenter()) && getNeighbors().equals(n.getNeighbors());
        }
        return false;
    }

    @Override
    public Set<Node<T>> getBetweenRange(final double min, final double max) {
        final Set<Node<T>> res = new LinkedHashSet<>(k.size() + 1, 1f);
        final Position centerposition = env.getPosition(c);
        for (final Node<T> n : k) {
            final double dist = centerposition.getDistanceTo(env.getPosition(n));
            if (dist < max && dist > min) {
                res.add(n);
            }
        }
        return res;
    }

    @Override
    public Node<T> getCenter() {
        return c;
    }

    @Override
    public Node<T> getNeighborById(final int id) {
        return k.get(kCache.binarySearch((id)));
    }

    @Override
    public Node<T> getNeighborByNumber(final int num) {
        return k.get(num % size());
    }

    @Override
    public List<? extends Node<T>> getNeighbors() {
        return Collections.unmodifiableList(k);
    }

    @Override
    public int hashCode() {
        return c.hashCode() ^ k.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return k.isEmpty();
    }

    @Override
    public Iterator<Node<T>> iterator() {
        return k.iterator();
    }

    @Override
    public void removeNeighbor(final Node<T> neighbor) {
        kCache.remove(neighbor.getId());
        k.remove(neighbor);
    }

    @Override
    public int size() {
        return k.size();
    }

    @Override
    public String toString() {
        return c + " links " + kCache;
    }

    @Override
    public void forEach(final Consumer<? super Node<T>> arg0) {
        k.forEach(arg0);
    }

    @Override
    public Spliterator<Node<T>> spliterator() {
        return k.spliterator();
    }

}
