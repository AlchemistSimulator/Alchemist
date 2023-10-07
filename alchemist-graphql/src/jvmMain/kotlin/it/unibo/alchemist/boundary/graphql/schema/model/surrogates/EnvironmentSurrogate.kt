/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model.surrogates

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import it.unibo.alchemist.boundary.graphql.schema.util.NodeToPosMap
import it.unibo.alchemist.boundary.graphql.schema.util.toNodeToPosMap
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.sync.Semaphore
import kotlin.jvm.optionals.getOrNull

/**
 * A surrogate for [Environment].
 * @param T the concentration type
 * @param P the position
 * @param dimensions the number of dimensions of this environment.
 */
@GraphQLDescription("The simulation environment")
data class EnvironmentSurrogate<T, P : Position<out P>>(
    @GraphQLIgnore override val origin: Environment<T, P>,
    @GraphQLIgnore val simulation: Simulation<T, P>,
    val dimensions: Int = origin.dimensions,
) : GraphQLSurrogate<Environment<T, P>>(origin) {

    /**
     * The [Layer]s of this environment associated with their corresponding [Molecule].
     * The first initialization is made upon all molecules contained inside this environment's nodes.
     * Subsequent updates should be made whenever the [Environment.addLayer] is called.
     */
    private val moleculeToLayer: Map<Molecule, Layer<T, P>?> =
        origin.nodes.map { it.contents.keys }
            .flatten()
            .distinct()
            .associateWith { origin.getLayer(it).getOrNull() }

    /**
     * The nodes inside this environment.
     * @return the nodes in this environment.
     */
    @GraphQLDescription("The nodes in this environment")
    fun nodes() = origin.nodes.map { NodeSurrogate(it) }

    /**
     * The layers inside this environment.
     * @return the layers in this environment.
     */
    @GraphQLDescription("The layers in this environment")
    fun layers() = origin.layers.map {
        it.toGraphQLLayerSurrogate { coordinates ->
            origin.makePosition(*coordinates.toTypedArray())
        }
    }

    /**
     * Returns the node with the given id.
     * @param id the id of the node
     */
    @GraphQLDescription("The node with the given id")
    fun nodeById(id: Int): NodeSurrogate<T> = origin.getNodeByID(id).toGraphQLNodeSurrogate()

    /**
     * Returns a [NodeToPosMap] representing all nodes associated with their position.
     */
    @GraphQLDescription("A list of entries NodeId-Position")
    fun nodeToPos(): NodeToPosMap = origin.nodes.associate { it.id to origin.getPosition(it) }.toNodeToPosMap()

    /**
     * Returns the neighborhood of the node with the given id.
     *
     * @param nodeId the id of the node
     * @return the neighborhood of the node with the given id.
     */
    @GraphQLDescription("The neighborhood of the node with the given id")
    fun getNeighborhood(nodeId: Int): NeighborhoodSurrogate<T> =
        origin.getNeighborhood(origin.getNodeByID(nodeId)).toGraphQLNeighborhoodSurrogate()

    /**
     * Clone the node associated with the given id to the specified position.
     *
     * @param nodeId the id of the node to clone
     * @param position the position where to clone the node
     * @return true if the node has been cloned, false otherwise
     */
    @GraphQLDescription("Clone the node associated with the given id to the specified position")
    suspend fun cloneNode(nodeId: Int, position: PositionInput, time: TimeSurrogate): NodeSurrogate<T>? {
        val newNode = origin.getNodeByID(nodeId).cloneNode(time.toAlchemistTime())
        val mutex = Semaphore(1, 1)
        var isAdded: Boolean = false
        simulation.schedule {
            try {
                isAdded = origin.addNode(newNode, origin.makePosition(*position.coordinates.toTypedArray()))
            } finally {
                mutex.release()
            }
        }
        mutex.acquire()
        return if (isAdded) newNode.toGraphQLNodeSurrogate() else null
    }

    /**
     * Returns the [LayerSurrogate] associated with the molecule represented by the given [MoleculeInput].
     *
     * @param m the [MoleculeInput] object associated to the layer
     * @return the [LayerSurrogate] associated with the molecule represented by the given [MoleculeInput]
     */
    @GraphQLDescription("The layer associated with the molecule represented by the given MoleculeInput")
    fun getLayer(m: MoleculeInput): LayerSurrogate<T, P>? =
        getLayerFromMoleculeInput(m)?.toGraphQLLayerSurrogate { coordinates ->
            origin.makePosition(*coordinates.toTypedArray())
        }

    /**
     * Returns the [Layer] associated with the molecule represented by the given [MoleculeInput].
     * NB: molecule is resolved with name matching.
     *
     * @param m the [MoleculeInput] object
     * @return the [Layer] associated with the molecule represented by the given [MoleculeInput]
     */
    private fun getLayerFromMoleculeInput(m: MoleculeInput) =
        moleculeToLayer.filterKeys { it.name == m.name }.values.firstOrNull()
}

/**
 * Converts an [Environment] to a [EnvironmentSurrogate].
 * @param T the concentration type
 * @param P the position
 * @param simulation the simulation containing this environment
 * @return a [EnvironmentSurrogate] representing the given [Environment]
 */
fun <T, P : Position<out P>> Environment<T, P>.toGraphQLEnvironmentSurrogate(simulation: Simulation<T, P>) = EnvironmentSurrogate(this, simulation)
