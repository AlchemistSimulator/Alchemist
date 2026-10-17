/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis.conditions

import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.conditions.AbstractLocalCondition
import it.unibo.alchemist.model.protelis.actions.RunProtelisProgram
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice

/**
 * Valid once [program] completes a computational round, until the round is consumed by sending its messages.
 */
class ComputationalRoundComplete(reaction: NodeReaction<Any>, val program: RunProtelisProgram<*>) :
    AbstractLocalCondition<Any>(reaction) {
    init {
        setValidity(program.computationalCycleIsComplete)
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Any>): ComputationalRoundComplete {
        val device = checkNotNull(newReaction.host.asPropertyOrNull<Any, ProtelisDevice<*>>()) {
            "${this::class.simpleName} cannot get cloned on a node with a missing ${ProtelisDevice::class.simpleName}"
        }
        val programs = device.allProtelisPrograms()
        check(programs.size == 1) {
            "There must be one and one only unconfigured ${RunProtelisProgram::class.simpleName}"
        }
        return ComputationalRoundComplete(newReaction, programs.single())
    }

    override fun toString(): String = "${program.asMolecule().name} completed round"
}
