/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.nodes

import arrow.core.Option
import com.google.common.collect.MapMaker
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableMutableMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.Nonnull

/**
 * This class realizes an abstract node. You may extend it to realize your own
 * nodes.
 *
 * @param <T> concentration type
</T> */
open class GenericNode<T> @JvmOverloads constructor(
    /**
     * simulation incarnation.
     */
    val incarnation: Incarnation<T, *>,
    /**
     * The environment in which the node is placed.
     */
    val environment: Environment<T, *>,
    final override val id: Int = idFromEnv(environment),
    final override val reactions: MutableList<Reaction<T>> = ArrayList(),
    /**
     * The node's molecules.
     */
    final override val contents: ObservableMutableMap<Molecule, T> = ObservableMutableMap(),
    final override val properties: MutableList<NodeProperty<T>> = ArrayList(),
) : Node<T> {

    constructor(
        environment: Environment<T, *>,
    ) : this(environment.incarnation, environment)

    final override fun addReaction(reactionToAdd: Reaction<T>) {
        reactions.add(reactionToAdd)
    }

    override fun cloneNode(currentTime: Time): Node<T> = GenericNode(
        incarnation,
        environment,
        contents = contents.copy(),
    ).also {
        this.properties.forEach { property -> it.addProperty(property.cloneOnNewNode(it)) }
        this.reactions.forEach { reaction -> it.addReaction(reaction.cloneOnNewNode(it, currentTime)) }
    }

    final override fun compareTo(@Nonnull other: Node<T>): Int = id.compareTo(other.id)

    override fun contains(molecule: Molecule): Observable<Boolean> = contents[molecule].map { it.isSome() }

    final override fun equals(other: Any?): Boolean = other is Node<*> && other.id == id

    override fun getConcentration(molecule: Molecule): Observable<Option<T>> = contents[molecule]

    override val moleculeCount: Observable<Int> = contents.map { it.size }

    final override fun hashCode(): Int = id // TODO: better hashing

    final override fun removeConcentration(moleculeToRemove: Molecule) {
        contents.remove(moleculeToRemove)
    }

    final override fun removeReaction(reactionToRemove: Reaction<T>) {
        reactions.remove(reactionToRemove)
    }

    override fun setConcentration(molecule: Molecule, concentration: T) {
        contents[molecule] = concentration
    }

    final override fun addProperty(nodeProperty: NodeProperty<T>) {
        if (properties.none { it::class == nodeProperty::class }) {
            properties.add(nodeProperty)
        } else {
            error(
                "Node with id ${this.id} already contains a property of type ${nodeProperty::class}, " +
                    "this may lead to an inconsistent state",
            )
        }
    }

    override fun toString(): String = "Node$id{ properties: $properties, molecules: $contents }"

    private companion object {
        private const val serialVersionUID = 2496775909028222278L

        private val IDGENERATOR = MapMaker().weakKeys().makeMap<Environment<*, *>, AtomicInteger>()

        private val MUTEX = Semaphore(1)

        private fun idFromEnv(environment: Environment<*, *>): Int {
            MUTEX.acquireUninterruptibly()
            var idgen = IDGENERATOR[environment]
            if (idgen == null) {
                idgen = AtomicInteger()
                IDGENERATOR[environment] = idgen
            }
            MUTEX.release()
            return idgen.getAndIncrement()
        }
    }
}
