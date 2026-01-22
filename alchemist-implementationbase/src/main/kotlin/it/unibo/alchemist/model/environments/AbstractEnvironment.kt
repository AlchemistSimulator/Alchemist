/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import gnu.trove.map.hash.TDoubleObjectHashMap
import gnu.trove.map.hash.TIntObjectHashMap
import gnu.trove.set.hash.TIntHashSet
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableMutableMap
import it.unibo.alchemist.model.observation.ObservableMutableSet
import it.unibo.alchemist.model.observation.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.model.observation.ObservableSet
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serial
import java.util.Objects
import java.util.Spliterator
import java.util.function.Consumer
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.LinkedListSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets
import org.danilopianini.util.SpatialIndex

/**
 * Very generic and basic implementation for an environment. Basically, only
 * manages an internal set of nodes and their position.
 *
 * @param <T>
 * concentration type
 * @param <P>
 * [it.unibo.alchemist.model.Position] type
</P></T> */
abstract class AbstractEnvironment<T, P : Position<P>> protected constructor(
    incarnation: Incarnation<T, P>,
    internalIndex: SpatialIndex<Node<T>>,
) : Environment<T, P> {
    private val _nodes: ListSet<Node<T>> = ArrayListSet()
    private val _globalReactions = ArrayListSet<GlobalReaction<T>>()
    final override var layers: Map<Molecule, Layer<T, P>> = LinkedHashMap()
        private set

    @Transient
    private val observableNeighCache = ObservableMutableMap<Int, Neighborhood<T>>()
    private val neighCache = TIntObjectHashMap<Neighborhood<T>>()

    @Transient
    private val observableNodeToPos = ObservableMutableMap<Int, P>()
    private val nodeToPos = TIntObjectHashMap<P>()

    private val spatialIndex: SpatialIndex<Node<T>> = internalIndex

//    override val layers: Map<Molecule, Layer<T, P>> get() = _layers

    override val globalReactions: ListSet<GlobalReaction<T>>
        get() = ListSets.unmodifiableListSet(_globalReactions)

    override val nodes: ListSet<Node<T>> = ListSets.unmodifiableListSet(_nodes)

    @Transient
    override val observableNodes: ObservableMutableSet<Node<T>> = _nodes.toList().toObservableSet()

    @Transient
    final override val nodeCount: Observable<Int> = observableNodes.observableSize

    private val regionObservers = ArrayList<RegionObserver>()

    private val regionNodeCenteredIndex = TIntObjectHashMap<TDoubleObjectHashMap<RegionObserver>>()

    private val regionPositionCenteredIndex = HashMap<P, TDoubleObjectHashMap<RegionObserver>>()

    final override var linkingRule: LinkingRule<T, P> = NoLinks()

    @Transient
    private var cache: LoadingCache<Pair<P, Double>, List<Node<T>>>? = null

    @Transient
    final override var incarnation: Incarnation<T, P> = requireNotNull(incarnation)
        private set

    @Transient
    final override var simulationOrNull: Simulation<T, P>? = null
        private set

    final override var simulation: Simulation<T, P>
        get() =
            requireNotNull(simulationOrNull) {
                "This environment is not attached to any simulation."
            }
        set(value) {
            if (simulationOrNull == null) {
                simulationOrNull = value
            } else {
                check(simulationOrNull == value) {
                    "Inconsistent simulation configuration for $this: simulation was set to " +
                        "$simulationOrNull (id: ${System.identityHashCode(simulationOrNull)}) " +
                        "and then switched to $value (id: ${System.identityHashCode(value)})"
                }
            }
        }

    private var terminationPredicate: TerminationPredicate<T, P> = TerminationPredicate { false }

    init {
        this.incarnation = requireNotNull(incarnation)
    }

    override fun addLayer(molecule: Molecule, layer: Layer<T, P>) {
        check(molecule !in layers.keys) { "A layer for $molecule was already associated to this environment." }
        layers += molecule to layer
    }

    override fun addGlobalReaction(reaction: GlobalReaction<T>) {
        _globalReactions.add(reaction)
        ifEngineAvailable { it.reactionAdded(reaction) }
    }

    override fun removeGlobalReaction(reaction: GlobalReaction<T>) {
        _globalReactions.remove(reaction)
        ifEngineAvailable { it.reactionRemoved(reaction) }
    }

    override fun addNode(node: Node<T>, position: P): Boolean = when {
        nodeShouldBeAdded(node, position) -> {
            val actualPosition = computeActualInsertionPosition(node, position)
            setPosition(node, actualPosition)
            require(_nodes.add(node)) { "Node with id ${node.id} was already existing in this environment." }
            observableNodes.add(node)
            spatialIndex.insert(node, *actualPosition.coordinates)
            updateNeighborhood(node, true)
            ifEngineAvailable { it.nodeAdded(node) }
            nodeAdded(node, position, retrieveNeighborhood(node))
            true
        }
        else -> false
    }

    /**
     * Adds to the simulation a predicate that determines whether a simulation should be terminated.
     *
     * @param terminator the termination predicate.
     */
    override fun addTerminator(terminator: TerminationPredicate<T, P>) {
        this.terminationPredicate = this.terminationPredicate.or(terminator)
    }

    /**
     * Allows subclasses to tune the actual position of a node, applying spatial
     * constrains at node addition.
     *
     * @param node
     * the node
     * @param originalPosition
     * the original (requested) position
     * @return the actual position where the node should be located
     */
    protected abstract fun computeActualInsertionPosition(node: Node<T>, originalPosition: P): P

    override fun forEach(action: Consumer<in Node<T>?>?) {
        nodes.forEach(action)
    }

    private fun foundNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Operation<T>> = newNeighborhood
        .getNeighbors()
        .asSequence()
        .filterNot { it in (oldNeighborhood ?: emptySet()) || retrieveNeighborhood(it).contains(center) }
        .map { Operation(center, it, true) }

    private fun getAllNodesInRange(center: P, range: Double): List<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        val validCache = cache ?: Caffeine.newBuilder()
            .maximumSize(1000)
            .build<Pair<P, Double>, List<Node<T>>> { (pos, r) -> runQuery(pos, r) }
            .also { cache = it }
        return validCache[center to range]
    }

    private fun observeAllNodesInRange(
        centerProvider: () -> P,
        range: Double,
        node: Node<T>? = null,
    ): ObservableSet<Node<T>> {
        val cached = if (node != null) {
            regionNodeCenteredIndex[node.id]?.get(range)
        } else {
            regionPositionCenteredIndex[centerProvider()]?.get(range)
        }

        if (cached != null) {
            return cached.visibleNodes
        }

        val actualCenter = centerProvider()
        val initialNodes = getAllNodesInRange(centerProvider(), range).toObservableSet()

        if (node != null) {
            check(initialNodes.remove(node)) {
                "Either the provided range ($range) is too small for queries to work without precision loss, " +
                    "or the environment is in an inconsistent state. Node $node at ${centerProvider()} was the " +
                    "query center, but within range $range, only nodes $initialNodes were found."
            }
        }

        val region = RegionObserver(
            centerId = node?.id,
            centerProvider = centerProvider,
            radius = range,
            visibleNodes = initialNodes,
        ).apply { regionObservers.add(this) }

        if (node != null) {
            var radiusMap = regionNodeCenteredIndex[node.id]
            if (radiusMap == null) {
                radiusMap = TDoubleObjectHashMap()
                regionNodeCenteredIndex.put(node.id, radiusMap)
            }
            radiusMap.put(range, region)
        } else {
            regionPositionCenteredIndex
                .computeIfAbsent(actualCenter) { TDoubleObjectHashMap() }
                .put(range, region)
        }

        return initialNodes
    }

    override fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double =
        retrievePosition(n1).distanceTo(retrievePosition(n2))

    override fun getLayer(molecule: Molecule): Layer<T, P>? = layers[molecule]

    protected fun retrieveNeighborhood(node: Node<T>): Neighborhood<T> {
        val result = neighCache[node.id]
        requireNotNull(result) {
            check(!nodes.contains(node)) {
                "The environment state is inconsistent. $node is among the nodes, but has no position."
            }
            "$node is not part of the environment."
        }
        return result
    }

    override fun getNeighborhood(node: Node<T>): Observable<Neighborhood<T>> =
        observableNeighCache[node.id].map { maybeNeighborhood ->
            val neighborhood = maybeNeighborhood.getOrNull()
            requireNotNull(neighborhood) {
                check(node !in observableNodes) {
                    "The environment state is inconsistent. $node is among the nodes, but has no position."
                }
                "$node is not part of the environment."
            }
            neighborhood
        }

    override fun getNodeByID(id: Int): Node<T> = nodes.first { n: Node<T> -> n.id == id }

    override fun getNodesWithinRange(node: Node<T>, range: Double): ListSet<Node<T>> {
        val centerPosition = retrievePosition(node)
        val res = LinkedListSet(getAllNodesInRange(centerPosition, range))
        check(res.remove(node)) {
            "Either the provided range ($range) is too small for queries to work without precision loss, " +
                "or the environment is in an inconsistent state. Node $node at $centerPosition was the query center, " +
                "but within range $range, only nodes $res were found."
        }
        return res
    }

    override fun getNodesWithinRange(position: P, range: Double): ListSet<Node<T>> {
        /*
         * Collect every node in range
         */
        return ImmutableListSet.copyOf(getAllNodesInRange(position, range))
    }

    override fun observeNodesWithinRange(node: Node<T>, range: Double): ObservableSet<Node<T>> =
        observeAllNodesInRange({ retrievePosition(node) }, range, node)

    override fun observeNodesWithinRange(position: P, range: Double): ObservableSet<Node<T>> =
        observeAllNodesInRange({ position }, range)

    protected fun retrievePosition(node: Node<T>): P = requireNotNull(nodeToPos[node.id]) {
        check(!nodes.contains(node)) {
            "Node $node is registered in the environment but has no position. " +
                "This could be a bug in Alchemist. Please open an issue at: " +
                "https://github.com/AlchemistSimulator/Alchemist/issues/new/choose"
        }
        "Node $node: ${node.javaClass.simpleName} does not exist in the environment."
    }

    override fun getPosition(node: Node<T>): Observable<P> = observableNodeToPos[node.id].map { maybePosition ->
        val position = maybePosition.getOrNull()
        requireNotNull(position) {
            check(!nodes.contains(node)) {
                "Node $node is registered in the environment but has no position. " +
                    "This could be a bug in Alchemist. Please open an issue at: " +
                    "https://github.com/AlchemistSimulator/Alchemist/issues/new/choose"
            }
            "Node $node: ${node.javaClass.simpleName} does not exist in the environment."
        }
        position
    }

    /**
     * Override this property if units measuring distance do not match with units used
     * for coordinates. For instance, if your space is non-Euclidean, or if you are
     * using polar coordinates. A notable example is using geographical
     * latitude-longitude as y-x coordinates and meters as distance measure.
     */
    override val sizeInDistanceUnits: DoubleArray get() = size

    /**
     * If this environment is attached to a simulation engine, executes consumer.
     *
     * @param action  the [Consumer] to execute
     */
    protected fun ifEngineAvailable(action: Consumer<Simulation<T, P>>) {
        simulationOrNull?.also(action::accept)
    }

    private fun invalidateCache() = cache?.invalidateAll()

    override val isTerminated: Boolean
        get() = terminationPredicate.test(this)

    override fun iterator(): MutableIterator<Node<T>> = nodes.iterator()

    private fun lostNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Operation<T>> = oldNeighborhood
        ?.neighbors
        ?.asSequence()
        ?.filter { neigh -> !newNeighborhood.contains(neigh) && retrieveNeighborhood(neigh).contains(center) }
        ?.map { neigh -> Operation(center, neigh, isAdd = false) }
        .orEmpty()

    /**
     * This method gets called once a node has been added, and its neighborhood has been computed and memorized.
     *
     * @param node the node
     * @param position the position of the node
     * @param neighborhood the current neighborhood of the node
     */
    protected abstract fun nodeAdded(node: Node<T>, position: P, neighborhood: Neighborhood<T>)

    /**
     * This method gets called once a node has been removed.
     *
     * @param node
     * the node
     * @param neighborhood
     * the OLD neighborhood of the node (it is no longer in sync with
     * the [Environment] status)
     */
    protected open fun nodeRemoved(node: Node<T>, neighborhood: Neighborhood<T>) {}

    /**
     * Allows subclasses to determine whether a [Node] should
     * actually get added to this environment.
     *
     * @param node the node
     * @param position the original (requested) position
     * @return true if the node should be added to this environment, false otherwise
     */
    protected open fun nodeShouldBeAdded(node: Node<T>, position: P): Boolean = true

    @Serial
    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()
        val name = inputStream.readObject().toString()
        incarnation =
            SupportedIncarnations
                .get<T, P>(name)
                .orElseThrow { IllegalStateException("Unknown incarnation $name") }
    }

    private fun recursiveOperation(origin: Node<T>): Sequence<Operation<T>> {
        val newNeighborhood = linkingRule.computeNeighborhood(Objects.requireNonNull(origin), this)
        val oldNeighborhood: Neighborhood<T>? = neighCache.put(origin.id, newNeighborhood)
        observableNeighCache.put(origin.id, newNeighborhood)
        return toQueue(origin, oldNeighborhood, newNeighborhood)
    }

    private fun recursiveOperation(origin: Node<T>, destination: Node<T>, isAdd: Boolean): Sequence<Operation<T>> {
        requireNotNull(destination) { "Destination node cannot be null." }
        ifEngineAvailable {
            if (isAdd) {
                it.neighborAdded(origin, destination)
            } else {
                it.neighborRemoved(origin, destination)
            }
        }
        val newNeighborhood = linkingRule.computeNeighborhood(destination, this)
        val oldNeighborhood = neighCache.put(destination.id, newNeighborhood)
        observableNeighCache.put(destination.id, newNeighborhood)
        return toQueue(destination, oldNeighborhood, newNeighborhood)
    }

    override fun removeNode(node: Node<T>) {
        invalidateCache()
        _nodes.remove(requireNotNull(node) { "Node cannot be null." })
        observableNodes.remove(node)
        val pos = requireNotNull(nodeToPos.remove(node.id)) { "Node position cannot be null." }
        observableNodeToPos.remove(node.id)
        spatialIndex.remove(node, *pos.coordinates)
        val neigh = neighCache.remove(node.id)
        observableNeighCache.remove(node.id)
        neigh.forEach {
            with(neighCache.remove(it.id).remove(node)) {
                neighCache.put(it.id, this)
                observableNeighCache.remove(it.id)
                observableNeighCache.put(it.id, this)
            }
        }
        updateRegionObservers(node, null, null)
        ifEngineAvailable { it.nodeRemoved(node, neigh) }
        nodeRemoved(node, neigh)
    }

    private fun runQuery(center: P, range: Double): List<Node<T>> = spatialIndex
        .query(*center.boundingBox(range).map { it.coordinates }.toTypedArray())
        .filter { retrievePosition(it).distanceTo(center) <= range }

    /**
     * Adds or updates a node's position in the position map.
     *
     * @param n the node
     * @param p its new position
     */
    protected fun setPosition(n: Node<T>, p: P) {
        val pos = nodeToPos.put(n.id, p)
        if (p != pos) {
            invalidateCache()
        }
        require(pos == null || spatialIndex.move(n, pos.coordinates, p.coordinates)) {
            "Tried to move a node not previously present in the environment:\nNode: $n\nRequested position: $p"
        }
        observableNodeToPos[n.id] = p
        updateRegionObservers(n, p, pos)
    }

    override fun spliterator(): Spliterator<Node<T>> = nodes.spliterator()

    private fun updateRegionObservers(node: Node<T>, newPosition: P?, oldPosition: P?) {
        if (regionObservers.isNotEmpty()) {
            regionObservers.forEach { region ->
                when {
                    newPosition == null -> { // removal
                        if (node in region.visibleNodes) region.visibleNodes.remove(node)
                        regionNodeCenteredIndex.remove(node.id)?.forEachValue {
                            regionObservers.remove(it)
                            true
                        }
                    }
                    else -> { // new node added or moved
                        val center = region.centerProvider()

                        val wasInside = oldPosition?.distanceTo(center)?.let { it <= region.radius } ?: false
                        val isInside = newPosition.distanceTo(center) <= region.radius

                        when {
                            wasInside && !isInside -> region.visibleNodes.remove(node)
                            !wasInside && isInside -> region.visibleNodes.add(node)
                        }
                    }
                }
            }
        }
    }

    private fun toQueue(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Operation<T>> = lostNeighbors(center, oldNeighborhood, newNeighborhood) +
        foundNeighbors(center, oldNeighborhood, newNeighborhood)

    /**
     * Not used internally. Override as you please.
     */
    override fun toString(): String = javaClass.getSimpleName()

    /**
     * After a node movement, recomputes the neighborhood and notifies the simulation of modifications.
     * This allows movement actions to be defined as LOCAL, though they are normally considered GLOBAL.
     *
     * @param node the moved node
     * @param isNewNode true if the node is new, false otherwise
     */
    protected fun updateNeighborhood(node: Node<T>, isNewNode: Boolean) {
        if (linkingRule.isLocallyConsistent()) {
            val newNeighborhood = linkingRule.computeNeighborhood(node, this)
            val oldNeighborhood: Neighborhood<T>? = neighCache.put(node.id, newNeighborhood)
            observableNeighCache.put(node.id, newNeighborhood)
            oldNeighborhood?.let {
                it
                    .getNeighbors()
                    .asSequence()
                    .filterNot(newNeighborhood::contains)
                    .map(this::retrieveNeighborhood)
                    .filter { neigh -> neigh.contains(node) }
                    .forEach { neighborhoodToChange ->
                        val formerNeighbor = neighborhoodToChange.getCenter()
                        with(neighborhoodToChange.remove(node)) {
                            neighCache.put(formerNeighbor.id, this)
                            observableNeighCache.put(formerNeighbor.id, this)
                        }
                        if (!isNewNode) {
                            ifEngineAvailable { it.neighborRemoved(node, formerNeighbor) }
                        }
                    }
            }
            val newNeighbors = newNeighborhood.neighbors
            val oldNeighbors: Set<Node<T>>? = oldNeighborhood?.neighbors
            (newNeighbors - oldNeighbors.orEmpty()).forEach { newNeighbor ->
                with(neighCache[newNeighbor.id].add(node)) {
                    neighCache.put(newNeighbor.id, this)
                    observableNeighCache.put(newNeighbor.id, this)
                }
                if (!isNewNode) {
                    ifEngineAvailable { it.neighborAdded(node, newNeighbor) }
                }
            }
        } else {
            val processed = TIntHashSet(nodes.size).apply { add(node.id) }
            val operations = recursiveOperation(node).toMutableList()
            while (operations.isNotEmpty()) {
                val next = operations.removeLast()
                if (processed.add(next.destination.id)) {
                    operations.addAll(recursiveOperation(next.origin, next.destination, next.isAdd))
                }
            }
        }
    }

    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
        out.writeObject(incarnation.javaClass.getSimpleName())
    }

    private inner class RegionObserver(
        val centerId: Int? = null,
        val centerProvider: () -> P,
        val radius: Double,
        val visibleNodes: ObservableMutableSet<Node<T>>,
    )

    private data class Operation<T>(val origin: Node<T>, val destination: Node<T>, val isAdd: Boolean) {
        override fun toString(): String = origin.toString() + (if (isAdd) " discovered " else " lost ") + destination
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
