/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.environments;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.danilopianini.lang.SpatialIndex;


import gnu.trove.map.hash.TIntObjectHashMap;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Very generic and basic implementation for an environment. Basically, only
 * manages an internal set of nodes and their position.
 * 
 * @param <T>
 */
public abstract class AbstractEnvironment<T> implements Environment<T> {

    private static final long serialVersionUID = 2704085518489753349L;
    /**
     * The default monitor that will be loaded. If null, the GUI must default to
     * a compatible monitor.
     */
    protected static final String DEFAULT_MONITOR = null;
    private final TIntObjectHashMap<Position> nodeToPos = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<Node<T>> nodes = new TIntObjectHashMap<Node<T>>();
    private String separator = System.getProperty("line.separator");
    private final SpatialIndex<Node<T>> spatialIndex;
    private final Map<Molecule, Layer<T>> layers = new LinkedHashMap<>();

    /**
     * @param internalIndex
     *            the {@link SpatialIndex} to use in order to efficiently
     *            retrieve nodes.
     */
    protected AbstractEnvironment(final SpatialIndex<Node<T>> internalIndex) {
        spatialIndex = Objects.requireNonNull(internalIndex);
    }

    /**
     * Adds or changes a position entry in the position map.
     * 
     * @param n
     *            the node
     * @param p
     *            its new position
     */
    protected final void setPosition(final Node<T> n, final Position p) {
        final Position pos = nodeToPos.put(n.getId(), p);
        if (pos != null && !spatialIndex.move(n, pos.getCartesianCoordinates(), p.getCartesianCoordinates())) {
            throw new IllegalArgumentException("Tried to move a node not previously present in the environment: \n"
                    + "Node: " + n + "\n" + "Requested position" + p);
        }
    }

    /**
     * This method gets called once that the basic operations for a node
     * addition have been performed by {@link AbstractEnvironment}.
     * 
     * @param node
     *            the node to add
     * @param p
     *            the position
     */
    protected abstract void nodeAdded(final Node<T> node, final Position p);

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
    protected abstract boolean nodeShouldBeAdded(final Node<T> node, final Position p);

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
    protected abstract Position computeActualInsertionPosition(final Node<T> node, final Position p);

    @Override
    public final void addNode(final Node<T> node, final Position p) {
        if (nodeShouldBeAdded(node, p)) {
            final Position actualPosition = computeActualInsertionPosition(node, p);
            setPosition(node, actualPosition);
            nodes.put(node.getId(), node);
            spatialIndex.insert(node, actualPosition.getCartesianCoordinates());
            nodeAdded(node, p);
        }
    }

    /**
     * Deletes a position from the map.
     * 
     * @param node
     *            the node whose position will be removed
     * @return the position removed
     * 
     * @deprecated This method is dangerous and should never be used. Will ne
     *             removed in the next major release
     */
    @Deprecated
    protected final Position getAndDeletePosition(final Node<T> node) {
        Objects.requireNonNull(node);
        return nodeToPos.remove(node.getId());
    }

    @Override
    public final Position getPosition(final Node<T> node) {
        return nodeToPos.get(node.getId());
    }

    /**
     * This method gets called once that the basic operations for a node removal
     * have been performed by {@link AbstractEnvironment}.
     * 
     * @param node
     *            the node to add
     * @param pos
     *            the position
     */
    protected abstract void nodeRemoved(Node<T> node, Position pos);

    @Override
    public final void removeNode(final Node<T> node) {
        nodes.remove(node.getId());
        final Position pos = nodeToPos.remove(node.getId());
        spatialIndex.remove(node, pos.getCartesianCoordinates());
        nodeRemoved(node, pos);
    }

    @Override
    public double getDistanceBetweenNodes(final Node<T> n1, final Node<T> n2) {
        final Position p1 = getPosition(n1);
        final Position p2 = getPosition(n2);
        return p1.getDistanceTo(p2);
    }

    @Override
    public int getNodesNumber() {
        return nodes.size();
    }

    @Override
    public Collection<Node<T>> getNodes() {
        return Collections.unmodifiableCollection(nodes.valueCollection());
    }

    @Override
    public Node<T> getNodeByID(final int id) {
        return nodes.get(id);
    }

    @Override
    public Iterator<Node<T>> iterator() {
        return getNodes().iterator();
    }

    @Override
    public Set<Node<T>> getNodesWithinRange(final Node<T> center, final double range) {
        /*
         * Remove the center node
         */
        return getAllNodesInRange(getPosition(center), range).filter((n) -> !n.equals(center))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<Node<T>> getAllNodesInRange(final Position center, final double range) {
        final List<Position> boundingBox = center.buildBoundingBox(range);
        assert boundingBox.size() == getDimensions();
        final double[][] queryArea = new double[getDimensions()][];
        IntStream.range(0, getDimensions()).parallel()
                .forEach(i -> queryArea[i] = boundingBox.get(i).getCartesianCoordinates());
        return spatialIndex.query(queryArea).parallelStream()
                .filter((n) -> getPosition(n).getDistanceTo(center) <= range);
    }

    @Override
    public Set<Node<T>> getNodesWithinRange(final Position center, final double range) {
        /*
         * Collect every node in range
         */
        return getAllNodesInRange(center, range).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * @return the separator used in toString()
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @param s
     *            the new separator
     */
    public void setSeparator(final String s) {
        separator = s;
    }

    @Override
    public String getPreferredMonitor() {
        return DEFAULT_MONITOR;
    }

    @Override
    public void forEach(final Consumer<? super Node<T>> action) {
        getNodes().forEach(action);
    }

    @Override
    public Spliterator<Node<T>> spliterator() {
        return getNodes().spliterator();
    }

    @Override
    public void addLayer(final Molecule m, final Layer<T> l) {
        if (layers.put(m, l) != null) {
            throw new IllegalStateException("Two layers have been associated to " + m);
        }
    }

    @Override
    public Optional<Layer<T>> getLayer(final Molecule m) {
        return Optional.ofNullable(layers.get(m));
    }

    @Override
    public Set<Layer<T>> getLayers() {
        return layers.values().stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
