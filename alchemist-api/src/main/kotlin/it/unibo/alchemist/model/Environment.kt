/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList
import it.unibo.alchemist.model.observation.ObservableSet

/**
 * Interface for an environment.
 * Every environment must implement this specification.
 * [T] is the concentration type, [P] is the [Position] type.
 */
@Suppress("TooManyFunctions")
interface Environment<T, P : Position<out P>> : ReactionHost<T> {
    /**
     * The number of dimensions of this environment.
     */
    val dimensions: Int

    /**
     * Return the [Incarnation] used to initialize the entities of this [Environment], if it has been set.
     */
    val incarnation: Incarnation<T, P>

    /**
     * Whether any of the terminators is satisfied.
     */
    val isTerminated: Boolean

    /**
     * Return all the Layers in this [Environment].
     */
    val layers: Map<Molecule, Layer<T, P>>

    /**
     * Returns an [Observable] view of the number of [Node]s currently in the [Environment].
     */
    val nodeCount: Observable<Int>

    /**
     * An ordered [Observable] view of all the [Node]s that exist in current [Environment].
     */
    val nodes: ObservableList<Node<T>>

    /**
     * This method allows to know which are the smallest coordinates represented.
     * Return an array of length [dimensions] containing the smallest
     * coordinates for each dimension.
     */
    val offset: DoubleArray

    /**
     * Return the current [Simulation], if present, or `null` otherwise.
     */
    val simulationOrNull: Simulation<T, P>?

    /**
     * The size of the environment as an array of length getDimensions().
     * This method must return distance measured with the same unit used by the positions.
     * No non-euclidean distance metrics are allowed.
     */
    val size: DoubleArray

    /**
     * This method returns the size of the environment as an array of length
     * [.getDimensions]. This method must return distance measured with
     * the same unit used for measuring distances. It may or may not return the
     * same result of [.getSize].
     */
    val sizeInDistanceUnits: DoubleArray

    /**
     * The current [NetworkModel], which must be bound to this environment.
     * Assigning a new model publishes the neighborhood it provides for every node in the environment.
     */
    var networkModel: NetworkModel<T>

    /**
     * Return the current [Simulation], if present, or throws an [IllegalStateException] otherwise.
     */
    var simulation: Simulation<T, P>

    /**
     * Associates [layer] with [molecule].
     * Layers are part of the environment setup: association fails once the environment is attached to a simulation,
     * and a molecule cannot be associated with more than one layer.
     */
    fun addLayer(molecule: Molecule, layer: Layer<T, P>)

    /**
     * Adds a new [node] to this environment in a specific [position].
     * The environment is responsible for calling the right method of the simulation to
     * ensure that the reaction is properly scheduled.
     * The function returns true if the node is added to the environment.
     */
    fun addNode(node: Node<T>, position: P): Boolean

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     * The simulation finishes as soon as any of the terminators is satisfied.
     */
    fun addTerminator(terminator: TerminationPredicate<T, P>)

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     * The simulation finishes as soon as any of the terminators is satisfied.
     */
    fun addTerminator(terminator: (Environment<T, P>) -> Boolean) = addTerminator(TerminationPredicate(terminator))

    /**
     * Whether [node] is part of this environment.
     */
    operator fun contains(node: Node<T>): Boolean = nodes.current.any { it === node }

    /**
     * Retrieves [node]'s current position.
     */
    fun currentPositionOf(node: Node<T>): P = positionOf(node).current

    /**
     * Measures the distance between two nodes  ([n1], [n2]) in the environment.
     */
    fun distanceBetween(n1: Node<T>, n2: Node<T>): Double

    /**
     * Get the [Layer] associate to the given [molecule]. If no Layer is associated
     * with the given molecule, return `null`.
     */
    fun layerFor(molecule: Molecule): Layer<T, P>?

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(vararg coordinates: Number): P

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(coordinates: DoubleArray): P

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(coordinates: List<Number>): P = makePosition(*coordinates.toTypedArray())

