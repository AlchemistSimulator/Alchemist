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
package it.unibo.alchemist.model.implementations.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.danilopianini.concurrency.ThreadLocalIdGenerator;

import com.google.common.collect.MapMaker;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;


/**
 * This class realizes an abstract node. You may extend it to realize your own
 * nodes.
 * 
 * @param <T>
 */
public abstract class GenericNode<T> implements Node<T> {

    private static final long serialVersionUID = 2496775909028222278L;
    private static final ConcurrentMap<Environment<?>, AtomicInteger> IDGENERATOR = new MapMaker()
            .weakKeys().makeMap();
    private static final Semaphore MUTEX = new Semaphore(1);
    private static final ThreadLocalIdGenerator SINGLETON = new ThreadLocalIdGenerator();
    private static final AtomicInteger THREAD_UNSAFE = new AtomicInteger();
    private final int id;
    private final List<Reaction<T>> reactions = new ArrayList<>();
    private final ConcurrentMap<Molecule, T> molecules = new ConcurrentLinkedHashMap.Builder<Molecule, T>()
            .maximumWeightedCapacity(Long.MAX_VALUE)
            .concurrencyLevel(2)
            .build();

    private static int idFromEnv(final Environment<?> env) {
        MUTEX.acquireUninterruptibly();
        AtomicInteger idgen = IDGENERATOR.get(Objects.requireNonNull(env));
        if (idgen == null) {
            idgen = new AtomicInteger();
            IDGENERATOR.put(env, idgen);
        }
        MUTEX.release();
        return idgen.getAndIncrement();
    }

    /**
     * Basically, builds the node and just caches the hash code.
     * 
     * @param threadLocal
     *            true if the id should be local to the current thread. In order
     *            to keep the node ids along multiple simulations, pass true and
     *            use different threads to instance the nodes.
     * @deprecated this constructor can not generate ids correctly in case
     *             batches of simulation are executed by the same thread, and as
     *             such it has been deprecated and is scheduled for removal at
     *             the next major release.
     */
    @Deprecated
    protected GenericNode(final boolean threadLocal) {
        this(threadLocal ? SINGLETON.genId() : THREAD_UNSAFE.getAndIncrement());
    }

    /**
     * @param env
     *            the environment, used to generate sequential ids for each
     *            environment, always starting from 0.
     */
    public GenericNode(final Environment<?> env) {
        this(idFromEnv(env));
    }

    private GenericNode(final int id) {
        this.id = id;
    }

    @Override
    public void addReaction(final Reaction<T> r) {
        reactions.add(r);
    }

    @Override
    public GenericNode<T> cloneNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(final Node<T> o) {
        if (o instanceof GenericNode<?>) {
            if (id > ((GenericNode<?>) o).id) {
                return 1;
            }
            if (id < ((GenericNode<?>) o).id) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public boolean contains(final Molecule m) {
        return molecules.containsKey(m);
    }

    /**
     * @return an empty concentration
     */
    protected abstract T createT();

    @Override
    public boolean equals(final Object o) {
        if (o instanceof GenericNode<?>) {
            return ((GenericNode<?>) o).id == id;
        }
        return false;
    }

    @Override
    public void forEach(final Consumer<? super Reaction<T>> action) {
        reactions.forEach(action);
    }

    @Override
    public int getChemicalSpecies() {
        return molecules.size();
    }

    @Override
    public T getConcentration(final Molecule mol) {
        final T res = molecules.get(mol);
        if (res == null) {
            return createT();
        }
        return res;
    }

    @Override
    public Map<Molecule, T> getContents() {
        return Collections.unmodifiableMap(molecules);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public List<Reaction<T>> getReactions() {
        return reactions;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Iterator<Reaction<T>> iterator() {
        return reactions.iterator();
    }

    @Override
    public void removeConcentration(final Molecule mol) {
        molecules.remove(mol);
    }

    @Override
    public void removeReaction(final Reaction<T> r) {
        reactions.remove(r);
    }

    @Override
    public void setConcentration(final Molecule mol, final T c) {
        molecules.put(mol, c);
    }

    @Override
    public Spliterator<Reaction<T>> spliterator() {
        return reactions.spliterator();
    }

    @Override
    public String toString() {
        return molecules.toString();
    }

}
