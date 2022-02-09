/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes

import com.google.common.collect.MapMaker
import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import java.lang.UnsupportedOperationException
import java.util.ArrayList
import java.util.Collections
import java.util.LinkedHashMap
import java.util.NoSuchElementException
import java.util.Objects
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
abstract class AbstractNode<T>(
    val env: Environment<*, *>,
    override val id: Int = idFromEnv(env),
    override val reactions: MutableList<Reaction<T>> = ArrayList(),
    val molecules: MutableMap<Molecule, T> = LinkedHashMap(),
    override val capabilities: MutableList<Capability> = ArrayList()
) : Node<T> {

    override fun addReaction(reactionToAdd: Reaction<T>) {
        reactions.add(reactionToAdd)
    }

    /**
     * Default implementation fails: override correctly calling the constructor.
     */
    override fun cloneNode(currentTime: Time): AbstractNode<T> {
        throw UnsupportedOperationException()
    }

    override fun compareTo(@Nonnull other: Node<T>): Int {
        if (other is AbstractNode<*>) {
            if (id > (other as AbstractNode<*>).id) {
                return 1
            }
            if (id < (other as AbstractNode<*>).id) {
                return -1
            }
        }
        return 0
    }

    /**
     * {@inheritDoc}
     */
    override fun contains(molecule: Molecule): Boolean {
        return molecules.containsKey(molecule)
    }

    /**
     * @return an empty concentration
     */
    protected abstract fun createT(): T
    override fun equals(other: Any?): Boolean {
        return if (other is AbstractNode<*>) {
            other.id == id
        } else false
    }

    override fun forEach(action: Consumer<in Reaction<T>>) {
        reactions.forEach(action)
    }

    /**
     * {@inheritDoc}
     */
    override fun getConcentration(molecule: Molecule): T {
        return molecules[molecule] ?: return createT()
    }

    /**
     * {@inheritDoc}
     */
    override fun getContents(): Map<Molecule, T> {
        return Collections.unmodifiableMap(molecules)
    }

    /**
     * {@inheritDoc}
     */
    override val moleculeCount: Int
        get() = molecules.size

    override fun hashCode(): Int {
        return id
    }

    override fun iterator(): Iterator<Reaction<T>> {
        return reactions.iterator()
    }

    /**
     * {@inheritDoc}
     */
    override fun removeConcentration(moleculeToRemove: Molecule) {
        if (molecules.remove(moleculeToRemove) == null) {
            throw NoSuchElementException("$moleculeToRemove was not present in node $id")
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun removeReaction(reactionToRemove: Reaction<T>) {
        reactions.remove(reactionToRemove)
    }

    /**
     * {@inheritDoc}
     */
    override fun setConcentration(molecule: Molecule, concentration: T) {
        molecules[molecule] = concentration
    }

    /**
     * {@inheritDoc}
     */
    override fun addCapability(capability: Capability) {
        capabilities.add(capability)
    }

    override fun spliterator(): Spliterator<Reaction<T>> {
        return reactions.spliterator()
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return molecules.toString()
    }

    companion object {
        private const val serialVersionUID = 2496775909028222278L
        private val IDGENERATOR = MapMaker()
            .weakKeys().makeMap<Environment<*, *>, AtomicInteger>()
        private val MUTEX = Semaphore(1)
        private fun idFromEnv(env: Environment<*, *>): Int {
            MUTEX.acquireUninterruptibly()
            var idgen = IDGENERATOR[Objects.requireNonNull(env)]
            if (idgen == null) {
                idgen = AtomicInteger()
                IDGENERATOR[env] = idgen
            }
            MUTEX.release()
            return idgen.getAndIncrement()
        }
    }
}
