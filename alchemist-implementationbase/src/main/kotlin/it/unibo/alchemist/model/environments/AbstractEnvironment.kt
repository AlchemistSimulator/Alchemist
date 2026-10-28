/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.networks.NoLinks
import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.ObservableMutableMap
import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.MutableObservables.observeShareable
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList
import it.unibo.alchemist.model.observation.ObservableSet
import java.util.TreeMap
import java.util.function.Consumer
import kotlin.math.nextUp
import org.danilopianini.util.SpatialIndex

/**
 * Base implementation of an [Environment]:
 * it owns the ordered node list, node positions and neighborhoods, the environment-hosted reactions,
 * the layers associated during setup,
 * and live range queries over node positions.
 * Subclasses define the geometry, through [computeActualInsertionPosition] and node movement, and may react to node
 * addition and removal through [nodeAdded] and [nodeRemoved].
 *
 * @param T concentration type
 * @param P position type
 */
abstract class AbstractEnvironment<T, P : Position<P>> protected constructor(
    final override val incarnation: Incarnation<T, P>,
    internalIndex: SpatialIndex<Node<T>>,
) : Environment<T, P> {
    override val isTerminated: Boolean
        get() = terminationPredicate.test(this)

    final override val nodeCount: Observable<Int> get() = nodeList.size

    override val nodes: ObservableList<Node<T>> get() = nodeList

    override val reactions: ObservableList<Reaction<T>> get() = environmentReactions

    /**
     * Override this property if units measuring distance do not match with units used
     * for coordinates. For instance, if your space is non-Euclidean, or if you are
     * using polar coordinates. A notable example is using geographical
     * latitude-longitude as y-x coordinates and meters as distance measure.
     */
    override val sizeInDistanceUnits: DoubleArray get() = size

    final override val layers: Map<Molecule, Layer<T, P>> get() = layerAssociations.current

    /*
     * Replacing the model stops following the neighborhoods it published,
     * and follows those of the new model for every node,
     * so no neighborhood built by the previous model survives.
     */
    final override var networkModel: NetworkModel<T> = NoLinks()
        set(value) {
            field = value
            neighborhoodSubscriptions.values.forEach(Disposable::dispose)
            neighborhoodSubscriptions.clear()
            nodes.current.forEach(::followNeighborhood)
        }

    final override var simulation: Simulation<T, P>
        get() =
            requireNotNull(simulationOrNull) {
                "This environment is not attached to any simulation."
            }
        set(value) {
            if (simulationOrNull == null) {
                attachedSimulation.current = value
            } else {
                check(simulationOrNull == value) {
                    "Inconsistent simulation configuration for $this: simulation was set to " +
                        "$simulationOrNull (id: ${System.identityHashCode(simulationOrNull)}) " +
                        "and then switched to $value (id: ${System.identityHashCode(value)})"
                }
            }
        }

    final override val attachedSimulation: Observable<Simulation<T, P>?>
        field: MutableObservable<Simulation<T, P>?> = observeShareable(null)

    /*
     * The range queries that currently have subscribers. Each one is updated incrementally on the node additions,
     * movements, and removals that may affect it; unobserved queries are not tracked and compute their members on
     * demand.
     */
    private val activeRangeQueries = ActiveRangeQueries()

    private val environmentReactions = ObservableMutableList<Reaction<T>>()

    // The layers by molecule, observable so that observers of a layer value follow layers associated later.
    private val layerAssociations = ObservableMutableMap<Molecule, Layer<T, P>>()

    /*
     * The authoritative model state is the ordered node list, the position of every node, and the neighborhood of
     * every node. The network model observes the state it depends on and publishes every change of a neighborhood,
     * which is mirrored here. Positions and neighborhoods are keyed by node id and published through observable maps;
     * each stored neighborhood is an immutable snapshot that topology changes replace.
     */
    private val neighborhoods = ObservableMutableMap<Int, Neighborhood<T>>()

    /*
     * The subscriptions to the neighborhoods that the network model publishes for each node, by node id.
     */
    private val neighborhoodSubscriptions = LinkedHashMap<Int, Disposable>()

    private val nodeList = ObservableMutableList<Node<T>>()

    private val nodesToPositions = ObservableMutableMap<Int, P>()

    /*
     * A secondary index of node coordinates answering range queries. It must mirror nodesToPositions exactly: every
     * addition, movement, and removal updates both.
     */
    private val spatialIndex: SpatialIndex<Node<T>> = internalIndex

    private var terminationPredicate: TerminationPredicate<T, P> = TerminationPredicate { false }

    override fun addLayer(molecule: Molecule, layer: Layer<T, P>) {
        check(simulationOrNull == null) {
            "Cannot associate a layer with $molecule: layers must be added before the environment joins a simulation."
        }
        check(molecule !in layers.keys) { "A layer for $molecule was already associated to this environment." }
        layerAssociations[molecule] = layer
    }

    override fun addNode(node: Node<T>, position: P): Boolean = when {
        nodeShouldBeAdded(node, position) -> {
            val actualPosition = computeActualInsertionPosition(node, position)
            require(node !in this) {
                "Node with id ${node.id} was already existing in this environment."
            }
            /*
             * The spatial index and position come first, because the network model queries them to compute the new
             * neighborhood; reactions are announced only once the node is fully part of the model.
             */
            spatialIndex.insert(node, *actualPosition.coordinates)
            nodesToPositions[node.id] = actualPosition
            /*
             * The neighborhood of the node is followed before the node gets listed, so that observers of the node list
             * can read it. Listing and positioning the node then update the neighborhoods of the other nodes.
             */
            followNeighborhood(node)
            nodeList.add(node)
            // Observers of the ranges the node entered find it indexed, positioned, listed, and linked.
            updateRangeQueries(node, null, actualPosition)
            ifAttachedToSimulation { simulation -> node.reactions.current.forEach(simulation::reactionAdded) }
            nodeAdded(node, actualPosition, currentNeighborhoodOf(node))
            true
        }
        else -> false
    }

    override fun addReaction(reaction: Reaction<T>) {
        require(reaction.host === this) { "$reaction is hosted by ${reaction.host}, not by $this" }
        if (reaction !in environmentReactions.current) {
            environmentReactions.add(reaction)
            ifAttachedToSimulation { it.reactionAdded(reaction) }
        }
    }

    /**
     * Adds to the simulation a predicate that determines whether a simulation should be terminated.
     *
     * @param terminator the termination predicate.
     */
    override fun addTerminator(terminator: TerminationPredicate<T, P>) {
        this.terminationPredicate = this.terminationPredicate.or(terminator)
    }

    override fun contains(node: Node<T>): Boolean = node.id in nodesToPositions.current

    // Reads the position map directly, rather than through an observable.
    override fun currentPositionOf(node: Node<T>): P = requirePosition(node, nodesToPositions.current[node.id])

    override fun distanceBetween(n1: Node<T>, n2: Node<T>): Double = n1.currentPosition.distanceTo(n2.currentPosition)

    override fun layerFor(molecule: Molecule): Layer<T, P>? = layers[molecule]

    override fun neighborhoodOf(node: Node<T>): Observable<Neighborhood<T>> =
        neighborhoods[node.id].map { requireNeighborhood(node, it.getOrNull()) }

    override fun nodesAround(node: Node<T>, range: Double): List<Node<T>> {
        val centerPosition = node.currentPosition
        val nodesInRange = queryNodesInRange(centerPosition, range)
        // The center is always within any positive range of itself, unless coordinates lost precision.
        check(node in nodesInRange) {
            "Either the provided range ($range) is too small for queries to work without precision loss, " +
                "or the environment is in an inconsistent state. Node $node at $centerPosition was the query center, " +
                "but within range $range, only nodes $nodesInRange were found."
        }
        return nodesInRange.filterNot { it == node }
    }

    override fun nodesAround(position: P, range: Double): List<Node<T>> = queryNodesInRange(position, range)

    override fun nodeWithId(id: Int): Node<T> = requireNotNull(neighborhoods.current[id]) {
        "Node with id $id does not exist in the environment. Available: ${nodeList.current.map { it.id }.sorted()}"
    }.center

    /*
     * The association of the layer is observed too:
     * conditions may be created during setup before their layer is associated.
     */
    override fun observeLayerValue(molecule: Molecule, node: Node<T>): Observable<T?> =
        layerAssociations[molecule].switchMap { association ->
            association.fold(
                ifEmpty = { observe(null) },
                ifSome = { layer -> node.position.switchMap { position -> layer.valueAt(position).map { it } } },
            )
        }

    override fun observeNodesAround(node: Node<T>, range: Double): ObservableSet<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        return RangeQuery(node, range)
    }

    override fun observeNodesAround(position: P, range: Double): ObservableSet<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        return RangeQuery(null, range) { position }
    }

    override fun positionOf(node: Node<T>): Observable<P> =
        nodesToPositions[node.id].map { requirePosition(node, it.getOrNull()) }

    override fun removeNode(node: Node<T>) {
        val reactions = node.reactions.current
        /*
         * Dispose the node, and with it its reactions and their subscriptions, before its position and neighborhood
         * leave the model: a reaction observing its own node would otherwise receive the removal emission and query a
         * node that no longer exists. Engine notifications below use the reactions captured beforehand.
         */
        node.dispose()
        val position = requireNotNull(nodesToPositions.current[node.id]) { "Node position cannot be null." }
        // The neighborhood of the node stops being followed before it leaves, so that it is never published again.
        checkNotNull(neighborhoodSubscriptions.remove(node.id)) {
            "Node neighborhood cannot be unobserved."
        }.dispose()
        val neigh = checkNotNull(neighborhoods.remove(node.id)) { "Node neighborhood cannot be null." }
        spatialIndex.remove(node, *position.coordinates)
        // Unlisting and unpositioning the node update the neighborhoods of the other nodes.
        nodeList.remove(node)
        updateRangeQueries(node, position, null)
        /*
         * The position goes last. Observers reach a node's position through the node list, a neighborhood, or a range
         * query: once the node has left all of them, they no longer observe its position, which can then go away
         * without notifying anyone about a node that no longer exists.
         */
        nodesToPositions.remove(node.id)
        ifAttachedToSimulation { simulation -> reactions.forEach(simulation::reactionRemoved) }
        nodeRemoved(node, neigh)
    }

    override fun removeReaction(reaction: Reaction<T>) {
        if (environmentReactions.remove(reaction)) {
            ifAttachedToSimulation { it.reactionRemoved(reaction) }
            reaction.dispose()
        }
    }

    /**
     * Not used internally. Override as you please.
     */
    override fun toString(): String = this::class.simpleName ?: this::class.toString()

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

    /**
     * The current neighborhood of [node], which must be part of this environment.
     */
    protected fun currentNeighborhoodOf(node: Node<T>): Neighborhood<T> =
        requireNeighborhood(node, neighborhoods.current[node.id])

    /**
     * Executes [action] on the simulation this environment is attached to, if any.
     */
    protected fun ifAttachedToSimulation(action: Consumer<Simulation<T, P>>) {
        simulationOrNull?.also(action::accept)
    }

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

    /**
     * Moves [n], which must be part of this environment, to [p]; moving it to its current position does nothing.
     *
     * @param n the node
     * @param p its new position
     */
    protected fun setPosition(n: Node<T>, p: P) {
        val pos = n.currentPosition
        /*
         * The position map is updated before the range queries, so that queries centered on the moved node see its
         * new position.
         */
        if (pos != p) {
            check(spatialIndex.move(n, pos.coordinates, p.coordinates)) {
                "The spatial index lost track of $n, which could not be moved to $p"
            }
            nodesToPositions[n.id] = p
            updateRangeQueries(n, pos, p)
        }
    }

    private fun followNeighborhood(node: Node<T>) {
        neighborhoodSubscriptions[node.id] = networkModel.neighborhoodOf(node).onChange { neighborhood ->
            check(neighborhood.center == node) {
                "The network model $networkModel published the neighborhood of ${neighborhood.center} for $node"
            }
            neighborhoods.put(node.id, neighborhood)
        }
    }

    private fun queryNodesInRange(center: P, range: Double): List<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        // The spatial index excludes the borders of the box: it is enlarged so that nodes exactly at range are found.
        return spatialIndex
            .query(*center.boundingBox(range.nextUp()).map { it.coordinates }.toTypedArray())
            .filter { it.currentPosition.distanceTo(center) <= range }
            .distinct()
    }

    private fun requireNeighborhood(node: Node<T>, neighborhood: Neighborhood<T>?): Neighborhood<T> =
        requireNotNull(neighborhood) {
            check(node !in nodes.current) {
                "The environment state is inconsistent. $node is among the nodes, but has no neighborhood."
            }
            "$node is not part of the environment."
        }

    private fun requirePosition(node: Node<T>, position: P?): P = requireNotNull(position) {
        check(node !in nodes.current) {
            "Node $node is registered in the environment but has no position. " +
                "This could be a bug in Alchemist. Please open an issue at: " +
                "https://github.com/AlchemistSimulator/Alchemist/issues/new/choose"
        }
        "Node $node: ${node.javaClass.simpleName} does not exist in the environment."
    }

    /*
     * Iterates over a snapshot: updating a query may deactivate it, which removes it from activeRangeQueries.
     */
    private fun updateRangeQueries(node: Node<T>, oldPosition: P?, newPosition: P?) {
        activeRangeQueries.affectedBy(node, oldPosition, newPosition).forEach { it.update(node, newPosition) }
    }

    /**
     * The active range queries, indexed so that a node addition, movement, or removal reaches only the queries it may
     * affect: those centered on the node, those centered on a fixed position, and those whose center is within their
     * radius of the old or the new position of the node, found through the spatial index.
     */
    private inner class ActiveRangeQueries {
        private val byCenter = LinkedHashMap<Int, MutableSet<RangeQuery>>()

        private val fixed = LinkedHashSet<RangeQuery>()

        // How many node-centered queries use each radius, so that the largest one is always known.
        private val radii = TreeMap<Double, Int>()

        /**
         * The queries that a change of [node] from [oldPosition] to [newPosition] may affect, as a snapshot.
         * A null [oldPosition] stands for an addition, a null [newPosition] for a removal.
         */
        fun affectedBy(node: Node<T>, oldPosition: P?, newPosition: P?): List<RangeQuery> {
            val affected = LinkedHashSet<RangeQuery>()
            byCenter[node.id]?.let(affected::addAll)
            affected += fixed
            radii.lastEntry()?.key?.let { largestRadius ->
                listOfNotNull(oldPosition, newPosition).forEach { position ->
                    queryNodesInRange(position, largestRadius).forEach { center ->
                        byCenter[center.id]?.let(affected::addAll)
                    }
                }
            }
            return affected.toList()
        }

        operator fun minusAssign(query: RangeQuery) {
            val center = query.centerNode
            if (center == null) {
                fixed -= query
            } else {
                byCenter[center.id]?.let { queries ->
                    if (queries.remove(query)) {
                        if (queries.isEmpty()) {
                            byCenter -= center.id
                        }
                        radii.computeIfPresent(query.radius) { _, count -> (count - 1).takeIf { it > 0 } }
                    }
                }
            }
        }

        operator fun plusAssign(query: RangeQuery) {
            val center = query.centerNode
            if (center == null) {
                fixed += query
            } else if (byCenter.getOrPut(center.id) { LinkedHashSet() }.add(query)) {
                radii.merge(query.radius, 1, Int::plus)
            }
        }
    }

    /**
     * The nodes within [radius] of a center, kept up to date while observed.
     * The center is either [centerNode], which is excluded from the members and followed as it moves, or a fixed
     * position; [centerPosition] provides the current center in both cases.
     *
     * While observed, the query is active: the environment updates its members as nodes move, and it publishes them.
     * While unobserved, [current] is computed on demand. Once its center node leaves the environment, the query is
     * empty and never updates again.
     */
    private inner class RangeQuery(
        val centerNode: Node<T>?,
        val radius: Double,
        private val centerPosition: () -> P = { checkNotNull(centerNode).currentPosition },
    ) : AbstractObservable<Set<Node<T>>>(emitOnDistinct = false),
        ObservableSet<Node<T>> {
        override val current: Set<Node<T>>
            get() = when {
                centerRemoved -> emptySet()
                isObserved -> members.current
                else -> computeMembers()
            }

        override val size: Observable<Int> = map { it.size }

        private val members = ObservableMutableSet<Node<T>>()

        /*
         * Active queries are told when their center leaves; inactive ones are not, so they check the environment.
         */
        private var centerRemoved = false
            get() = field || centerNode != null && centerNode !in this@AbstractEnvironment

        override fun computeFresh(): Set<Node<T>> = if (centerRemoved) emptySet() else computeMembers()

        override fun containsItem(item: Node<T>): Observable<Boolean> = map { item in it }

        override fun dispose() {
            super.dispose()
            members.dispose()
            activeRangeQueries -= this
        }

        /**
         * Reacts to [node] being added or moved to [newPosition], or removed when [newPosition] is null.
         */
        fun update(node: Node<T>, newPosition: P?) {
            when {
                node == centerNode && newPosition == null -> {
                    centerRemoved = true
                    activeRangeQueries -= this
                    members.clearAndAddAll(emptySet())
                }
                node == centerNode -> members.clearAndAddAll(computeMembers())
                newPosition != null && newPosition.distanceTo(centerPosition()) <= radius -> members.add(node)
                else -> members.remove(node)
            }
        }

        override fun startObservingSources() {
            if (!centerRemoved) {
                members.clearAndAddAll(computeMembers())
                activeRangeQueries += this
            }
            observeSource(members) { updateAndNotify(it) }
        }

        override fun stopObservingSources() {
            activeRangeQueries -= this
            // Unobserved members go stale: later observers recompute them, or get none if the center left.
            members.clearAndAddAll(emptySet())
        }

        private fun computeMembers(): Set<Node<T>> =
            queryNodesInRange(centerPosition(), radius).filterNot { it == centerNode }.toSet()
    }
}
