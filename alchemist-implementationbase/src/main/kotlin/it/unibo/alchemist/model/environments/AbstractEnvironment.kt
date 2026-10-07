/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

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
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList
import it.unibo.alchemist.model.observation.ObservableSet
import java.util.Objects
import java.util.function.Consumer
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
    incarnation: Incarnation<T, P>,
    internalIndex: SpatialIndex<Node<T>>,
) : Environment<T, P> {
    private val nodeList = ObservableMutableList<Node<T>>()
    private val environmentReactions = ObservableMutableList<Reaction<T>>()
    final override var layers: Map<Molecule, Layer<T, P>> = LinkedHashMap()
        private set

    /*
     * The authoritative model state is the ordered node list, the position of every node, and the neighborhood of
     * every node. Neighborhoods are stored rather than recomputed on demand: a linking rule that is not locally
     * consistent derives a node's neighborhood from the rest of the topology, so only the stored snapshots are
     * reliable. Positions and neighborhoods are keyed by node id and published through observable maps; each stored
     * neighborhood is an immutable snapshot that topology changes replace.
     */
    private val neighborhoods = ObservableMutableMap<Int, Neighborhood<T>>()

    private val nodesToPositions = ObservableMutableMap<Int, P>()

    /*
     * A secondary index of node coordinates answering range queries. It must mirror nodesToPositions exactly: every
     * addition, movement, and removal updates both.
     */
    private val spatialIndex: SpatialIndex<Node<T>> = internalIndex

    /*
     * The range queries that currently have subscribers. Each one is updated incrementally on every node addition,
     * movement, and removal; unobserved queries are not tracked and compute their members on demand.
     */
    private val activeRangeQueries = LinkedHashSet<RangeQuery>()

    override val reactions: ObservableList<Reaction<T>> = environmentReactions

    override val nodes: ObservableList<Node<T>> = nodeList

    final override val nodeCount: Observable<Int> = nodes.size

    /*
     * Replacing the rule recomputes every stored neighborhood at once from the current positions, so no snapshot built
     * by the previous rule survives.
     */
    final override var linkingRule: LinkingRule<T, P> = NoLinks()
        set(value) {
            field = value
            nodes.current.forEach { node -> neighborhoods.put(node.id, value.computeNeighborhood(node, this)) }
        }

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
        require(reaction.host === this) { "$reaction is hosted by ${reaction.host}, not by $this" }
        if (reaction !in environmentReactions.current) {
            environmentReactions.add(reaction)
            ifAttachedToSimulation { it.reactionAdded(reaction) }
        }
    }

    override fun removeReaction(reaction: Reaction<T>) {
        if (environmentReactions.remove(reaction)) {
            ifAttachedToSimulation { it.reactionRemoved(reaction) }
            reaction.dispose()
        }
    }

    override fun addNode(node: Node<T>, position: P): Boolean = when {
        nodeShouldBeAdded(node, position) -> {
            val actualPosition = computeActualInsertionPosition(node, position)
            require(node !in nodeList.current) {
                "Node with id ${node.id} was already existing in this environment."
            }
            /*
             * The position and spatial index come first, because the linking rule queries them to compute the new
             * neighborhood; reactions are announced only once the node is fully part of the model.
             */
            setPosition(node, actualPosition)
            spatialIndex.insert(node, *actualPosition.coordinates)
            nodeList.add(node)
            refreshNeighborhoodsAround(node)
            ifAttachedToSimulation { simulation -> node.reactions.current.forEach(simulation::reactionAdded) }
            nodeAdded(node, actualPosition, currentNeighborhoodOf(node))
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
        .filterNot { it in (oldNeighborhood ?: emptySet()) || currentNeighborhoodOf(it).contains(center) }

    private fun queryNodesInRange(center: P, range: Double): List<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        return spatialIndex
            .query(*center.boundingBox(range).map { it.coordinates }.toTypedArray())
            .filter { currentPositionOf(it).distanceTo(center) <= range }
            .distinct()
    }

    override fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double =
        currentPositionOf(n1).distanceTo(currentPositionOf(n2))

    override fun getLayer(molecule: Molecule): Layer<T, P>? = layers[molecule]

    /*
     * The layer is looked up whenever the position changes or the value is recomputed,
     * not when the observable is built:
     * conditions may be created during setup before their layer is associated.
     */
    override fun observeLayerValue(molecule: Molecule, node: Node<T>): Observable<T?> =
        getPosition(node).switchMap { position ->
            getLayer(molecule)?.observeValue(position)?.map<T?> { it } ?: observe<T?>(null)
        }

    /**
     * The current neighborhood of [node], which must be part of this environment.
     */
    protected fun currentNeighborhoodOf(node: Node<T>): Neighborhood<T> =
        requireNeighborhood(node, neighborhoods.current[node.id])

    override fun getNeighborhood(node: Node<T>): Observable<Neighborhood<T>> =
        neighborhoods[node.id].map { requireNeighborhood(node, it.getOrNull()) }

    private fun requireNeighborhood(node: Node<T>, neighborhood: Neighborhood<T>?): Neighborhood<T> =
        requireNotNull(neighborhood) {
            check(node !in nodes.current) {
                "The environment state is inconsistent. $node is among the nodes, but has no neighborhood."
            }
            "$node is not part of the environment."
        }

    override fun contains(node: Node<T>): Boolean = node.id in nodesToPositions.current

    override fun getNodeByID(id: Int): Node<T> = nodes.current.first { n: Node<T> -> n.id == id }

    override fun getNodesWithinRange(node: Node<T>, range: Double): List<Node<T>> {
        val centerPosition = currentPositionOf(node)
        val nodesInRange = queryNodesInRange(centerPosition, range)
        // The center is always within any positive range of itself, unless coordinates lost precision.
        check(node in nodesInRange) {
            "Either the provided range ($range) is too small for queries to work without precision loss, " +
                "or the environment is in an inconsistent state. Node $node at $centerPosition was the query center, " +
                "but within range $range, only nodes $nodesInRange were found."
        }
        return nodesInRange.filterNot { it == node }
    }

    override fun getNodesWithinRange(position: P, range: Double): List<Node<T>> = queryNodesInRange(position, range)

    override fun observeNodesWithinRange(node: Node<T>, range: Double): ObservableSet<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        return RangeQuery(node, range)
    }

    override fun observeNodesWithinRange(position: P, range: Double): ObservableSet<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        return RangeQuery(null, range) { position }
    }

    /**
     * The current position of [node], which must be part of this environment.
     */
    protected fun currentPositionOf(node: Node<T>): P = requirePosition(node, nodesToPositions.current[node.id])

    override fun getPosition(node: Node<T>): Observable<P> =
        nodesToPositions[node.id].map { requirePosition(node, it.getOrNull()) }

    private fun requirePosition(node: Node<T>, position: P?): P = requireNotNull(position) {
        check(node !in nodes.current) {
            "Node $node is registered in the environment but has no position. " +
                "This could be a bug in Alchemist. Please open an issue at: " +
                "https://github.com/AlchemistSimulator/Alchemist/issues/new/choose"
        }
        "Node $node: ${node.javaClass.simpleName} does not exist in the environment."
    }

    /**
     * Override this property if units measuring distance do not match with units used
     * for coordinates. For instance, if your space is non-Euclidean, or if you are
     * using polar coordinates. A notable example is using geographical
     * latitude-longitude as y-x coordinates and meters as distance measure.
     */
    override val sizeInDistanceUnits: DoubleArray get() = size

    /**
     * Executes [action] on the simulation this environment is attached to, if any.
     */
    protected fun ifAttachedToSimulation(action: Consumer<Simulation<T, P>>) {
        simulationOrNull?.also(action::accept)
    }

    override val isTerminated: Boolean
        get() = terminationPredicate.test(this)

    private fun lostNeighbors(
        center: Node<T>,
        oldNeighborhood: Neighborhood<T>?,
        newNeighborhood: Neighborhood<T>,
    ): Sequence<Node<T>> = oldNeighborhood
        ?.neighbors
        ?.asSequence()
        ?.filter { neigh -> !newNeighborhood.contains(neigh) && currentNeighborhoodOf(neigh).contains(center) }
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
        val oldNeighborhood = neighborhoods.current[node.id]
        neighborhoods.put(node.id, newNeighborhood)
        return affectedNeighbors(node, oldNeighborhood, newNeighborhood)
    }

    override fun removeNode(node: Node<T>) {
        val reactions = node.reactions.current
        /*
         * Dispose the node, and with it its reactions and their subscriptions, before its position and neighborhood
         * leave the model: a reaction observing its own node would otherwise receive the removal emission and query a
         * node that no longer exists. Engine notifications below use the reactions captured beforehand.
         */
        node.dispose()
        val position = requireNotNull(nodesToPositions.current[node.id]) { "Node position cannot be null." }
        spatialIndex.remove(node, *position.coordinates)
        nodeList.remove(node)
        nodesToPositions.remove(node.id)
        val neigh = requireNotNull(neighborhoods.remove(node.id)) { "Node neighborhood cannot be null." }
        // A locally consistent rule only loses the removed node; any other rule may rewire the remaining topology.
        if (linkingRule.isLocallyConsistent()) {
            neigh.forEach {
                with(currentNeighborhoodOf(it).remove(node)) {
                    neighborhoods.put(it.id, this)
                }
            }
        } else {
            nodes.current.forEach { remainingNode ->
                val updatedNeighborhood = linkingRule.computeNeighborhood(remainingNode, this)
                neighborhoods.put(remainingNode.id, updatedNeighborhood)
            }
        }
        updateRangeQueries(node, null)
        ifAttachedToSimulation { simulation -> reactions.forEach(simulation::reactionRemoved) }
        nodeRemoved(node, neigh)
    }

    /**
     * Adds or updates a node's position in the position map.
     *
     * @param n the node
     * @param p its new position
     */
    protected fun setPosition(n: Node<T>, p: P) {
        val pos = nodesToPositions.current[n.id]
        /*
         * A new node has no previous position and is inserted in the spatial index by addNode after this call.
         * The position map is updated before the range queries, so that queries centered on the moved node see its
         * new position.
         */
        require(pos == null || spatialIndex.move(n, pos.coordinates, p.coordinates)) {
            "Tried to move a node not previously present in the environment:\nNode: $n\nRequested position: $p"
        }
        nodesToPositions[n.id] = p
        updateRangeQueries(n, p)
    }

    /*
     * Iterates over a snapshot: updating a query may deactivate it, which removes it from activeRangeQueries.
     */
    private fun updateRangeQueries(node: Node<T>, newPosition: P?) {
        activeRangeQueries.toList().forEach { it.update(node, newPosition) }
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
     * Recomputes the neighborhoods affected by the addition or movement of [node]: its own, and those of the nodes
     * it joined or left, or every neighborhood the linking rule rewires when the rule is not locally consistent.
     */
    protected fun refreshNeighborhoodsAround(node: Node<T>) {
        /*
         * With a locally consistent rule, a node's neighborhood depends only on its own position: recompute it, then
         * add or remove the node in exactly the neighborhoods it entered or left. Any other rule may change the
         * neighborhoods of nodes that did not move, so recomputation spreads from the moved node through every node
         * whose neighborhood changed, visiting each node at most once.
         */
        if (linkingRule.isLocallyConsistent()) {
            val newNeighborhood = linkingRule.computeNeighborhood(node, this)
            val oldNeighborhood = neighborhoods.current[node.id]
            neighborhoods.put(node.id, newNeighborhood)
            oldNeighborhood?.let {
                it.neighbors.asSequence()
                    .filterNot(newNeighborhood::contains)
                    .map(this::currentNeighborhoodOf)
                    .filter { neigh -> neigh.contains(node) }
                    .forEach { neighborhoodToChange ->
                        val formerNeighbor = neighborhoodToChange.center
                        with(neighborhoodToChange.remove(node)) {
                            neighborhoods.put(formerNeighbor.id, this)
                        }
                    }
            }
            val newNeighbors = newNeighborhood.neighbors
            val oldNeighbors = oldNeighborhood?.neighbors.orEmpty()
            (newNeighbors - oldNeighbors).forEach { newNeighbor ->
                with(currentNeighborhoodOf(newNeighbor).add(node)) {
                    neighborhoods.put(newNeighbor.id, this)
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

    /**
     * The nodes within [radius] of a center, kept up to date while observed.
     * The center is either [centerNode], which is excluded from the members and followed as it moves, or a fixed
     * position; [centerPosition] provides the current center in both cases.
     *
     * The query implements [ObservableSet] directly, rather than delegating, so that every subscription path,
     * including the default members of [Observable], goes through [onChange] and [stopWatching]. The first observer
     * computes the members and activates the query; the last one to leave deactivates it. While unobserved, [current]
     * is computed on demand. Once its center node leaves the environment, the query is empty and never updates again.
     */
    private inner class RangeQuery(
        private val centerNode: Node<T>?,
        private val radius: Double,
        private val centerPosition: () -> P = { currentPositionOf(checkNotNull(centerNode)) },
    ) : ObservableSet<Node<T>> {
        private val members = ObservableMutableSet<Node<T>>()

        private var centerRemoved = false

        private val isActive: Boolean get() = members.observers.isNotEmpty()

        override val current: Set<Node<T>>
            get() = when {
                centerRemoved -> emptySet()
                isActive -> members.current
                else -> computeMembers()
            }

        override val observers: List<Any> get() = members.observers

        override val observingCallbacks: Map<Any, List<(Set<Node<T>>) -> Unit>> get() = members.observingCallbacks

        override val size: Observable<Int> get() = map { it.size }

        override fun containsItem(item: Node<T>): Observable<Boolean> = map { item in it }

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Set<Node<T>>) -> Unit) {
            if (!isActive && !centerRemoved) {
                members.clearAndAddAll(computeMembers())
                activeRangeQueries += this
            }
            members.onChange(registrant, invokeOnRegistration, callback)
        }

        override fun stopWatching(registrant: Any) {
            members.stopWatching(registrant)
            if (!isActive) {
                activeRangeQueries -= this
            }
        }

        override fun dispose() {
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

        private fun computeMembers(): Set<Node<T>> =
            queryNodesInRange(centerPosition(), radius).filterNot { it == centerNode }.toSet()
    }
}
