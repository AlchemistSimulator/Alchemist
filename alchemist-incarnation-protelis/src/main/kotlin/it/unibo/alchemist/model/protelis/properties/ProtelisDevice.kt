/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis.properties

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.protelis.AlchemistExecutionContext
import it.unibo.alchemist.model.protelis.AlchemistNetworkManager
import it.unibo.alchemist.model.protelis.actions.RunProtelisProgram
import it.unibo.alchemist.model.protelis.actions.SendToNeighbor
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.vm.ExecutionEnvironment
import org.slf4j.LoggerFactory

/**
 * Base implementation of [ProtelisDevice]. Requires an [environment] to work.
 */
class ProtelisDevice<P : Position<P>>
@JvmOverloads
constructor(
    val environment: Environment<Any, P>,
    override val node: Node<Any>,
    networkManagers: Map<RunProtelisProgram<*>, AlchemistNetworkManager> = mapOf(),
) : NodeProperty<Any>,
    ExecutionEnvironment,
    DeviceUID {
    private val incarnation: ProtelisIncarnation<*> =
        environment.incarnation as? ProtelisIncarnation<P>
            ?: ProtelisIncarnation.INSTANCE

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
     * All the [AlchemistExecutionContext]s in this node.
     */
    private var executionContexts: Map<RunProtelisProgram<*>, AlchemistExecutionContext<P>> = mapOf()

    /**
     * Gets or creates the [AlchemistNetworkManager] for the given program.
     * Creates the network manager lazily if it doesn't exist.
     *
     * @param program the [RunProtelisProgram]
     * @return the [AlchemistNetworkManager] for the program
     */
    fun networkManagerOf(program: RunProtelisProgram<*>): AlchemistNetworkManager = networkManagers[program] ?: run {
        val networkManager = AlchemistNetworkManager(
            program.reaction,
            this,
            program,
            program.retentionTime,
            program.packetLossDistance,
        )
        networkManagers = networkManagers + (program to networkManager)
        networkManager
    }

    /**
     * Gets or creates the [AlchemistExecutionContext] for the given program.
     * Creates the execution context lazily if it doesn't exist.
     *
     * @param program the [RunProtelisProgram]
     * @return the [AlchemistExecutionContext] for the program
     */
    fun executionContextOf(program: RunProtelisProgram<*>): AlchemistExecutionContext<P> =
        executionContexts[program] ?: run {
            val networkManager = networkManagerOf(program)
            val executionContext = AlchemistExecutionContext(
                environment,
                node,
                this,
                program.reaction,
                program.randomGenerator,
                networkManager,
            )
            executionContexts = executionContexts + (program to executionContext)
            executionContext
        }

    /**
     * Adds a new [AlchemistNetworkManager].
     *
     * Deprecated: prefer [networkManagerOf] for better encapsulation.
     *
     * @param program the [RunProtelisProgram]
     * @param networkManager the [AlchemistNetworkManager]
     */
    @Deprecated("Use networkManagerOf instead", ReplaceWith("networkManagerOf"))
    fun addNetworkManger(program: RunProtelisProgram<*>, networkManager: AlchemistNetworkManager) {
        networkManagers = networkManagers + (program to networkManager)
    }

    /**
     * Finds all the [RunProtelisProgram]s installed on this node.
     */
    fun allProtelisPrograms(): List<RunProtelisProgram<*>> = node.reactions
        .asSequence()
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
            else ->
                checkNotNull(environment.getLayer(molecule)?.getValue(environment.getCurrentPosition(node))) {
                    "Molecule (variable) \"$id\" not found in $this, nor a layer with the same name exists"
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
    fun getNetworkManager(program: RunProtelisProgram<*>) = networkManagerOf(program)

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
    override fun keySet(): Set<String> = node.contents.keys
        .mapNotNull { it.name }
        .toSet()

    /**
     * Called just after the VM is executed, to finalize information of the execution for the environment.
     */
    override fun commit() { /* Nothing to do */ }

    /**
     * Called just before the VM is executed, to enable and preparations needed in the environment.
     */
    override fun setup() {
        validateCommunicationConfiguration()
    }

    /**
     * Validates that the node has the required send actions for communication.
     * Warns the user if ProtelisDevice nodes are missing SendToNeighbor actions.
     */
    private fun validateCommunicationConfiguration() {
        val hasProtelisPrograms = allProtelisPrograms().isNotEmpty()
        if (hasProtelisPrograms) {
            val hasSendAction = node.reactions
                .asSequence()
                .flatMap { it.actions }
                .any { it is SendToNeighbor }

            if (!hasSendAction) {
                LOGGER.warn(
                    "Protelis node {} is missing a 'send' action. This node will not be able to " +
                        "communicate with neighboring nodes. Consider adding a reaction with 'send' action " +
                        "to enable communication.",
                    node.id,
                )
            }
        }
    }

    override fun toString(): String = "PtDevice${node.id}"

    private companion object {
        private const val serialVersionUID: Long = 1L
        private val LOGGER = LoggerFactory.getLogger(ProtelisDevice::class.java)
    }
}
