/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime

private class RecordingScheduler<T> : Scheduler<T> {
    val reactions = mutableListOf<Actionable<T>>()
    val updates = mutableListOf<Actionable<T>>()
    var updateBeforeAdd = false

    override fun addReaction(reaction: Actionable<T>) {
        reactions += reaction
    }

    override fun getNext(): Actionable<T>? = reactions.firstOrNull()

    override fun removeReaction(reaction: Actionable<T>) {
        reaction.nextOccurrence.observers.size shouldBe 0
        reactions.remove(reaction)
    }

    override fun updateReaction(reaction: Actionable<T>) {
        if (reaction !in reactions) {
            updateBeforeAdd = true
        }
        updates += reaction
    }
}

private class EmittingReaction(
    node: Node<Double>,
    distribution: TimeDistribution<Double> = DiracComb(1.0),
) : AbstractReaction<Double>(node, distribution) {
    var emitOnExecute = false

    override fun execute() {
        if (emitOnExecute) {
            emit(DoubleTime(4.0), DoubleTime(5.0))
        }
    }

    fun emit(vararg times: Time) = times.forEach(::setNextOccurrence)

    override fun updateInternalStatus(
        currentTime: Time,
        hasBeenExecuted: Boolean,
        environment: Environment<Double, *>,
    ) = Unit

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): Reaction<Double> = error("Not needed in test")
}

private class TestEngine<T, P : it.unibo.alchemist.model.Position<out P>>(
    environment: Environment<T, P>,
    scheduler: Scheduler<T>,
) : Engine<T, P>(environment, scheduler) {
    fun initializeForTest() = initialize()

    fun stepForTest() = doStep()

    fun drainCommand() = processCommand(commands.poll())
}

class EngineSchedulingSubscriptionTest : FreeSpec({
    fun fixture(): Triple<Continuous2DEnvironment<Double>, GenericNode<Double>, EmittingReaction> {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val reaction = EmittingReaction(node)
        node.addReaction(reaction)
        environment.addNode(node, environment.makePosition(0, 0))
        return Triple(environment, node, reaction)
    }

    "initial registration does not update before scheduler add" {
        val (environment, _, reaction) = fixture()
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        scheduler.updateBeforeAdd shouldBe false
        scheduler.updates shouldNotContain reaction
    }

    "each next occurrence emission updates the active scheduler entry" {
        val (environment, _, reaction) = fixture()
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        reaction.emit(DoubleTime(1.0), DoubleTime(2.0))

        scheduler.updates.count { it === reaction } shouldBe 2

        reaction.emitOnExecute = true
        engine.stepForTest()
        scheduler.updates.count { it === reaction } shouldBe 5
    }

    "removal disposes the subscription before scheduler removal" {
        val (environment, _, reaction) = fixture()
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        engine.reactionRemoved(reaction)
        engine.drainCommand()
        val updatesBeforeEmission = scheduler.updates.count { it === reaction }
        reaction.emit(DoubleTime(4.0))

        scheduler.reactions shouldNotContain reaction
        scheduler.updates.count { it === reaction } shouldBe updatesBeforeEmission
    }
})
