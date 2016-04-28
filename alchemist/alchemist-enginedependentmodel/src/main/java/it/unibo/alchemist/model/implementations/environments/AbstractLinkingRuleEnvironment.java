/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

import java.util.LinkedHashSet;
import java.util.Set;

import org.danilopianini.lang.SpatialIndex;

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
        final Neighborhood<T> neigh = computeEnvironment(node);
        updateNeighbors(neigh);
        /*
         * Reaction and dependencies creation on the engine. This must be
         * executed only when the neighborhoods have been correctly computed.
         */
        Engine.nodeAdded(this, node);
        /*
         * Call the subclass method.
         */
        nodeAdded(node, p, neigh);
    }

    /**
     * This method gets called once a node has been added, and its neighborhood has been computed and memorized.
     * 
     * @param node the node
     * @param position the position of the node
     * @param neighborhood the current neighborhood of the node
     */
    protected abstract void nodeAdded(Node<T> node, Position position, Neighborhood<T> neighborhood);

    /**
     * Produces a new neighborhood after a node movement operation.
     * 
     * @param center
     *            the node to recompute
     * @return the new neighborhood
     */
    protected Neighborhood<T> computeEnvironment(final Node<T> center) {
        assert center != null;
        assert rule != null;
        final Neighborhood<T> neigh = rule.computeNeighborhood(center, this);
        updateNeighbors(neigh);
        neighCache.put(center.getId(), neigh);
        return neigh;
    }

    @Override
    public LinkingRule<T> getLinkingRule() {
        return rule;
    }

    @Override
    public Neighborhood<T> getNeighborhood(final Node<T> center) {
        assert center != null;
        return neighCache.get(center.getId());
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
        rule = r;
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
         * normally considered global. This because for each node wich is
         * detached, all the dependencies are updated, ensuring the soundness.
         */
        final Neighborhood<T> oldNeighborhood = neighCache.get(node.getId());
        final Neighborhood<T> newNeighborhood = computeEnvironment(node);
        /*
         * oldclone will contain the neighbors this node still have in the new
         * neighborhood
         */
        final Set<? extends Node<T>> oldclone = new LinkedHashSet<>(oldNeighborhood.getNeighbors());
        for (final Node<T> n : oldNeighborhood) {
            if (!newNeighborhood.contains(n)) {
                /*
                 * Neighbor lost
                 */
                oldclone.remove(n);
                Engine.neighborRemoved(this, node, n);
                neighCache.get(n.getId()).removeNeighbor(node);
            }
        }
        for (final Node<T> n : newNeighborhood) {
            if (!oldclone.contains(n)) {
                /*
                 * If it's a whole new neighbor
                 */
                Engine.neighborAdded(this, node, n);
            }
        }

    }

    /**
     * Scans the neighborhood, and for each neighbor ensures that the link is
     * bidirectional.
     * 
     * @param neigh
     *            the neighborhood to scan
     */
    protected void updateNeighbors(final Neighborhood<T> neigh) {
        final Node<T> node = neigh.getCenter();
        for (final Node<T> n : neigh) {
            final Neighborhood<T> list = neighCache.get(n.getId());
            if (!list.contains(node)) {
                list.addNeighbor(node);
            }
        }
    }

}
