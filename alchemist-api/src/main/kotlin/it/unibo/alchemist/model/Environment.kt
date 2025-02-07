/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.core.Simulation
import org.danilopianini.util.ListSet
import java.io.Serializable

/**
 * Interface for an environment.
 * Every environment must implement this specification.
 * [T] is the [Concentration] type, [P] is the [Position] type.
 */
interface Environment<T, P : Position<out P>> :
    Serializable,
    Iterable<Node<T>> {
    /**
     * Add a [Layer] to the [Environment].
     */
    fun addLayer(
        molecule: Molecule,
        layer: Layer<T, P>,
    )

    /**
     * Add a [GlobalReaction] to the [Environment].
     */
    fun addGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Remove a [GlobalReaction] from the [Environment].
     */
    fun removeGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Get the [Environment]'s [GlobalReaction]s.
     */
    val globalReactions: ListSet<GlobalReaction<T>>

    /**
     * Adds a new [node] to this environment in a specific [position].
     * The environment is responsible for calling the right method of the simulation to
     * ensure that the reaction is properly scheduled.
     * The function returns true if the node is added to the environment.
     */
    fun addNode(
        node: Node<T>,
        position: P,
    ): Boolean

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     */
    fun addTerminator(terminator: TerminationPredicate<T, P>)

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     */
    fun addTerminator(terminator: (Environment<T, P>) -> Boolean) = addTerminator(TerminationPredicate(terminator))

    /**
     * The number of dimensions of this environment.
     */
    val dimensions: Int

    /**
     * Measures the distance between two nodes  ([n1], [n2]) in the environment.
     */
    fun getDistanceBetweenNodes(
        n1: Node<T>,
        n2: Node<T>,
    ): Double

    /**
     * Return the [Incarnation] used to initialize the entities of this [Environment], if it has been set.
     */
    val incarnation: Incarnation<T, P>

    /**
     * Get the [Layer] associate to the given [molecule]. If no Layer is associated
     * with the given molecule, return `null`.
     */
    fun getLayer(molecule: Molecule): Layer<T, P>?

    /**
     * Return all the Layers in this [Environment].
     */
    val layers: ListSet<Layer<T, P>>

    /**
     * Returns the current [LinkingRule].
     */
    var linkingRule: LinkingRule<T, P>

    /**
     * Given a [node], this method returns its neighborhood.
     */
    fun getNeighborhood(node: Node<T>): Neighborhood<T>

    /**
     * Allows accessing a [Node] in this [Environment] known its [id].
     * Depending on the implementation, this method may or may not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     */
    fun getNodeByID(id: Int): Node<T>

    /**
     * Returns all the [Node]s that exist in current [Environment].
     */
    val nodes: ListSet<Node<T>>

    /**
     * Returns the number of [Node]s currently in the [Environment].
     */
    val nodeCount: Int

    /**
     * Given a [node] this method returns a list of all the surrounding
     * nodes within the given [range]. Note that this method (depending on the
     * implementation) might be not optimized, and it's consequently **much**
     * better to use [Environment.getNeighborhood] and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     */
    fun getNodesWithinRange(
        node: Node<T>,
        range: Double,
    ): ListSet<Node<T>>

    /**
     * Given a [position] this method returns a list of all the
     * surrounding nodes within the given [range].
     * Note that this method
     * (depending on the implementation) might be not optimized.
     */
    fun getNodesWithinRange(
        position: P,
        range: Double,
    ): ListSet<Node<T>>

    /**
     * This method allows to know which are the smallest coordinates represented.
     * Return an array of length [dimensions] containing the smallest
     * coordinates for each dimension.
     */
    val offset: DoubleArray

    /**
     * Calculates the position of a [node].
     */
    fun getPosition(node: Node<T>): P

    /**
     * Return the current [Simulation], if present, or throws an [IllegalStateException] otherwise.
     */
    var simulation: Simulation<T, P>

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
     * Return true if all the terminators are true.
     */
    val isTerminated: Boolean

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
     * This method moves a [node] in the environment to some [position].
     * If node movement is unsupported, it does nothing.
     */
    fun moveNodeToPosition(
        node: Node<T>,
        position: P,
    )

    /**
     * Removes [node].
     * If node removal is unsupported, it does nothing.
     */
    fun removeNode(node: Node<T>)
}
