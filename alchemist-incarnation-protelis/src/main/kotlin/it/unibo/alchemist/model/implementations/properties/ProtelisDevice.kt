/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.ProtelisIncarnation
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.NodeProperty
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.protelis.AlchemistNetworkManager
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.vm.ExecutionEnvironment

/**
 * Base implementation of [ProtelisDevice]. Requires an [environment] to work.
 */
class ProtelisDevice<P : Position<P>> @JvmOverloads constructor(
    val environment: Environment<Any, P>,
    override val node: Node<Any>,
    networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager> = mapOf(),
) : NodeProperty<Any>, ExecutionEnvironment, DeviceUID {

    private val incarnation: ProtelisIncarnation<*> =
        environment.incarnation as? ProtelisIncarnation<P> ?: ProtelisIncarnation.INSTANCE

    /**
     * The node's id.
     */
    val id: Int get() = node.id

    /**
     * All the [AlchemistNetworkManager]s in this node.
     */
    var networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager> = networkManagers
        private set

    /**
     * Adds a new [AlchemistNetworkManager].
     *
     * @param program
     * the [RunProtelisProgram]
     * @param networkManager
     * the [AlchemistNetworkManager]
     */
    fun addNetworkManger(program: RunProtelisProgram<*>, networkManager: AlchemistNetworkManager) {
        networkManagers = networkManagers + (program to networkManager)
    }

    /**
     * Finds all the [RunProtelisProgram]s installed on this node.
     */
    fun allProtelisPrograms(): List<RunProtelisProgram<*>> = node.reactions.asSequence()
        .flatMap { it.actions }
        .filterIsInstance<RunProtelisProgram<*>>()
        .toList()

    override fun cloneOnNewNode(node: Node<Any>) = ProtelisDevice(environment, node)

    /**
     * Returns the value associated with [id].
     */
    override fun get(id: String): Any = incarnation.createMolecule(id).let { molecule ->
        when {
            node.contains(molecule) -> node.getConcentration(molecule)
            else -> environment.getLayer(molecule).map { it.getValue(environment.getPosition(node)) }.orElseThrow {
                IllegalArgumentException(
                    "Molecule (variable) \"$id\" not found in $this, nor a layer with the same name exists",
                )
            }
        }
    }

    /**
     * Returns the value associated with [id].
     */
    override fun get(id: String, defaultValue: Any): Any = get(id)

    /**
     * @param program
     * the [RunProtelisProgram]
     * @return the [AlchemistNetworkManager] for this specific
     * [RunProtelisProgram]
     */
    fun getNetworkManager(program: RunProtelisProgram<*>) = requireNotNull(networkManagers[program]) {
        "No network manager found for $program"
    }

    /**
     * Returns true if node contains [id].
     */
    override fun has(id: String): Boolean = node.contains(incarnation.createMolecule(id))

    /**
     * Stores a [value] associated with [key].
     */
    override fun put(key: String, value: Any): Boolean {
        node.setConcentration(incarnation.createMolecule(key), value)
        return true
    }

    /**
     * Stores a [value] associated with [key].
     */
    fun putField(key: String, value: Field<*>): Boolean {
        node.setConcentration(incarnation.createMolecule(key), value)
        return true
    }

    /**
     * Removes the value associated with [id].
     */
    override fun remove(id: String): Any {
        val value = get(id)
        node.removeConcentration(incarnation.createMolecule(id))
        return value
    }

    /**
     * Return all stored variables names.
     */
    override fun keySet(): Set<String> = node.contents.keys.mapNotNull { it.name }.toSet()

    /**
     * Called just after the VM is executed, to finalize information of the execution for the environment.
     */
    override fun commit() { /* Nothing to do */ }

    /**
     * Called just before the VM is executed, to enable and preparations needed in the environment.
     */
    override fun setup() { /* Nothing to do */ }

    override fun toString(): String = "PtDevice${node.id}"

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
