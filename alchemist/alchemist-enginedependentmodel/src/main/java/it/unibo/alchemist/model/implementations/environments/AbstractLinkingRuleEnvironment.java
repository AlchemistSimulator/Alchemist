/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.danilopianini.util.SpatialIndex;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * An environment that links nodes according to an external rule.
 * 
 * @param <T>
 */
public abstract class AbstractLinkingRuleEnvironment<T> extends AbstractEnvironment<T> {

    private static final long serialVersionUID = -7298218653879976550L;
    private final TIntObjectHashMap<Neighborhood<T>> neighCache = new TIntObjectHashMap<>();
    private LinkingRule<T> rule;

    /**
     * @param internalIndex the spatial index that should be used
     */
    protected AbstractLinkingRuleEnvironment(final SpatialIndex<Node<T>> internalIndex) {
        super(internalIndex);
    }

    @Override
    protected final void nodeAdded(final Node<T> node, final Position p) {
        assert node != null;
        assert p != null;
        /*
         * Neighborhood computation
         */
        updateNeighborhood(node);
        /*
         * Reaction and dependencies creation on the engine. This must be
         * executed only when the neighborhoods have been correctly computed.
         */
        Engine.nodeAdded(this, node);
        /*
         * Call the subclass method.
         */
        nodeAdded(node, p, getNeighborhood(node));
    }

    /**
     * This method gets called once a node has been added, and its neighborhood has been computed and memorized.
     * 
     * @param node the node
     * @param position the position of the node
     * @param neighborhood the current neighborhood of the node
     */
    protected abstract void nodeAdded(Node<T> node, Position position, Neighborhood<T> neighborhood);

    @Override
    public LinkingRule<T> getLinkingRule() {
        return rule;
    }

    @Override
    public Neighborhood<T> getNeighborhood(@Nonnull final Node<T> center) {
        return neighCache.get(Objects.requireNonNull(center).getId());
    }

    /**
     * @return a pointer to the neighborhoods cache structure
     */
    protected TIntObjectHashMap<Neighborhood<T>> getNeighborsCache() {
        return neighCache;
    }

    @Override
    protected final void nodeRemoved(final Node<T> node, final Position pos) {
        assert node != null;
        /*
         * Neighborhood update
         */
        final Neighborhood<T> neigh = neighCache.remove(node.getId());
        for (final Node<T> n : neigh) {
            neighCache.get(n.getId()).removeNeighbor(node);
        }
        /*
         * Update all the reactions which may have been affected by the node
         * removal
         */
        Engine.nodeRemoved(this, node, neigh);
        /*
         * Call subclass remover
         */
        nodeRemoved(node, neigh);
    }

    /**
     * This method gets called once a node has been removed.
     * 
     * @param node
     *            the node
     * @param neighborhood
     *            the OLD neighborhood of the node (it is no longer in sync with
     *            the {@link Environment} status)
     */
    protected abstract void nodeRemoved(final Node<T> node, Neighborhood<T> neighborhood);

    @Override
    public void setLinkingRule(final LinkingRule<T> r) {
        rule = Objects.requireNonNull(r);
    }

    /**
     * After a node movement, recomputes the neighborhood, also notifying the
     * running simulation about the modifications. This allows movement actions
     * to be defined as LOCAL (they should be normally considered GLOBAL).
     * 
     * @param node
     *            the node that has been moved
     */
    protected final void updateNeighborhood(final Node<T> node) {
        /*
         * The following optimization allows to define as local the context of
         * reactions which are actually including a move, which should be
         * normally considered global. This because for each node which is
         * detached, all the dependencies are updated, ensuring the soundness.
         */
        if (Objects.requireNonNull(rule).isLocallyConsistent()) {
            final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(node), this);
            final Neighborhood<T> oldNeighborhood = neighCache.put(node.getId(), newNeighborhood);
            if (oldNeighborhood != null) {
                final Iterator<Node<T>> iter = oldNeighborhood.iterator();
                while (iter.hasNext()) {
                    final Node<T> neighbor = iter.next();
                    if (!newNeighborhood.contains(neighbor)) {
                        /*
                         * Neighbor lost
                         */
                        iter.remove();
                        final Neighborhood<T> neighborsNeighborhood = neighCache.get(neighbor.getId());
                        neighborsNeighborhood.removeNeighbor(node);
                        Engine.neighborRemoved(this, node, neighbor);
                    }
                }
            }
            for (final Node<T> n : newNeighborhood) {
                if (oldNeighborhood == null || !oldNeighborhood.contains(n)) {
                    /*
                     * If it's a new neighbor
                     */
                    neighCache.get(n.getId()).addNeighbor(node);
                    Engine.neighborAdded(this, node, n);
                }
            }
        } else {
            final Queue<Operation> operations = recursiveOperation(node);
            final TIntSet processed = new TIntHashSet(getNodesNumber());
            processed.add(node.getId());
            while (!operations.isEmpty()) {
                final Operation next = operations.poll();
                final Node<T> dest = next.destination;
                final int destId = dest.getId();
                if (!processed.contains(destId)) {
                    operations.addAll(recursiveOperation(next.origin, next.destination, next.isAdd));
                    processed.add(destId);
                }
            }
        }
    }

    private Queue<Operation> recursiveOperation(final Node<T> origin) {
        final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(origin), this);
        final Neighborhood<T> oldNeighborhood = neighCache.put(origin.getId(), newNeighborhood);
        return toQueue(origin, oldNeighborhood, newNeighborhood);
    }

    private Queue<Operation> toQueue(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return Stream.concat(
                lostNeighbors(center, oldNeighborhood, newNeighborhood),
                foundNeighbors(center, oldNeighborhood, newNeighborhood))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Queue<Operation> recursiveOperation(final Node<T> origin, final Node<T> destination, final boolean isAdd) {
        if (isAdd) {
            Engine.neighborAdded(this, origin, destination);
        } else {
            Engine.neighborRemoved(this, origin, destination);
        }
        final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(destination), this);
        final Neighborhood<T> oldNeighborhood = neighCache.put(destination.getId(), newNeighborhood);
        return toQueue(destination, oldNeighborhood, newNeighborhood);
    }

    private Stream<Operation> lostNeighbors(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return Optional.ofNullable(oldNeighborhood)
        .map(Neighborhood::getNeighbors)
        .orElse(Collections.emptyList())
        .stream()
        .filter(neigh -> !newNeighborhood.contains(neigh))
        .filter(neigh -> getNeighborhood(neigh).contains(center))
        .map(n -> new Operation(center, n, false));
    }

    private Stream<Operation> foundNeighbors(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return newNeighborhood.getNeighbors().stream()
                .filter(neigh -> oldNeighborhood == null || !oldNeighborhood.contains(neigh))
                .filter(neigh -> !getNeighborhood(neigh).contains(center))
                .map(n -> new Operation(center, n, true));
    }

    private class Operation {
        private final Node<T> origin;
        private final Node<T> destination;
        private final boolean isAdd;
        Operation(final Node<T> origin, final Node<T> destination, final boolean isAdd) {
            this.origin = origin;
            this.destination = destination;
            this.isAdd = isAdd;
        }
        @Override
        public String toString() {
            return origin + (isAdd ? " discovered " : " lost ") + destination;
        }
    }
}
