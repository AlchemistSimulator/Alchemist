/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.environments;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.danilopianini.util.ArrayListSet;
import org.danilopianini.util.LinkedListSet;
import org.danilopianini.util.ListSet;
import org.danilopianini.util.ListSets;
import org.danilopianini.util.SpatialIndex;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Sets;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import it.unibo.alchemist.SupportedIncarnations;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.LinkingRule;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Very generic and basic implementation for an environment. Basically, only
 * manages an internal set of nodes and their position.
 * 
 * @param <T>
 *            concentration type
 * @param <P>
 *            {@link Position} type
 * 
 */
public abstract class AbstractEnvironment<T, P extends Position<P>> implements Environment<T, P> {

    private static final long serialVersionUID = 0L;
    private transient LoadingCache<ImmutablePair<P, Double>, ListSet<Node<T>>> cache;
    private transient Incarnation<T, P> incarnation;
    private final Map<Molecule, Layer<T, P>> layers = new LinkedHashMap<>();
    private final TIntObjectHashMap<Neighborhood<T>> neighCache = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<Node<T>> nodes = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<P> nodeToPos = new TIntObjectHashMap<>();
    private LinkingRule<T, P> rule;
    private transient Simulation<T, P> simulation;
    private final SpatialIndex<Node<T>> spatialIndex;
    private Predicate<Environment<T, P>> terminator = (Predicate<Environment<T, P>> & Serializable) c -> false;

    /**
     * @param internalIndex
     *            the {@link SpatialIndex} to use in order to efficiently
     *            retrieve nodes.
     */
    protected AbstractEnvironment(final SpatialIndex<Node<T>> internalIndex) {
        spatialIndex = Objects.requireNonNull(internalIndex);
    }

    @Override
    public final void addLayer(final Molecule m, final Layer<T, P> l) {
        if (layers.put(m, l) != null) {
            throw new IllegalStateException("Two layers have been associated to " + m);
        }
    }
 
    @Override
    public final void addNode(final Node<T> node, final P p) {
        if (nodeShouldBeAdded(node, p)) {
            final P actualPosition = computeActualInsertionPosition(node, p);
            setPosition(node, actualPosition);
            if (nodes.put(node.getId(), node) != null) {
                throw new IllegalArgumentException("Node with id " + node.getId() + " was already existing in this environment.");
            }
            spatialIndex.insert(node, actualPosition.getCartesianCoordinates());
            /*
             * Neighborhood computation
             */
            updateNeighborhood(node, true);
            /*
             * Reaction and dependencies creation on the engine. This must be
             * executed only when the neighborhoods have been correctly computed,
             * and only if a simulation engine has actually been attached.
             */
            ifEngineAvailable(s -> s.nodeAdded(node));
            /*
             * Call the subclass method.
             */
            nodeAdded(node, p, getNeighborhood(node));
        }
    }

    @Override
    public final void addTerminator(final Predicate<Environment<T, P>> terminator) {
        this.terminator = this.terminator.or((Serializable & Predicate<Environment<T, P>>) terminator);
    }

    /**
     * Allows subclasses to tune the actual position of a node, applying spatial
     * constrains at node addition.
     * 
     * @param node
     *            the node
     * @param p
     *            the original (requested) position
     * @return the actual position where the node should be located
     */
    protected abstract P computeActualInsertionPosition(Node<T> node, P p);

    @Override
    public final void forEach(final Consumer<? super Node<T>> action) {
        getNodes().forEach(action);
    }

    private Stream<Operation> foundNeighbors(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return newNeighborhood.getNeighbors().stream()
                .filter(neigh -> oldNeighborhood == null || !oldNeighborhood.contains(neigh))
                .filter(neigh -> !getNeighborhood(neigh).contains(center))
                .map(n -> new Operation(center, n, true));
    }

