/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis.actions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice

/**
 * Action that broadcasts the content of a Protelis program to neighbours.
 *
 * @param reaction the reaction triggering this action
 * @property protelisProgram the [RunProtelisProgram] whose data will be sent
 */
class SendToNeighbor(reaction: NodeReaction<Any>, val protelisProgram: RunProtelisProgram<*>) :
    AbstractLocalAction<Any>(reaction) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<Any>): SendToNeighbor {
        val device: ProtelisDevice<*> = newReaction.host.asProperty()
        val possibleRefs: List<RunProtelisProgram<*>> = device.allProtelisPrograms()
        check(possibleRefs.size == 1) {
            "There must be one and one only unconfigured " + RunProtelisProgram::class.simpleName
        }
        return SendToNeighbor(newReaction, possibleRefs[0])
    }

    override fun execute() {
        val protelisDevice = targetNode.asProperty<ProtelisDevice<*>>(ProtelisDevice::class.java)
        val mgr = protelisDevice.getNetworkManager(this.protelisProgram)
        mgr.simulateMessageArrival(reaction.nextOccurrence.current.toDouble())
        protelisProgram.prepareForComputationalCycle()
    }

    override fun toString(): String = "broadcast " + protelisProgram.asMolecule().getName() + " data"
}
