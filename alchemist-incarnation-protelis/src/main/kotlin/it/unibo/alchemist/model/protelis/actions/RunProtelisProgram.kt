/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.protelis.AlchemistExecutionContext
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice
import it.unibo.alchemist.util.RealDistributions
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.protelis.lang.ProtelisLoader
import org.protelis.vm.ProtelisProgram
import org.protelis.vm.ProtelisVM

/**
 * An [it.unibo.alchemist.model.Action] that executes a Protelis program.
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
 * It is possible to simulate the loss of messages due to a higher connection distance by providing a [RealDistribution]
 * ([packetLossDistance]) mapping distances to the loss probability. By default, this feature is disabled.
 */
class RunProtelisProgram<P : Position<P>> private constructor(
    val randomGenerator: RandomGenerator,
    val environment: Environment<Any, P>,
    val device: ProtelisDevice<P>,
    reaction: NodeReaction<Any>,
    val originalProgram: String,
    val program: ProtelisProgram,
    val retentionTime: Double,
    val packetLossDistance: RealDistribution?,
) : AbstractLocalAction<Any>(reaction) {
    @JvmOverloads
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: NodeReaction<Any>,
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
        packetLossDistance = null,
    )

    @JvmOverloads
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: NodeReaction<Any>,
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
        packetLossDistance =
        RealDistributions.makeRealDistribution(
            randomGenerator,
            packetLossDistributionName,
            *packetLossDistributionParameters,
        ),
    )

    @JvmOverloads
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: NodeReaction<Any>,
        program: String,
        retentionTime: Double = Double.NaN,
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

    @JvmOverloads
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        device: ProtelisDevice<P>,
        reaction: NodeReaction<Any>,
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
        packetLossDistance =
        RealDistributions.makeRealDistribution(
            randomGenerator,
            packetLossDistributionName,
            *packetLossDistributionParameters,
        ),
    )

    /**
     * An observable that emits updates indicating whether the computational cycle of a Protelis program
     * has been completed.
     */
    val computationalCycleIsComplete: Observable<Boolean>
        field: MutableObservable<Boolean> = observe(false)

    private val name: Molecule =
        targetNode.reactions.current
            .asSequence()
            .flatMap { it.actions.asSequence() }
            .filterIsInstance<RunProtelisProgram<*>>()
            .map { it.program.name }
            .count { it == program.name }
            .let { otherCopies -> SimpleMolecule(program.name + if (otherCopies == 0) "" else "\$copy$otherCopies") }

    /**
     * Provides an access to the underlying [org.protelis.vm.ExecutionContext].
     *
     * @return the current [AlchemistExecutionContext]
     */
    val executionContext = device.executionContextOf(this)

    private val vm: ProtelisVM = ProtelisVM(program, executionContext)

    /**
     * @return the molecule associated with the execution of this program
     */
    fun asMolecule(): Molecule = name

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Any>): RunProtelisProgram<P> = RunProtelisProgram(
        randomGenerator,
        environment,
        newReaction.host.asProperty(),
        newReaction,
        originalProgram = originalProgram,
        program = program,
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
        targetNode.setConcentration(name, vm.currentValue)
        computationalCycleIsComplete.update { true }
    }

    override fun hashCode() = name.hashCode()

    /**
     * Marks the computational cycle as incomplete before the next run.
     */
    fun prepareForComputationalCycle() {
        computationalCycleIsComplete.update { false }
    }

    override fun toString(): String = name.toString() + "@" + targetNode.id
}