    private ListSet<Node<T>> getAllNodesInRange(final P center, final double range) {
        if (range <= 0) {
            throw new IllegalArgumentException("Range query must be positive (provided: " + range + ")");
        }
        if (cache == null) {
            cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .build(pair -> runQuery(pair.left, pair.right));
        }
        return cache.get(new ImmutablePair<>(center, range));
    }

    @Override
    public final double getDistanceBetweenNodes(final Node<T> n1, final Node<T> n2) {
        return getPosition(n1).getDistanceTo(getPosition(n2));
    }

    @Override
    public final Optional<Incarnation<T, P>> getIncarnation() {
        return Optional.ofNullable(incarnation);
    }

    @Override
    public final Optional<Layer<T, P>> getLayer(final Molecule m) {
        return Optional.ofNullable(layers.get(m));
    }

    @Override
    public final ListSet<Layer<T, P>> getLayers() {
        return new ArrayListSet<>(layers.values());
    }

    @Override
    public final LinkingRule<T, P> getLinkingRule() {
        return rule;
    }

    @Override
    public final Neighborhood<T> getNeighborhood(@Nonnull final Node<T> center) {
        final Neighborhood<T> result = neighCache.get(Objects.requireNonNull(center).getId());
        if (result == null) {
            if (getNodes().contains(center)) {
                throw new IllegalStateException("The environment state is inconsistent. " + center + " is among the nodes, but apparently has no position.");
            }
            throw new IllegalArgumentException(center + " is not part of the environment.");
        }
        return result;
    }

    /**
     * @return a pointer to the neighborhoods cache structure
     */
    protected final TIntObjectHashMap<Neighborhood<T>> getNeighborsCache() {
        return neighCache;
    }


    @Override
    public final Node<T> getNodeByID(final int id) {
        return nodes.get(id);
    }

    @Override
    public final ListSet<Node<T>> getNodes() {
        return new ArrayListSet<>(nodes.valueCollection());
    }

    @Override
    public final int getNodesNumber() {
        return nodes.size();
    }

    @Override
    public final ListSet<Node<T>> getNodesWithinRange(final Node<T> center, final double range) {
        final P centerPosition = getPosition(center);
        if (centerPosition == null) {
            throw new IllegalArgumentException("Node " + center + " was not part of this environment");
        }
        final ListSet<Node<T>> res = new LinkedListSet<>(getAllNodesInRange(centerPosition, range));
        if (!res.remove(center)) {
            throw new IllegalStateException("Either the provided range (" + range + ") is too small"
                    + " for queries to work without losses of precision, or the environment is an inconsistent state."
                    + " Node " + center + " located at " + centerPosition + " was the query center, but within range "
                    + range + " only nodes " + res + " were found in the environment.");
        }
        return res;
    }

    @Override
    public final ListSet<Node<T>> getNodesWithinRange(final P center, final double range) {
        /*
         * Collect every node in range
         */
        return getAllNodesInRange(center, range);
    }

    /**
     * This method should not get overriden in general. However, if your
     */
    @Override
    public P getPosition(final Node<T> node) {
        return nodeToPos.get(Objects.requireNonNull(node).getId());
    }

    @Override
    public final Simulation<T, P> getSimulation() {
        return simulation;
    }

    /**
     * Override this method if units measuring distance do not match with units used
     * for coordinates. For instance, if your space is non-Euclidean, or if you are
     * using polar coordinates. A notable example is using geographical
     * latitude-longitude as y-x coordinates and meters as distance measure.
     */
    @Override
    public double[] getSizeInDistanceUnits() {
        return getSize();
    }

    /**
     * If this environment is attached to a simulation engine, executes consumer.
     *
     * @param action  the {@link Consumer} to execute
     */
    protected final void ifEngineAvailable(final Consumer<Simulation<T, P>> action) {
        Optional.ofNullable(getSimulation()).ifPresent(action);
    }

