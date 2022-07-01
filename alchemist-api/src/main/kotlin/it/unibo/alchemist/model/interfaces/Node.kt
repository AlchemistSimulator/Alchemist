/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.Node.Companion.takeIfInstance
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * @param <T>
 * The type of the concentration
 *
 * This interface must be implemented in every realization of node
</T> */
interface Node<T> : Serializable, Iterable<Reaction<T>>, Comparable<Node<T>> {
    /**
     * Adds a reaction to this node.
     * The reaction is added only in the node,
     * but not in the [it.unibo.alchemist.core.interfaces.Simulation] scheduler,
     * so it will never be executed.
     * To add the reaction also in the scheduler (and start to execute it),
     * you have to call also the method
     * [it.unibo.alchemist.core.interfaces.Simulation.reactionAdded].
     *
     * @param reactionToAdd the reaction to be added
     */
    fun addReaction(reactionToAdd: Reaction<T>)

    /**
     * Creates a new Node which is a clone of the current Node. The new Node
     * will have all the current Node's properties, such as reactions and
     * molecules, but it will also have a different ID.
     *
     * @param currentTime
     * the time at which the cloning operation happens
     *
     * @return A new Node which is a clone of the current one.
     *
     * @throws UnsupportedOperationException
     * if the implementation does not support node cloning.
     */
    fun cloneNode(currentTime: Time): Node<T>

    /**
     * Tests whether a node contains a [Molecule].
     *
     * @param molecule
     * the molecule to check
     * @return true if the molecule is present, false otherwise
     */
    operator fun contains(molecule: Molecule): Boolean

    /**
     * Calculates the concentration of a molecule.
     *
     * @param molecule
     * the molecule whose concentration will be returned
     * @return the concentration of the molecule
     */
    fun getConcentration(molecule: Molecule): T

    /**
     * @return the molecule corresponding to the i-th position
     */
    val contents: Map<Molecule, T>

    /**
     * @return an univocal id for this node in the environment
     */
    val id: Int

    /**
     * @return the count of different molecules in this node
     */
    val moleculeCount: Int

    /**
     * @return a list of the node's properties/capabilities
     */
    val properties: List<NodeProperty<T>>

    /**
     * This method allows to access all the reaction of the node.
     *
     * @return the list of rections belonging to this node
     */
    val reactions: List<Reaction<T>>

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean

    /**
     * @param moleculeToRemove the molecule that should be removed
     */
    fun removeConcentration(moleculeToRemove: Molecule)

    /**
     * Removes a reaction from this node.
     * The reaction is removed only in the node,
     * but not in the [it.unibo.alchemist.core.interfaces.Simulation] scheduler,
     * so the scheduler will continue to execute the reaction.
     * To remove the reaction also in the scheduler (and stop to execute it),
     * you have to call also the method [it.unibo.alchemist.core.interfaces.Simulation.reactionRemoved].
     *
     * @param reactionToRemove the reaction to be removed
     */
    fun removeReaction(reactionToRemove: Reaction<T>)

    /**
     * Sets the concentration of mol to c.
     *
     * @param molecule
     * the molecule you want to set the concentration
     * @param concentration
     * the concentration you want for mol
     */
    fun setConcentration(molecule: Molecule, concentration: T)

    /**
     * Adds a capability to the node.
     * @param nodeProperty the capability you want to add to the node
     */
    fun addProperty(nodeProperty: NodeProperty<T>)

    /**
     * returns a [NodeProperty] of the provided [superType] [C].
     * @param [C] type of capability
     * @param superType the type of capability to retrieve
     * @return a capability of the provided type [C]
     */
    fun <C : NodeProperty<T>> asPropertyOrNull(superType: Class<in C>): C? = asPropertyOrNull(superType.kotlin)

    /**
     * returns a [NodeProperty] of the provided [superType] [C].
     * @param [C] type of capability
     * @param superType the type of capability to retrieve
     * @return a capability of the provided type [C]
     */
    @Suppress("UNCHECKED_CAST")
    fun <C : NodeProperty<T>> asPropertyOrNull(superType: KClass<in C>): C? = when {
        properties.size <= 1 -> properties.firstOrNull()?.takeIfInstance(superType)
        else -> {
            val validProperties: List<C> = properties.mapNotNull { it.takeIfInstance(superType) }
            when {
                validProperties.size <= 1 -> validProperties.firstOrNull()
                else -> {
                    validProperties
                        .mapNotNull { nodeProperty: NodeProperty<T> ->
                            nodeProperty::class.distanceFrom(superType)?.let { nodeProperty to it }
                        }
                        .minByOrNull { it.second }
                        ?.first as? C
                }
            }
        }
    }

    /**
     * returns a [NodeProperty] of the provided [superType] [C].
     * @param [C] type of capability
     * @param superType the type of capability to retrieve
     * @return a capability of the provided type [C]
     */
    fun <C : NodeProperty<T>> asProperty(superType: KClass<C>): C =
        requireNotNull(asPropertyOrNull(superType)) {
            "A ${superType.simpleName} is required for node ${this.id} but is missing"
        }

    /**
     * returns a [NodeProperty] of the provided [superType] [C].
     * @param [C] type of capability
     * @param superType the type of capability to retrieve
     * @return a capability of the provided type [C]
     */
    fun <C : NodeProperty<T>> asProperty(superType: Class<C>): C = asProperty(superType.kotlin)

    companion object {
        /**
         * returns a [NodeProperty] of the provided type [C].
         * @param [C] type of capability
         * @return a capability of the provided type [C]
         */
        inline fun <T, reified C : NodeProperty<T>> Node<T>.asProperty(): C = asProperty(C::class)

        /**
         * returns a [NodeProperty] of the provided type [C] or null if the node does not have a compatible property.
         * @param [C] type of capability
         * @return if present, a capability of the provided type [C]
         */
        inline fun <T, reified C : NodeProperty<T>> Node<T>.asPropertyOrNull(): C? = when {
            properties.size <= 1 -> properties.firstOrNull() as? C
            else -> {
                val validProperties: List<C> = properties.filterIsInstance<C>()
                when {
                    validProperties.size <= 1 ->
                        validProperties.firstOrNull()
                    else ->
                        validProperties
                            .mapNotNull { nodeProperty: NodeProperty<T> ->
                                nodeProperty::class.distanceFrom(C::class)?.let { nodeProperty to it }
                            }
                            .minByOrNull { it.second }
                            ?.first as? C
                }
            }
        }

        @JvmSynthetic @PublishedApi internal fun KClass<*>.distanceFrom(
            superType: KClass<*>,
            depth: Int = 0
        ): Int? = when {
            !isSubclassOf(superType) -> null
            superType == this -> depth
            else -> supertypes.asSequence()
                .map { it.jvmErasure }
                .mapNotNull { it.distanceFrom(superType, depth + 1) }
                .minOrNull()
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T, C : NodeProperty<T>> NodeProperty<T>.takeIfInstance(type: KClass<in C>): C? =
            this.takeIf { type.isInstance(it) }?.let { it as? C }
    }
}
