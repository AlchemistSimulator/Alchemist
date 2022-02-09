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
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.graph.DefaultEdge

/**
 * A homogeneous [OrientingPedestrian2D] capable of physical interactions.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class HomogeneousOrientingPhysicalPedestrian2D<T, N : ConvexPolygon, E> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    nodeCreationParameter: String? = null,
    knowledgeDegree: Double,
    group: PedestrianGroup2D<T>? = null
) : HomogeneousPhysicalPedestrian2D<T>(incarnation, randomGenerator, environment, nodeCreationParameter, group),
    OrientingPedestrian2D<T, Ellipse, DefaultEdge> by HomogeneousOrientingPedestrian2D(
        incarnation = incarnation,
        randomGenerator = randomGenerator,
        environment = environment,
        nodeCreationParameter = nodeCreationParameter,
        knowledgeDegree = knowledgeDegree,
        group = group,
    ) {

    override val fieldOfView: FieldOfView2D<T>
        get() = super.fieldOfView

    override val membershipGroup: PedestrianGroup<T, Euclidean2DPosition, Euclidean2DTransformation>
        get() = super.membershipGroup

    override fun addReaction(reaction: Reaction<T>?) {
        super.addReaction(reaction)
    }

    override fun cloneNode(currentTime: Time?): Node<T> = TODO()

    override fun compareTo(other: Node<T>?): Int {
        return super.compareTo(other)
    }

    override fun contains(molecule: Molecule?): Boolean {
        return super.contains(molecule)
    }

    override fun getConcentration(molecule: Molecule?): T {
        return super.getConcentration(molecule)
    }

    override fun getContents(): MutableMap<Molecule, T> {
        return super.contents
    }

    override fun getId(): Int {
        return super.id
    }

    override fun getMoleculeCount(): Int {
        return super.moleculeCount
    }

    override fun getReactions(): MutableList<Reaction<T>> {
        return super.reactions
    }

    override fun iterator(): MutableIterator<Reaction<T>> {
        return super.iterator()
    }

    override fun removeConcentration(mol: Molecule?) {
        super.removeConcentration(mol)
    }

    override fun removeReaction(r: Reaction<T>?) {
        super.removeReaction(r)
    }

    override fun setConcentration(mol: Molecule?, c: T) {
        super.setConcentration(mol, c)
    }

    override fun speed(): Double {
        return super.speed()
    }

    override val shape: GeometricShape<Euclidean2DPosition, Euclidean2DTransformation>
        get() = super.shape
}
