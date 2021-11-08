/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes;

import com.google.common.collect.MapMaker;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * This class realizes an abstract node. You may extend it to realize your own
 * nodes.
 * 
 * @param <T> concentration type
 */
public abstract class AbstractNode<T> implements Node<T> {

    private static final long serialVersionUID = 2496775909028222278L;
    private static final ConcurrentMap<Environment<?, ?>, AtomicInteger> IDGENERATOR = new MapMaker()
            .weakKeys().makeMap();
    private static final Semaphore MUTEX = new Semaphore(1);
    private final int id;
    private final List<Reaction<T>> reactions = new ArrayList<>();
    private final Map<Molecule, T> molecules = new LinkedHashMap<>();

    private static int idFromEnv(final Environment<?, ?> env) {
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
     * @param env
     *            the environment, used to generate sequential ids for each
     *            environment, always starting from 0.
     */
    public AbstractNode(final Environment<?, ?> env) {
        id = idFromEnv(env);
    }

    @Override
    public final void addReaction(final Reaction<T> reactionToAdd) {
        reactions.add(reactionToAdd);
    }

    /**
     * Default implementation fails: override correctly calling the constructor.
     */
    @Override
    public AbstractNode<T> cloneNode(final Time currentTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int compareTo(@Nonnull final Node<T> other) {
        if (other instanceof AbstractNode<?>) {
            if (id > ((AbstractNode<?>) other).id) {
                return 1;
            }
            if (id < ((AbstractNode<?>) other).id) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Molecule molecule) {
        return molecules.containsKey(molecule);
    }

    /**
     * @return an empty concentration
     */
    protected abstract T createT();

    @Override
    public final boolean equals(final Object other) {
        if (other instanceof AbstractNode<?>) {
            return ((AbstractNode<?>) other).id == id;
        }
        return false;
    }

    @Override
    public final void forEach(final Consumer<? super Reaction<T>> action) {
        reactions.forEach(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMoleculeCount() {
        return molecules.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getConcentration(final Molecule molecule) {
        final T res = molecules.get(molecule);
        if (res == null) {
            return createT();
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Molecule, T> getContents() {
        return Collections.unmodifiableMap(molecules);
    }

    @Override
    public final int getId() {
        return id;
    }

    @Override
    public final List<Reaction<T>> getReactions() {
        return Collections.unmodifiableList(reactions);
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public final Iterator<Reaction<T>> iterator() {
        return reactions.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeConcentration(final Molecule moleculeToRemove) {
        if (molecules.remove(moleculeToRemove) == null) {
            throw new NoSuchElementException(moleculeToRemove + " was not present in node " + getId());
        }
    }

    @Override
    public final void removeReaction(final Reaction<T> reactionToRemove) {
        reactions.remove(reactionToRemove);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConcentration(final Molecule molecule, final T concentration) {
        molecules.put(molecule, concentration);
    }

    @Override
    public final Spliterator<Reaction<T>> spliterator() {
        return reactions.spliterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return molecules.toString();
    }

}
