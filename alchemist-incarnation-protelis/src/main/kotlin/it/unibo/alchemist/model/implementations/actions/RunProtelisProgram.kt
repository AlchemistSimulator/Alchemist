/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.properties.ProtelisDevice
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Dependency
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.math.RealDistributionUtil
import it.unibo.alchemist.protelis.AlchemistExecutionContext
import it.unibo.alchemist.protelis.AlchemistNetworkManager
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.util.ImmutableListSet
import org.protelis.lang.ProtelisLoader
import org.protelis.vm.ProtelisProgram
import org.protelis.vm.ProtelisVM
import java.io.ObjectInputStream

/**
 * An [Action] that executes a Protelis program.
 *
 * Requires the current [randomGenerator] and [environment], a valid [ProtelisDevice] ([device]),
 * and the local [reaction] hosting the computation.
 *
 * The program can be created using a String ([originalProgram]), or, alternatively,
 * by providing a [ProtelisProgram] ([program]).
 *
 * [retentionTime] specifies whether, upon message usage, the received messages should be deleted
 * (assuming a reasonable synchronization among devices) or if they should remain in memory for a specified amount
 * of time. By default, [retentionTime] is [Double.NaN], indicating that messages are deleted upon read.
 *
 * It is possible to sumulate the loss of messages due to a higher connection distance by providing a [RealDistribution]
 * ([packetLossDistance]) mapping distances to the loss probability. By default this feature is disabled.
 */
class RunProtelisProgram<P : Position<P>> private constructor(
    val randomGenerator: RandomGenerator,
    val environment: Environment<Any, P>,
    val device: ProtelisDevice<P>,
    val reaction: Reaction<Any>,
    val originalProgram: String,
    val program: ProtelisProgram,
    val retentionTime: Double,
    val packetLossDistance: RealDistribution?,
) : Action<Any> {

    @JvmOverloads constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: Reaction<Any>,
        program: ProtelisProgram,
        retentionTime: Double = Double.NaN,
    ) : this(
        randomGenerator,
        environment,
        device,
        reaction,
        originalProgram = program.name,
        program = program,
        retentionTime = retentionTime,
        packetLossDistance = null
    )

    @JvmOverloads constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: Reaction<Any>,
        program: ProtelisProgram,
        retentionTime: Double = Double.NaN,
        packetLossDistributionName: String,
        vararg packetLossDistributionParameters: Double,
    ) : this(
        randomGenerator,
        environment,
        device,
        reaction,
        originalProgram = program.name,
        program = program,
        retentionTime = retentionTime,
        packetLossDistance = RealDistributionUtil.makeRealDistribution(
            randomGenerator,
            packetLossDistributionName,
            *packetLossDistributionParameters
        )
    )

    @JvmOverloads constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: Reaction<Any>,
        program: String,
        retentionTime: Double = Double.NaN
    ) : this(
        randomGenerator,
        environment,
        device,
        reaction,
        originalProgram = program,
        program = ProtelisLoader.parse(program),
        retentionTime = retentionTime,
        packetLossDistance = null,
    )

    @JvmOverloads constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: Reaction<Any>,
        program: String,
        retentionTime: Double = Double.NaN,
        packetLossDistributionName: String,
        vararg packetLossDistributionParameters: Double,
    ) : this(
        randomGenerator,
        environment,
        device,
        reaction,
        originalProgram = program,
        retentionTime = retentionTime,
        program = ProtelisLoader.parse(program),
        packetLossDistance = RealDistributionUtil.makeRealDistribution(
            randomGenerator,
            packetLossDistributionName,
            *packetLossDistributionParameters
        )
    )

    /**
     * The Alchemist [Node] hosting the [ProtelisDevice].
     */
    val node = device.node

    /**
     * @return true if the Program has finished its last computation,
     * and is ready to send a new message (used for dependency management)
     */
    var isComputationalCycleComplete = false
        private set

    private val name: Molecule = node.reactions.asSequence()
        .flatMap { it.actions.asSequence() }
        .filterIsInstance<RunProtelisProgram<*>>()
        .map { it.program.name }
        .count { it == program.name }
        .let { otherCopies -> SimpleMolecule(program.name + if (otherCopies == 0) "" else "\$copy$otherCopies") }

    private val networkManager = AlchemistNetworkManager(reaction, device, this, retentionTime, packetLossDistance)

    /**
     * Provides an access to the underlying [org.protelis.vm.ExecutionContext].
     *
     * @return the current [AlchemistExecutionContext]
     */
    @Transient
    var executionContext = AlchemistExecutionContext(environment, node, reaction, randomGenerator, networkManager)
        private set

    @Transient
    private var vm: ProtelisVM = ProtelisVM(program, executionContext)

    init {
        device.addNetworkManger(this, networkManager)
    }

    /**
     * @return the molecule associated with the execution of this program
     */
    fun asMolecule(): Molecule {
        return name
    }

    override fun cloneAction(node: Node<Any>, reaction: Reaction<Any>): RunProtelisProgram<P> = RunProtelisProgram(
        randomGenerator,
        environment,
        node.asProperty(),
        reaction,
        originalProgram = originalProgram,
        program = program, // TODO: this is broken until https://github.com/Protelis/Protelis/pull/676 gets merged
        retentionTime = retentionTime,
        packetLossDistance = packetLossDistance,
    )

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other != null && other.javaClass == javaClass) {
            val otherProgram = other as RunProtelisProgram<*>
            return name == otherProgram.name
        }
        return false
    }

    override fun execute() {
        vm.runCycle()
        node.setConcentration(name, vm.currentValue)
        isComputationalCycleComplete = true
    }

    /*
     * A Protelis program never writes in other nodes
     */
    override fun getContext() = Context.LOCAL

    override fun getOutboundDependencies(): ImmutableListSet<Dependency> =
        ImmutableListSet.of(Dependency.EVERY_MOLECULE)

    override fun hashCode() = name.hashCode()

    /**
     * Resets the computation status (used for dependency management).
     */
    fun prepareForComputationalCycle() {
        isComputationalCycleComplete = false
    }

    @Suppress("UnusedPrivateMember")
    private fun readObject(stream: ObjectInputStream) {
        stream.defaultReadObject()
        executionContext = AlchemistExecutionContext(environment, node, reaction, randomGenerator, networkManager)
        vm = ProtelisVM(program, executionContext)
    }

    override fun toString(): String {
        return name.toString() + "@" + node.id
    }

    companion object {
        private const val serialVersionUID = 2L
    }
}
