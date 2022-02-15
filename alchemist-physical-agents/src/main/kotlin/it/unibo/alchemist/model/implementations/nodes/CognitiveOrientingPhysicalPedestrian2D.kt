/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.graph.DefaultEdge
import kotlin.reflect.KClass

/**
 * A cognitive [OrientingPedestrian2D] capable of physical interactions.
 * TODO(rename it into something like "SmartPedestrian2D"?)
 */
class CognitiveOrientingPhysicalPedestrian2D<T, N : ConvexPolygon, E> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    nodeCreationParameter: String? = null,
    override val knowledgeDegree: Double,
    group: PedestrianGroup2D<T>? = null,
    age: String,
    gender: String,
    danger: Molecule? = null
) : CognitivePhysicalPedestrian2D<T>(
    incarnation,
    randomGenerator,
    environment,
    nodeCreationParameter,
    age,
    gender,
    danger,
    group,
),
    OrientingPedestrian2D<T, Ellipse, DefaultEdge> by HomogeneousOrientingPedestrian2D(
        incarnation,
        randomGenerator,
        environment,
        nodeCreationParameter,
        knowledgeDegree,
    ) {

    override val fieldOfView = super.fieldOfView

    override val membershipGroup = super.membershipGroup

    override val shape = super.shape

    override fun addReaction(reactionToAdd: Reaction<T>) = super.addReaction(reactionToAdd)

    override fun cloneNode(currentTime: Time): Node<T> = TODO()

    override fun compareTo(other: Node<T>) = super.compareTo(other)

    override fun contains(molecule: Molecule) = super.contains(molecule)

    override fun getConcentration(molecule: Molecule) = super.getConcentration(molecule)

    override val contents = super.contents

    override val id = super.id

    override val moleculeCount = super.moleculeCount

    override val reactions = super.reactions

    override fun iterator() = super.iterator()

    override fun removeConcentration(moleculeToRemove: Molecule) = super.removeConcentration(moleculeToRemove)

    override fun removeReaction(reactionToRemove: Reaction<T>) = super.removeReaction(reactionToRemove)

    override fun setConcentration(molecule: Molecule, concentration: T) = super.setConcentration(molecule, concentration)

    override fun speed() = super.speed()

    override val capabilities: List<Capability<T>> = super.capabilities

    override fun addCapability(capability: Capability<T>) = super.addCapability(capability)

    override fun <C : Capability<T>> asCapability(superType: Class<C>): C =
        super<CognitivePhysicalPedestrian2D>.asCapability(superType)

    override fun <C : Capability<T>> asCapability(superType: KClass<C>): C =
        super<CognitivePhysicalPedestrian2D>.asCapability(superType)

    override fun <C : Capability<T>> asCapabilityOrNull(superType: Class<C>): C? =
        super<CognitivePhysicalPedestrian2D>.asCapabilityOrNull(superType)

    override fun <C : Capability<T>> asCapabilityOrNull(superType: KClass<C>): C? =
        super<CognitivePhysicalPedestrian2D>.asCapabilityOrNull(superType)

    override fun equals(other: Any?) = super.equals(other)

    override fun hashCode(): Int = super.hashCode()
}