    private void invalidateCache() {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    @Override
    public final boolean isTerminated() {
        return terminator.test(this);
    }

    @Override
    public final Iterator<Node<T>> iterator() {
        return Collections.unmodifiableCollection(nodes.valueCollection()).iterator();
    }

    private Stream<Operation> lostNeighbors(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return Optional.ofNullable(oldNeighborhood)
            .map(Neighborhood::getNeighbors)
            .orElse(ListSets.emptyListSet())
            .stream()
            .filter(neigh -> !newNeighborhood.contains(neigh))
            .filter(neigh -> getNeighborhood(neigh).contains(center))
            .map(n -> new Operation(center, n, false));
    }

    /**
     * This method gets called once a node has been added, and its neighborhood has been computed and memorized.
     * 
     * @param node the node
     * @param position the position of the node
     * @param neighborhood the current neighborhood of the node
     */
    protected abstract void nodeAdded(Node<T> node, P position, Neighborhood<T> neighborhood);

    /**
     * This method gets called once a node has been removed.
     * 
     * @param node
     *            the node
     * @param neighborhood
     *            the OLD neighborhood of the node (it is no longer in sync with
     *            the {@link Environment} status)
     */
    protected void nodeRemoved(final Node<T> node, final Neighborhood<T> neighborhood) { }

    /**
     * Allows subclasses to determine wether or not a {@link Node} should
     * actually get added to this environment.
     * 
     * @param node
     *            the node
     * @param p
     *            the original (requested) position
     * @return true if the node should be added to this environment, false
     *         otherwise
     */
    protected boolean nodeShouldBeAdded(final Node<T> node, final P p) {
        return true;
    }

    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        final String name = in.readObject().toString();
        incarnation = SupportedIncarnations.<T, P>get(name).orElseThrow(() ->
            new IllegalStateException("Unknown incarnation " + name)
        );
    }

