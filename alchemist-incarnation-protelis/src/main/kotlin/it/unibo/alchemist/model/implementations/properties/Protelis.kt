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
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.properties.ProtelisProperty
import it.unibo.alchemist.protelis.AlchemistNetworkManager

/**
 * Base implementation of [ProtelisProperty].
 */
class Protelis<P : Position<P>> @JvmOverloads constructor(
    override val incarnation: ProtelisIncarnation<*>,
    override val node: Node<Any>,
    networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager> = mapOf()
) : ProtelisProperty {

    override var networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager> = networkManagers
        private set

    override fun addNetworkManger(program: RunProtelisProgram<*>, networkManager: AlchemistNetworkManager) {
        networkManagers = networkManagers + (program to networkManager)
    }

    /**
     * Returns true if node contains [id].
     */
    override fun has(id: String): Boolean = node.contains(incarnation.createMolecule(id))

    /**
     * Returns the value associated with [id].
     */
    override fun get(id: String): Any = node.getConcentration(incarnation.createMolecule(id))

    /**
     * Returns the value associated with [id].
     */
    override fun get(id: String, defaultValue: Any): Any = get(id)

    /**
     * Stores the value associated with [id].
     */
    override fun put(id: String, v: Any): Boolean {
        node.setConcentration(incarnation.createMolecule(id), v)
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
    override fun commit() = throw NotImplementedError()

    /**
     * Called just before the VM is executed, to enable and preparations needed in the environment.
     */
    override fun setup() = throw NotImplementedError()
}
