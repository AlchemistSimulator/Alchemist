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
     * Get the {@link Environment}'s {@link GlobalReaction}s.
     *
     * @return the list of {@link GlobalReaction}s in this {@link Environment}.
     */
    fun getGlobalReactions(): Set<GlobalReaction<T>>

    /**
     * This method allows to add a new node to this environment. The environment
     * is responsible to call the right method of the simulation in order to
     * ensure that the reaction is properly scheduled.
     *
     * @param node The node to add
     * @param p    The position where to place it
     * @return true if node is added in the environment
     */
    fun addNode(node: Node<T>, position: P): Boolean

    /**
     * @param terminator a {@link Predicate} indicating whether the simulation should
     *                   be considered finished
     */
    fun addTerminator(terminator: Predicate<Environment<T, P>>)

    /**
     * The number of dimensions of this environment.
     *
     * @return the number of dimensions of this environment
     */
    fun getDimensions(): Int

    /**
     * Measures the distance between two nodes ([n1], [n2] in the environment.
     */
    fun getDistanceBetweenNodes(n1: Node<T>, n2: Node<T>): Double

    /**
     * @return the {@link Incarnation} used to initialize the entities of this {@link Environment}, if it has been set.
     */
    @NotNull
    fun getIncarnation(): Incarnation<T, P>

    /**
     * Get the layer associate to the given molecule. If no Layer is associated
     * with the given molecule, return an empty optional.
     *
     * @param m the {@link Molecule}
     * @return the {@link Optional} containing the {@link Layer} associated with
     * the requested molecule
     */
    fun getLayer(molecule: Molecule): Optional<Layer<T, P>>

    /**
     * Return all the Layers in this {@link Environment}.
     *
     * @return a {@link ListSet} of {@link Layer}.
     */
    fun getLayers(): Set<Layer<T, P>>

    /**
     * Returns the current linking rule.
     */
    fun getLinkingRule(): LinkingRule<T, P>

    /**
     * Set the [rule] passed as new [LinkingRule] of the environment.
     */
    fun setLinkingRule(rule: LinkingRule<T, P>)

    /**
     * Given a [node], this method returns its neighborhood.
     */
    fun getNeighborhood(node: Node<T>): Neighborhood<T>

    /**
     * Allows to access a node known its id. Depending on the implementation, this method may or not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     *
     * @param id the node's ID
     * @return the node with that id, or null if it does not exist in this
     * environment
     */
    fun getNodeByID(id: Int): Node<T>

    /**
     * All the nodes that exist in current environment.
     *
     * @return All the nodes that exist in current environment
     */
    fun getNodes(): Set<Node<T>>

    /**
     * Returns the number of nodes currently in the environment.
     */
    fun getNodeCount(): Int

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
     * Given a {@link Position}(center) this method returns a list of all the
     * surroundings nodes within the given range. Note that this method
     * (depending on the implementation) might be not optimized.
     *
     * @param center the {@link Position} to consider as center
     * @param range  the exploration range
     * @return the list of nodes within the range
     */
    fun getNodesWithinRange(position: P, range: Double): Collection<Node<T>>

    /**
     * This method allows to know which are the smallest coordinates
     * represented.
     *
     * @return an array of length getDimensions() containing the smallest
     * coordinates for each dimension.
     */
    fun getOffset(): DoubleArray

    /**
     * Calculates the position of a node.
     *
     * @param node the node you want to know the position
     * @return The position
     */
    @NotNull
    fun getPosition(node: Node<T>): P

    /**
     * @return the current simulation, if present, or throws an
     * {@link IllegalStateException} otherwise
     */
    fun getSimulation(): Simulation<T, P>

    /**
     * @param s the simulation
     */
    fun setSimulation(simulation: Simulation<T, P>)

    /**
     * This method returns the size of the environment as an array of length
     * {@link #getDimensions()}. This method must return distance measured with
     * the same unit used by the positions. No non-euclidean distance metrics
     * are allowed.
     *
     * @return the size of this environment
     */
    fun getSize(): DoubleArray

    /**
     * This method returns the size of the environment as an array of length
     * {@link #getDimensions()}. This method must return distance measured with
     * the same unit used for measuring distances. It may or may not return the
     * same result of {@link #getSize()}
     *
     * @return the size of this environment
     */
    fun getSizeInDistanceUnits(): DoubleArray

    /**
     * @return true if all the terminators are true
     */
    fun isTerminated(): Boolean

    /**
     * @param coordinates the coordinates of the point
     * @return a {@link Position} compatible with this environment
     */
    fun makePosition(coordinates: DoubleArray): P

    /**
     * This method moves a node in the environment to some position. If node
     * move is unsupported, it does nothing.
     *
     * @param node     The node to move
     * @param position The absolute position in which this node will be moved.
     */
    @NotNull
    fun moveNodeToPosition(@NotNull node: Node<T>, @NotNull position: P)

    /**
     * This method allows to remove a node. If node removal is unsupported, it
     * does nothing.
     *
     * @param node the node to remove
     */
    fun removeNode(node: Node<T>)
}
