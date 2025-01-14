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
import org.jetbrains.annotations.NotNull
import java.io.Serializable
import java.util.Optional
import java.util.function.Predicate

interface Environment<T, P : Position<P>> : Serializable, Iterable<Node<T>> {

    /**
     * Add a [layer] identified by [molecule] to the [Environment].
     */
    fun addLayer(molecule: Molecule, layer: Layer<T, P>)

    /**
     * Add a [reaction] to the [Environment].
     */
    fun addGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Remove a [reaction] from the [Environment].
     */
    fun removeGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Get the [GlobalReaction]s in this [Environment].
     */
    val globalReactions: Set<GlobalReaction<T>>

    /**
     * This method allows to add a new [node] to this environment in a specific [position].
     * The environment is responsible to call the right method of the simulation in order to
     * ensure that the reaction is properly scheduled.
     * The function returns true if the node is added to the environment.
     */
    fun addNode(node: Node<T>, position: P): Boolean

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     */
    fun addTerminator(terminator: Predicate<Environment<T, P>>)

    /**
     * The number of dimensions of this environment.
     */
    val dimensions: Int

    /**
     * Measures the distance between two nodes ([n1], [n2]) in the environment.
     */
    fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double

    /**
     * Return the [Incarnation] used to initialize the entities of this [Environment], if it has been set.
     */
    val incarnation: Incarnation<T, P>

    /**
     * Get the [Layer] associate to the given [molecule]. If no Layer is associated
     * with the given molecule, return an empty optional.
     */
    fun getLayer(molecule: Molecule): Optional<Layer<T, P>>

    /**
     * Return all the [Layer]s in this [Environment].
     */
    val layers: Set<Layer<T, P>>

    /**
     * Returns the current [LinkingRule].
     */
    val linkingRule: LinkingRule<T, P>

    /**
     * Set the [rule] passed as new [LinkingRule] of the environment.
     */
    fun setLinkingRule(rule: LinkingRule<T, P>)

    /**
     * Given a [node], this method returns its neighborhood.
     */
    fun getNeighborhood(node: Node<T>): Neighborhood<T>

    /**
     * Allows to access a [Node] in this [Environment] known its [id].
     * Depending on the implementation, this method may or not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     */
    fun getNodeByID(id: Int): Node<T>

    /**
     * Returns all the [Node]s that exist in current [Environment].
     */
    val nodes: Set<Node<T>>

    /**
     * Returns the number of [Node]s currently in the [Environment].
     */
    val nodeCount: Int

    /**
     * Given a [node] this method returns a list of all the surroundings
     * nodes within the given [range]. Note that this method (depending on the
     * implementation) might be not optimized and it's consequently <b>much</b>
     * better to use {@link Environment#getNeighborhood(Node)} and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     */
    fun getNodesWithinRange(node: Node<T>, range: Double): Collection<Node<T>>

    /**
     * Given a [position] this method returns a list of all the
     * surroundings nodes within the given [range]. Note that this method
     * (depending on the implementation) might be not optimized.
     */
    fun getNodesWithinRange(position: P, range: Double): Collection<Node<T>>

    /**
     * This method allows to know which are the smallest coordinates
     * represented.
     * Return an array of length getDimensions() containing the smallest
     * coordinates for each dimension.
     */
    val offset: DoubleArray

    /**
     * Calculates the position of a [node].
     */
    fun getPosition(node: Node<T>): P

    /**
     * Return the current [Simulation], if present, or throws an [IllegalStateException] otherwise
     */
    val simulation: Simulation<T, P>

    /**
     * Set the [simulation] given as current.
     */
    fun setSimulation(simulation: Simulation<T, P>)

    /**
     * The size of the environment as an array of length getDimensions().
     * This method must return distance measured with the same unit used by the positions.
     * No non-euclidean distance metrics are allowed.
     */
    val size: DoubleArray

    /**
     * This method returns the size of the environment as an array of length
     * getDimensions(). This method must return distance measured with
     * the same unit used for measuring distances.
     * It may or may not return the same result of getSize().
     */
    val sizeInDistanceUnits: DoubleArray

    /**
     * Returns true if all the terminators are true
     */
    val isTerminated: Boolean

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(coordinates: DoubleArray): P

    /**
     * This method moves a [node] in the environment to some [position].
     * If node move is unsupported, it does nothing.
     */
    fun moveNodeToPosition(@NotNull node: Node<T>, @NotNull position: P)

    /**
     * This method allows to remove a [node].
     * If node removal is unsupported, it does nothing.
     */
    fun removeNode(node: Node<T>)
}
