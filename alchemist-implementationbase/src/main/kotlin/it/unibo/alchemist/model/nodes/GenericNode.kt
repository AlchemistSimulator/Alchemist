/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.nodes

import com.google.common.collect.MapMaker
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import java.util.Collections
import java.util.Spliterator
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
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
     * The environment in which the node is places.
     */
    val environment: Environment<T, *>,
    final override val id: Int = idFromEnv(environment),
    final override val reactions: MutableList<Reaction<T>> = ArrayList(),
    /**
     * The node's molecules.
     */
    val molecules: MutableMap<Molecule, T> = LinkedHashMap(),
    final override val properties: MutableList<NodeProperty<T>> = ArrayList(),
) : Node<T> {

    constructor(
        environment: Environment<T, *>,
    ) : this(environment.incarnation, environment)

    final override fun addReaction(reactionToAdd: Reaction<T>) {
        reactions.add(reactionToAdd)
    }

    override fun cloneNode(currentTime: Time): Node<T> = GenericNode(environment).also {
        this.properties.forEach { property -> it.addProperty(property.cloneOnNewNode(it)) }
        this.contents.forEach(it::setConcentration)
        this.reactions.forEach { reaction -> it.addReaction(reaction.cloneOnNewNode(it, currentTime)) }
    }

    final override fun compareTo(@Nonnull other: Node<T>): Int = id.compareTo(other.id)

    override fun contains(molecule: Molecule): Boolean = molecules.containsKey(molecule)

    /**
     * @return an empty concentration
     */
    protected open fun createT(): T = incarnation.createConcentration()

    final override fun equals(other: Any?): Boolean = other is Node<*> && other.id == id

    /**
     * Performs an [action] for every reaction.
     */
    final override fun forEach(action: Consumer<in Reaction<T>>) = reactions.forEach(action)

    override fun getConcentration(molecule: Molecule): T = molecules[molecule] ?: createT()

    override val contents: Map<Molecule, T> = Collections.unmodifiableMap(molecules)

    override val moleculeCount: Int get() = molecules.size

    final override fun hashCode(): Int = id // TODO: better hashing

    final override fun iterator(): Iterator<Reaction<T>> = reactions.iterator()

    final override fun removeConcentration(moleculeToRemove: Molecule) {
        if (molecules.remove(moleculeToRemove) == null) {
            throw NoSuchElementException("$moleculeToRemove was not present in node $id")
        }
    }

    final override fun removeReaction(reactionToRemove: Reaction<T>) {
        reactions.remove(reactionToRemove)
    }

    override fun setConcentration(molecule: Molecule, concentration: T) {
        molecules[molecule] = concentration
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

    /**
     * Returns the [reactions] [Spliterator].
     */
    final override fun spliterator(): Spliterator<Reaction<T>> = reactions.spliterator()

    override fun toString(): String = "Node$id{ properties: $properties, molecules: $molecules }"

    companion object {
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
