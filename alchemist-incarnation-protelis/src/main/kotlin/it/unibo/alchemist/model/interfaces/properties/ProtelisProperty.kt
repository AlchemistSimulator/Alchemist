/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.ProtelisIncarnation
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.interfaces.NodeProperty
import it.unibo.alchemist.protelis.AlchemistNetworkManager
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.vm.ExecutionEnvironment

/**
 *
 */
interface ProtelisProperty : NodeProperty<Any>, ExecutionEnvironment, DeviceUID {

    /**
     * The node's id.
     */
    val id: Int get() = node.id

    /**
     * An instance of a [ProtelisIncarnation].
     */
    val incarnation: ProtelisIncarnation<*>

    /**
     * All the [AlchemistNetworkManager]s in this node.
     */
    val networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager>

    /**
     * Adds a new [AlchemistNetworkManager].
     *
     * @param program
     * the [RunProtelisProgram]
     * @param networkManager
     * the [AlchemistNetworkManager]
     */
    fun addNetworkManger(program: RunProtelisProgram<*>, networkManager: AlchemistNetworkManager)

    /**
     * @param program
     * the [RunProtelisProgram]
     * @return the [AlchemistNetworkManager] for this specific
     * [RunProtelisProgram]
     */
    fun getNetworkManager(program: RunProtelisProgram<*>): AlchemistNetworkManager =
        requireNotNull(networkManagers[program]) { "No network manager associated with $program" }

    /**
     * Writes a Map representation of the Field on the environment.
     *
     * @param id variable name
     * @param v the [Field]
     * @return true
     */
    fun putField(id: String, v: Field<Any>) = true.also {
        node.setConcentration(incarnation.createMolecule(id), v.toMap())
    }
}
