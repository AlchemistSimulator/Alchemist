/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis.actions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice
import java.io.Serial
import java.util.Objects

/**
 * @param node
 * the local node
 * @param reaction
 * the reaction
 * @param protelisProgram the [RunProtelisProgram] whose data will be sent
 */
class SendToNeighbor(
    node: Node<Any>,
    reaction: Reaction<Any>,
    val protelisProgram: RunProtelisProgram<*>,
) : AbstractAction<Any>(node) {
    private val reaction: Reaction<Any> = Objects.requireNonNull<Reaction<Any>>(reaction)

    init {
        declareDependencyTo(protelisProgram.asMolecule())
    }

    override fun cloneAction(
        newNode: Node<Any>,
        newReaction: Reaction<Any>,
    ): SendToNeighbor {
        val device: ProtelisDevice<*> = newNode.asProperty()
        val possibleRefs: List<RunProtelisProgram<*>> = device.allProtelisPrograms()
        check(possibleRefs.size == 1) {
            "There must be one and one only unconfigured " + RunProtelisProgram::class.simpleName
        }
        return SendToNeighbor(newNode, this.reaction, possibleRefs[0])
    }

    override fun getContext(): Context = Context.NEIGHBORHOOD

    override fun execute() {
        val protelisDevice = node.asProperty<ProtelisDevice<*>>(ProtelisDevice::class.java)
        val mgr = protelisDevice.getNetworkManager(this.protelisProgram)
        mgr.simulateMessageArrival(reaction.tau.toDouble())
        protelisProgram.prepareForComputationalCycle()
    }

    override fun toString(): String = "broadcast " + protelisProgram.asMolecule().getName() + " data"

    private companion object {
        @Serial
        private val serialVersionUID = -8826563176323247613L
    }
}