    private Queue<Operation> recursiveOperation(final Node<T> origin) {
        final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(origin), this);
        final Neighborhood<T> oldNeighborhood = neighCache.put(origin.getId(), newNeighborhood);
        return toQueue(origin, oldNeighborhood, newNeighborhood);
    }

    private Queue<Operation> recursiveOperation(final Node<T> origin, final Node<T> destination, final boolean isAdd) {
        if (isAdd) {
            ifEngineAvailable(s -> s.neighborAdded(origin, destination));
        } else {
            ifEngineAvailable(s -> s.neighborRemoved(origin, destination));
        }
        final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(destination), this);
        final Neighborhood<T> oldNeighborhood = neighCache.put(destination.getId(), newNeighborhood);
        return toQueue(destination, oldNeighborhood, newNeighborhood);
    }

    @Override
    public final void removeNode(@Nonnull final Node<T> node) {
        invalidateCache();
        nodes.remove(Objects.requireNonNull(node).getId());
        final P pos = nodeToPos.remove(node.getId());
        spatialIndex.remove(node, pos.getCartesianCoordinates());
        /*
         * Neighborhood update
         */
        final Neighborhood<T> neigh = neighCache.remove(node.getId());
        for (final Node<T> n : neigh) {
            final Neighborhood<T> target = neighCache.remove(n.getId());
            neighCache.put(n.getId(), target.remove(node));
        }
        /*
         * Update all the reactions which may have been affected by the node
         * removal
         */
        ifEngineAvailable(s -> s.nodeRemoved(node, neigh));
        /*
         * Call subclass remover
         */
        nodeRemoved(node, neigh);
    }

    private ListSet<Node<T>> runQuery(final P center, final double range) {
        final List<Node<T>> result = spatialIndex.query(center.boundingBox(range).stream()
                .map(Position::getCartesianCoordinates)
                .toArray(double[][]::new));
        final int size = result.size();
        return ListSets.unmodifiableListSet(result.stream()
            .filter(it -> getPosition(it).getDistanceTo(center) <= range)
            .collect(Collectors.toCollection(() -> new ArrayListSet<>(size))));
    }

    @Override
    public final void setIncarnation(final Incarnation<T, P> incarnation) {
        if (this.incarnation == null) {
            this.incarnation = Objects.requireNonNull(incarnation);
        } else {
            throw new IllegalStateException("The Environment has already been equipeed with an incarnation: " + this.incarnation);
        }
    }

    @Override
    public final void setLinkingRule(final LinkingRule<T, P> r) {
        rule = Objects.requireNonNull(r);
    }

    /**
     * Adds or changes a position entry in the position map.
     * 
     * @param n
     *            the node
     * @param p
     *            its new position
     */
    protected final void setPosition(final Node<T> n, final P p) {
        final P pos = nodeToPos.put(Objects.requireNonNull(n).getId(), Objects.requireNonNull(p));
        if (!p.equals(pos)) {
            invalidateCache();
        }
        if (pos != null && !spatialIndex.move(n, pos.getCartesianCoordinates(), p.getCartesianCoordinates())) {
            throw new IllegalArgumentException("Tried to move a node not previously present in the environment: \n"
                    + "Node: " + n + "\n" + "Requested position" + p);
        }
    }

    @Override
    public final void setSimulation(final Simulation<T, P> s) {
        if (simulation == null) {
            simulation = s;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public final Spliterator<Node<T>> spliterator() {
        return getNodes().spliterator();
    }

    private Queue<Operation> toQueue(final Node<T> center, final Neighborhood<T> oldNeighborhood, final Neighborhood<T> newNeighborhood) {
        return Stream.concat(
                lostNeighbors(center, oldNeighborhood, newNeighborhood),
                foundNeighbors(center, oldNeighborhood, newNeighborhood))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Not used internally. Override as you please.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * After a node movement, recomputes the neighborhood, also notifying the
     * running simulation about the modifications. This allows movement actions
     * to be defined as LOCAL (they should be normally considered GLOBAL).
     * 
     * @param node
     *            the node that has been moved
     * @param  isNewNode
     *            true if the node is a new node, false otherwise
     */
    protected final void updateNeighborhood(final Node<T> node, final boolean isNewNode) {
        /*
         * The following optimization allows to define as local the context of
         * reactions which are actually including a move, which should be
         * normally considered global. This because for each node which is
         * detached, all the dependencies are updated, ensuring the soundness.
         */
        if (Objects.requireNonNull(rule).isLocallyConsistent()) {
            final Neighborhood<T> newNeighborhood = rule.computeNeighborhood(Objects.requireNonNull(node), this);
            final Neighborhood<T> oldNeighborhood = neighCache.put(node.getId(), newNeighborhood);
            /*
             * Remove the node from all lost neighbors' neighborhoods.
             */
            if (oldNeighborhood != null) {
                StreamSupport.stream(oldNeighborhood.spliterator(), false)
                .filter(formerNeighbor -> !newNeighborhood.contains(formerNeighbor))
                .map(this::getNeighborhood)
                .filter(neigh -> neigh.contains(node))
                .forEachOrdered(neighborhoodToChange -> {
                    final Node<T> formerNeighbor = neighborhoodToChange.getCenter();
                    neighCache.put(formerNeighbor.getId(), neighborhoodToChange.remove(node));
                    if (!isNewNode) {
                        ifEngineAvailable(s -> s.neighborRemoved(node, formerNeighbor));
                    }
                });
            }
            /*
             * Add the node to all gained neighbors' neighborhoods
             */
            for (final Node<T> newNeighbor: Sets.difference(newNeighborhood.getNeighbors(),
                    Optional.ofNullable(oldNeighborhood)
                    .map(Neighborhood::getNeighbors)
                    .map(it -> (Set<? extends Node<T>>) it)
                    .orElse(Collections.emptySet()))) {
                neighCache.put(newNeighbor.getId(), neighCache.get(newNeighbor.getId()).add(node));
                if (!isNewNode) {
                    ifEngineAvailable(s -> s.neighborAdded(node, newNeighbor));
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

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(incarnation.getClass().getSimpleName());
    }

    private class Operation {
        private final Node<T> destination;
        private final boolean isAdd;
        private final Node<T> origin;
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