    /**
     * Moves [node] to [position]. If node movement is unsupported, it does nothing.
     *
     * Every movement goes through this function, including relative movements in a [EuclideanEnvironment]:
     * environments constraining movements (walls, obstacles, physics) override it.
     */
    fun moveNodeTo(node: Node<T>, position: P)

    /**
     * Given a [node], this method returns an observable view of
     * its neighborhood.
     */
    fun neighborhoodOf(node: Node<T>): Observable<Neighborhood<T>>

    /**
     * Given a [node] this method returns a list of all the surrounding
     * nodes within the given [range]. Note that this method (depending on the
     * implementation) might be not optimized, and it's consequently **much**
     * better to use [Environment.neighborhoodOf] and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     */
    fun nodesAround(node: Node<T>, range: Double): List<Node<T>>

    /**
     * Given a [position] this method returns a list of all the
     * surrounding nodes within the given [range].
     * Note that this method
     * (depending on the implementation) might be not optimized.
     */
    fun nodesAround(position: P, range: Double): List<Node<T>>

    /**
     * Allows accessing a [Node] in this [Environment] known its [id].
     * Depending on the implementation, this method may or may not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     */
    fun nodeWithId(id: Int): Node<T>

    /**
     * Observes the value of the [Layer] associated with [molecule] at the position of [node].
     * The observable emits when [node] moves to a position where the value differs, and when the layer changes the
     * value at the current position of [node]. Its value is `null` while no layer is associated with [molecule], and it
     * emits when one is: observables created during setup follow layers associated later.
     */
    fun observeLayerValue(molecule: Molecule, node: Node<T>): Observable<T?>

    /**
     * An [Observable] alternative to [nodesAround].
     */
    fun observeNodesAround(node: Node<T>, range: Double): ObservableSet<Node<T>>

    /**
     * An [Observable] alternative to [nodesAround].
     */
    fun observeNodesAround(position: P, range: Double): ObservableSet<Node<T>>

    /**
     * Observe the position of a [node].
     */
    fun positionOf(node: Node<T>): Observable<P>

    /**
     * Removes [node].
     * If node removal is unsupported, it does nothing.
     */
    fun removeNode(node: Node<T>)

    /**
     * A position with these coordinates, see [makePosition].
     */
    fun DoubleArray.toPosition(): P = makePosition(this)

    /**
     * The current position of this node, see [currentPositionOf].
     */
    val Node<T>.currentPosition: P get() = currentPositionOf(this)

    /**
     * The observable neighborhood of this node, see [neighborhoodOf].
     */
    val Node<T>.neighborhood: Observable<Neighborhood<T>> get() = neighborhoodOf(this)

    /**
     * The observable position of this node, see [positionOf].
     */
    val Node<T>.position: Observable<P> get() = positionOf(this)

    /**
     * The distance between this node and [other], see [distanceBetween].
     */
    fun Node<T>.distanceTo(other: Node<T>): Double = distanceBetween(this, other)

    /**
     * Moves this node to [position], see [moveNodeTo].
     */
    fun Node<T>.moveTo(position: P): Unit = moveNodeTo(this, position)

    /**
     * The nodes within [range] of this node, see [nodesAround].
     */
    fun Node<T>.nodesInRange(range: Double): List<Node<T>> = nodesAround(this, range)

    /**
     * Observes the nodes within [range] of this node, see [observeNodesAround].
     */
    fun Node<T>.observeNodesInRange(range: Double): ObservableSet<Node<T>> = observeNodesAround(this, range)

    /**
     * Removes this node, see [removeNode].
     */
    fun Node<T>.remove(): Unit = removeNode(this)

    /**
     * The nodes within [range] of this position, see [nodesAround].
     */
    fun P.nodesInRange(range: Double): List<Node<T>> = nodesAround(this, range)

    /**
     * Observes the nodes within [range] of this position, see [observeNodesAround].
     */
    fun P.observeNodesInRange(range: Double): ObservableSet<Node<T>> = observeNodesAround(this, range)
}
