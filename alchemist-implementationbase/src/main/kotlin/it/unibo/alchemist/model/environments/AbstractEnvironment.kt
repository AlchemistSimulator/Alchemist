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
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.ObservableMutableMap
import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList
import it.unibo.alchemist.model.observation.ObservableSet
import java.util.Objects
import java.util.function.Consumer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
    private val mutableNodes = ObservableMutableList<Node<T>>()
    private val _reactions = LinkedHashSet<Reaction<T>>()
    final override var layers: Map<Molecule, Layer<T, P>> = LinkedHashMap()
        private set

    private val observableNeighCache = ObservableMutableMap<Int, Neighborhood<T>>()

    private val observableNodeToPos = ObservableMutableMap<Int, P>()

    private val spatialIndex: SpatialIndex<Node<T>> = internalIndex

    override val reactions: ImmutableList<Reaction<T>>
        get() = _reactions.toImmutableList()

    override val nodes: ObservableList<Node<T>> = mutableNodes

    final override val nodeCount: Observable<Int> = nodes.size

    private val regionObservers = ArrayList<RegionObserver>()

    private val regionNodeCenteredIndex = TIntObjectHashMap<TDoubleObjectHashMap<RegionObserver>>()

    private val regionPositionCenteredIndex = HashMap<P, TDoubleObjectHashMap<RegionObserver>>()

    final override var linkingRule: LinkingRule<T, P> = NoLinks()
        set(value) {
            field = value
            nodes.current.forEach { node -> observableNeighCache.put(node.id, value.computeNeighborhood(node, this)) }
        }

    private var cache: LoadingCache<Pair<P, Double>, List<Node<T>>>? = null

    final override val incarnation: Incarnation<T, P> = requireNotNull(incarnation)

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

    override fun addLayer(molecule: Molecule, layer: Layer<T, P>) {
        check(simulationOrNull == null) {
            "Cannot associate a layer with $molecule: layers must be added before the environment joins a simulation."
        }
        check(molecule !in layers.keys) { "A layer for $molecule was already associated to this environment." }
        layers += molecule to layer
    }

    override fun addReaction(reaction: Reaction<T>) {
        if (_reactions.add(reaction)) {
            ifEngineAvailable { it.reactionAdded(reaction) }
        }
    }

    override fun removeReaction(reaction: Reaction<T>) {
        if (_reactions.remove(reaction)) {
            ifEngineAvailable { it.reactionRemoved(reaction) }
            reaction.dispose()
        }
    }

    override fun addNode(node: Node<T>, position: P): Boolean = when {
        nodeShouldBeAdded(node, position) -> {
            val actualPosition = computeActualInsertionPosition(node, position)
            require(node !in mutableNodes.current) {
                "Node with id ${node.id} was already existing in this environment."
            }
            setPosition(node, actualPosition)
            spatialIndex.insert(node, *actualPosition.coordinates)
            mutableNodes.add(node)
            updateNeighborhood(node)
            ifEngineAvailable { simulation -> node.reactions.forEach(simulation::reactionAdded) }
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

    private fun foundNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Node<T>> = newNeighborhood
        .neighbors
        .asSequence()
        .filterNot { it in (oldNeighborhood ?: emptySet()) || retrieveNeighborhood(it).contains(center) }

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
        )

        val addRegion = {
            runCatching {
                val currentCenter = centerProvider()
                val currentNodes = getAllNodesInRange(currentCenter, range)
                initialNodes.clearAndAddAll(currentNodes.toSet())

                regionObservers.add(region)
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
            }.onFailure { initialNodes.clearAndAddAll(emptySet()) }
        }

        val removeRegion = {
            regionObservers.remove(region)
            if (node != null) {
                regionNodeCenteredIndex[node.id]?.remove(range)
                if (regionNodeCenteredIndex[node.id]?.isEmpty == true) {
                    regionNodeCenteredIndex.remove(node.id)
                }
            } else {
                regionPositionCenteredIndex[actualCenter]?.remove(range)
                if (regionPositionCenteredIndex[actualCenter]?.isEmpty == true) {
                    regionPositionCenteredIndex.remove(actualCenter)
                }
            }
        }

        return RefCountObservableSet(
            delegate = initialNodes,
            onActive = { addRegion() },
            onInactive = { removeRegion() },
        )
    }

    override fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double =
        retrievePosition(n1).distanceTo(retrievePosition(n2))

    override fun getLayer(molecule: Molecule): Layer<T, P>? = layers[molecule]

    override fun observeLayerValue(molecule: Molecule, node: Node<T>): Observable<T?> =
        getPosition(node).switchMap { position ->
            getLayer(molecule)?.observeValue(position)?.map<T?> { it } ?: observe<T?>(null)
        }

    protected fun retrieveNeighborhood(node: Node<T>): Neighborhood<T> {
        val result = observableNeighCache.current[node.id]
        requireNotNull(result) {
            check(node !in nodes.current) {
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
                check(node !in nodes.current) {
                    "The environment state is inconsistent. $node is among the nodes, but has no position."
                }
                "$node is not part of the environment."
            }
            neighborhood
        }

    override fun getNodeByID(id: Int): Node<T> = nodes.current.first { n: Node<T> -> n.id == id }

    override fun getNodesWithinRange(node: Node<T>, range: Double): List<Node<T>> {
        val centerPosition = retrievePosition(node)
        val nodesInRange = getAllNodesInRange(centerPosition, range).distinct()
        check(node in nodesInRange) {
            "Either the provided range ($range) is too small for queries to work without precision loss, " +
                "or the environment is in an inconsistent state. Node $node at $centerPosition was the query center, " +
                "but within range $range, only nodes $nodesInRange were found."
        }
        return nodesInRange.filterNot { it == node }
    }

    override fun getNodesWithinRange(position: P, range: Double): List<Node<T>> {
        /*
         * Collect every node in range
         */
        return getAllNodesInRange(position, range).distinct()
    }

    override fun observeNodesWithinRange(node: Node<T>, range: Double): ObservableSet<Node<T>> =
        observeAllNodesInRange({ retrievePosition(node) }, range, node)

    override fun observeNodesWithinRange(position: P, range: Double): ObservableSet<Node<T>> =
        observeAllNodesInRange({ position }, range)

    protected fun retrievePosition(node: Node<T>): P = requireNotNull(observableNodeToPos.current[node.id]) {
        check(node !in nodes.current) {
            "Node $node is registered in the environment but has no position. " +
                "This could be a bug in Alchemist. Please open an issue at: " +
                "https://github.com/AlchemistSimulator/Alchemist/issues/new/choose"
        }
        "Node $node: ${node.javaClass.simpleName} does not exist in the environment."
    }

    override fun getPosition(node: Node<T>): Observable<P> = observableNodeToPos[node.id].map { maybePosition ->
        val position = maybePosition.getOrNull()
        requireNotNull(position) {
            check(node !in nodes.current) {
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

    private fun lostNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Node<T>> = oldNeighborhood
        ?.neighbors
        ?.asSequence()
        ?.filter { neigh -> !newNeighborhood.contains(neigh) && retrieveNeighborhood(neigh).contains(center) }
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

    private fun recomputeNeighborhood(node: Node<T>): Sequence<Node<T>> {
        val newNeighborhood = linkingRule.computeNeighborhood(Objects.requireNonNull(node), this)
        val oldNeighborhood = observableNeighCache.current[node.id]
        observableNeighCache.put(node.id, newNeighborhood)
        return affectedNeighbors(node, oldNeighborhood, newNeighborhood)
    }

    override fun removeNode(node: Node<T>) {
        val reactions = node.reactions.toList()
        // Detach the node's reactions before its position and neighborhood leave the observable model state.
        node.dispose()
        invalidateCache()
        val position = requireNotNull(observableNodeToPos.current[node.id]) { "Node position cannot be null." }
        spatialIndex.remove(node, *position.coordinates)
        mutableNodes.remove(node)
        observableNodeToPos.remove(node.id)
        val neigh = requireNotNull(observableNeighCache.remove(node.id)) { "Node neighborhood cannot be null." }
        if (linkingRule.isLocallyConsistent()) {
            neigh.forEach {
                with(retrieveNeighborhood(it).remove(node)) {
                    observableNeighCache.put(it.id, this)
                }
            }
        } else {
            nodes.current.forEach { remainingNode ->
                val updatedNeighborhood = linkingRule.computeNeighborhood(remainingNode, this)
                observableNeighCache.put(remainingNode.id, updatedNeighborhood)
            }
        }
        updateRegionObservers(node, null, null)
        ifEngineAvailable { simulation -> reactions.forEach(simulation::reactionRemoved) }
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
        val pos = observableNodeToPos.current[n.id]
        if (p != pos) {
            invalidateCache()
        }
        require(pos == null || spatialIndex.move(n, pos.coordinates, p.coordinates)) {
            "Tried to move a node not previously present in the environment:\nNode: $n\nRequested position: $p"
        }
        observableNodeToPos[n.id] = p
        updateRegionObservers(n, p, pos)
    }

    private fun updateRegionObservers(node: Node<T>, newPosition: P?, oldPosition: P?) {
        if (regionObservers.isNotEmpty()) {
            regionObservers.forEach { region ->
                when {
                    newPosition == null -> { // removal
                        if (node in region.visibleNodes.current) region.visibleNodes.remove(node)
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

    private fun affectedNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Node<T>> = lostNeighbors(center, oldNeighborhood, newNeighborhood) +
        foundNeighbors(center, oldNeighborhood, newNeighborhood)

    /**
     * Not used internally. Override as you please.
     */
    override fun toString(): String = javaClass.getSimpleName()

    /**
     * Recomputes the neighborhood after a node is added or moved.
     *
     * @param node the moved node
     */
    protected fun updateNeighborhood(node: Node<T>) {
        if (linkingRule.isLocallyConsistent()) {
            val newNeighborhood = linkingRule.computeNeighborhood(node, this)
            val oldNeighborhood = observableNeighCache.current[node.id]
            observableNeighCache.put(node.id, newNeighborhood)
            oldNeighborhood?.let {
                it.neighbors.asSequence()
                    .filterNot(newNeighborhood::contains)
                    .map(this::retrieveNeighborhood)
                    .filter { neigh -> neigh.contains(node) }
                    .forEach { neighborhoodToChange ->
                        val formerNeighbor = neighborhoodToChange.center
                        with(neighborhoodToChange.remove(node)) {
                            observableNeighCache.put(formerNeighbor.id, this)
                        }
                    }
            }
            val newNeighbors = newNeighborhood.neighbors
            val oldNeighbors = oldNeighborhood?.neighbors.orEmpty()
            (newNeighbors - oldNeighbors).forEach { newNeighbor ->
                with(retrieveNeighborhood(newNeighbor).add(node)) {
                    observableNeighCache.put(newNeighbor.id, this)
                }
            }
        } else {
            val processed = TIntHashSet(nodes.current.size).apply { add(node.id) }
            val nodesToUpdate = recomputeNeighborhood(node).toMutableList()
            while (nodesToUpdate.isNotEmpty()) {
                val next = nodesToUpdate.removeLast()
                if (processed.add(next.id)) {
                    nodesToUpdate.addAll(recomputeNeighborhood(next))
                }
            }
        }
    }

    private inner class RegionObserver(
        val centerId: Int? = null,
        val centerProvider: () -> P,
        val radius: Double,
        val visibleNodes: ObservableMutableSet<Node<T>>,
    )

    /**
     * Simple wrapper for [ObservableSet] that manages reference counting to track observers and
     * invoke specified callbacks when observers are registered or deregistered.
     * It serves as both a way to avoid leaks through the [onInactive] callback (which should clear
     * the backing caches), and a lazy evaluation of [onActive] when observers are registered.
     *
     * @param onActive the callback to be invoked when the first observer is added
     * @param onInactive the callback to be invoked when the last observer is removed,
     * which should clear backing caches and resources.
     */
    private class RefCountObservableSet<T>(
        private val delegate: ObservableMutableSet<T>,
        private val onActive: () -> Unit,
        private val onInactive: () -> Unit,
    ) : ObservableSet<T> by delegate {

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Set<T>) -> Unit) {
            if (delegate.observers.isEmpty()) {
                onActive()
            }
            delegate.onChange(registrant, invokeOnRegistration, callback)
        }

        override fun stopWatching(registrant: Any) {
            delegate.stopWatching(registrant)
            if (delegate.observers.isEmpty()) {
                onInactive()
            }
        }

        override fun dispose() {
            delegate.dispose()
            onInactive()
        }
    }
}
