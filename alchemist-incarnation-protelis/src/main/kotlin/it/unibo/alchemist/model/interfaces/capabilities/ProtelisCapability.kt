/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.capabilities

import it.unibo.alchemist.model.ProtelisIncarnation
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.protelis.AlchemistNetworkManager
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.vm.ExecutionEnvironment

interface ProtelisCapability : Capability<Any>, ExecutionEnvironment, DeviceUID {

    /**
     * All the [AlchemistNetworkManager]s in this node.
     */
    val networkManagers: MutableMap<RunProtelisProgram<*>, AlchemistNetworkManager>
    /**
     * Adds a new [AlchemistNetworkManager].
     *
     * @param program
     * the [RunProtelisProgram]
     * @param networkManager
     * the [AlchemistNetworkManager]
     */
    fun addNetworkManger(program: RunProtelisProgram<*>, networkManager: AlchemistNetworkManager) {
        networkManagers[program] = networkManager
    }

    /**
     * @param program
     * the [RunProtelisProgram]
     * @return the [AlchemistNetworkManager] for this specific
     * [RunProtelisProgram]
     */
    fun getNetworkManager(program: RunProtelisProgram<*>): AlchemistNetworkManager? {
        return networkManagers[program]
    }

    /**
     * Writes a Map representation of the Field on the environment.
     *
     * @param id variable name
     * @param v the [Field]
     * @return true
     */
    fun putField(id: String, v: Field<Any>): Boolean {
        node.setConcentration(makeMol(id), v.toMap())
        return true
    }

    companion object {
        fun <P : Position<P>?> makeMol(id: String): Molecule {
            return ProtelisIncarnation<P>().createMolecule(id)
        }
    }
}
