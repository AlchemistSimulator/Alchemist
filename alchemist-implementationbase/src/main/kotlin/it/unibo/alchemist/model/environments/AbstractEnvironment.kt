/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
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
import org.danilopianini.util.ArrayListSet
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.LinkedListSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets
import org.danilopianini.util.SpatialIndex
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serial
import java.util.Objects
import java.util.Spliterator
import java.util.function.Consumer

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
    private val _nodes: ListSet<Node<T>> = ArrayListSet<Node<T>>()
    private val _globalReactions = ArrayListSet<GlobalReaction<T>>()
    private val _layers: MutableMap<Molecule, Layer<T, P>> = LinkedHashMap<Molecule, Layer<T, P>>()
    private val neighCache = TIntObjectHashMap<Neighborhood<T>>()
    private val nodeToPos = TIntObjectHashMap<P>()
    private val spatialIndex: SpatialIndex<Node<T>> = requireNotNull(internalIndex)

    override val layers: ListSet<Layer<T, P>> get() = ArrayListSet(_layers.values)

    override val globalReactions: ListSet<GlobalReaction<T>>
        get() = ListSets.unmodifiableListSet(_globalReactions)

    override val nodes: ListSet<Node<T>> = ListSets.unmodifiableListSet(_nodes)

    final override val nodeCount: Int get() = nodes.size

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

    /**
     * @param incarnation the incarnation to be used.
     * @param internalIndex
     * the [SpatialIndex] to use in order to efficiently
     * retrieve nodes.
     */
    init {
        this.incarnation = requireNotNull(incarnation)
    }

    override fun addLayer(molecule: Molecule, layer: Layer<T, P>) {
        check(_layers.put(molecule, layer) == null) { "Two layers have been associated to $molecule" }
    }

    override fun addGlobalReaction(reaction: GlobalReaction<T>) {
        _globalReactions.add(reaction)
        ifEngineAvailable { it.reactionAdded(reaction) }
    }

    override fun removeGlobalReaction(reaction: GlobalReaction<T>) {
        _globalReactions.remove(reaction)
        ifEngineAvailable { it.reactionRemoved(reaction) }
    }

    override fun addNode(node: Node<T>, p: P): Boolean = when {
        nodeShouldBeAdded(node, p) -> {
            val actualPosition = computeActualInsertionPosition(node, p)
            setPosition(node, actualPosition)
            require(_nodes.add(node)) { "Node with id ${node.id} was already existing in this environment." }
            spatialIndex.insert(node, *actualPosition.coordinates)
            updateNeighborhood(node, true)
            ifEngineAvailable { it.nodeAdded(node) }
            nodeAdded(node, p, getNeighborhood(node))
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
     * @param p
     * the original (requested) position
     * @return the actual position where the node should be located
     */
    protected abstract fun computeActualInsertionPosition(node: Node<T>, p: P): P

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
        .filterNot { it in (oldNeighborhood ?: emptySet()) || getNeighborhood(it).contains(center) }
        .map { Operation(center, it, true) }

    private fun getAllNodesInRange(center: P, range: Double): List<Node<T>> {
        require(range > 0) { "Range query must be positive (provided: $range)" }
        val validCache =
            cache ?: Caffeine
                .newBuilder()
                .maximumSize(1000)
                .build<Pair<P, Double>, List<Node<T>>> { (pos, r) -> runQuery(pos, r) }
                .also { cache = it }
        return validCache[center to range]
    }

    override fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double = getPosition(n1).distanceTo(getPosition(n2))

    override fun getLayer(molecule: Molecule): Layer<T, P>? = _layers[molecule]

    override fun getNeighborhood(node: Node<T>): Neighborhood<T> {
        val result = neighCache[node.id]
        requireNotNull(result) {
            check(!nodes.contains(node)) {
                "The environment state is inconsistent. $node is among the nodes, but has no position."
            }
            "$node is not part of the environment."
        }
        return result
    }

    override fun getNodeByID(id: Int): Node<T> = nodes.first { n: Node<T> -> n.id == id }

    override fun getNodesWithinRange(node: Node<T>, range: Double): ListSet<Node<T>> {
        val centerPosition = getPosition(node)
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

    override fun getPosition(node: Node<T>): P = requireNotNull(nodeToPos[node.id]) {
        check(!nodes.contains(node)) {
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
        ?.filter { neigh -> !newNeighborhood.contains(neigh) && getNeighborhood(neigh).contains(center) }
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
     * @param p the original (requested) position
     * @return true if the node should be added to this environment, false otherwise
     */
    protected open fun nodeShouldBeAdded(node: Node<T>, p: P): Boolean = true

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
        val newNeighborhood = linkingRule.computeNeighborhood(Objects.requireNonNull<Node<T>?>(origin), this)
        val oldNeighborhood: Neighborhood<T>? = neighCache.put(origin.id, newNeighborhood)
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
        return toQueue(destination, oldNeighborhood, newNeighborhood)
    }

    override fun removeNode(node: Node<T>) {
        invalidateCache()
        _nodes.remove(requireNotNull(node) { "Node cannot be null." })
        val pos = requireNotNull(nodeToPos.remove(node.id)) { "Node position cannot be null." }
        spatialIndex.remove(node, *pos.coordinates)
        val neigh = neighCache.remove(node.id)
        neigh.forEach { neighCache.put(it.id, neighCache.remove(it.id).remove(node)) }
        ifEngineAvailable { it.nodeRemoved(node, neigh) }
        nodeRemoved(node, neigh)
    }

    private fun runQuery(center: P, range: Double): List<Node<T>> = spatialIndex
        .query(*center.boundingBox(range).map { it.coordinates }.toTypedArray())
        .filter { getPosition(it).distanceTo(center) <= range }

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
    }

    override fun spliterator(): Spliterator<Node<T>> = nodes.spliterator()

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
            oldNeighborhood?.let {
                it
                    .getNeighbors()
                    .asSequence()
                    .filterNot(newNeighborhood::contains)
                    .map(this::getNeighborhood)
                    .filter { neigh -> neigh.contains(node) }
                    .forEach { neighborhoodToChange ->
                        val formerNeighbor = neighborhoodToChange.getCenter()
                        neighCache.put(formerNeighbor.id, neighborhoodToChange.remove(node))
                        if (!isNewNode) {
                            ifEngineAvailable { it.neighborRemoved(node, formerNeighbor) }
                        }
                    }
            }
            val newNeighbors = newNeighborhood.neighbors
            val oldNeighbors: Set<Node<T>>? = oldNeighborhood?.neighbors
            (newNeighbors - oldNeighbors.orEmpty()).forEach { newNeighbor ->
                neighCache.put(newNeighbor.id, neighCache[newNeighbor.id].add(node))
                if (!isNewNode) {
                    ifEngineAvailable { it.neighborAdded(node, newNeighbor) }
                }
            }
        } else {
            val processed = TIntHashSet(nodeCount).apply { add(node.id) }
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

    private data class Operation<T>(val origin: Node<T>, val destination: Node<T>, val isAdd: Boolean) {
        override fun toString(): String = origin.toString() + (if (isAdd) " discovered " else " lost ") + destination
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
