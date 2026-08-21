/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.AbstractNodeReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private class RecordingScheduler<T> : Scheduler<T> {
    val reactions = mutableListOf<Actionable<T>>()
    val updates = mutableListOf<Actionable<T>>()
    var updateBeforeAdd = false
    var throwOnAdd = false

    override fun addReaction(reaction: Actionable<T>) {
        reactions += reaction
        if (throwOnAdd) {
            reactions.remove(reaction)
            error("synthetic scheduler failure")
        }
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

private class EmittingNodeReaction(
    node: Node<Double>,
    distribution: TimeDistribution<Double> = DiracComb(1.0),
) : AbstractNodeReaction<Double>(node, distribution) {
    var emitOnExecute = false
    var disposed = false

    override fun dispose() {
        disposed = true
        super.dispose()
    }

    override fun execute() {
        if (emitOnExecute) {
            emit(DoubleTime(4.0), DoubleTime(5.0))
        }
    }

    fun emit(vararg times: Time) = times.forEach(::setNextOccurrence)

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): NodeReaction<Double> =
        error("Not needed in test")
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
    fun fixture(): Triple<Continuous2DEnvironment<Double>, GenericNode<Double>, EmittingNodeReaction> {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val reaction = EmittingNodeReaction(node)
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
        reaction.disposed shouldBe true
    }

    "duplicate registration is rejected" {
        val (environment, _, _) = fixture()
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        shouldThrow<IllegalStateException> { engine.initializeForTest() }
    }

    "failed scheduler registration propagates without an engine subscription" {
        val (environment, _, reaction) = fixture()
        val scheduler = RecordingScheduler<Double>().also {
            it.throwOnAdd = true
        }
        val engine = TestEngine(environment, scheduler)

        shouldThrow<IllegalStateException> { engine.initializeForTest() }
        reaction.nextOccurrence.observers.size shouldBe 0
        scheduler.reactions shouldNotContain reaction
        reaction.disposed shouldBe false
    }

    "running-engine callbacks are confined to the simulation thread" {
        val (environment, _, reaction) = fixture()
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        val worker = Thread(engine::run)
        worker.start()
        try {
            val deadline = System.nanoTime() + 5_000_000_000L
            while (engine.status != Status.READY && System.nanoTime() < deadline) {
                delay(10.milliseconds)
            }
            engine.status shouldBe Status.READY
            scheduler.reactions shouldContain reaction
            val updatesBeforeEmission = scheduler.updates.size
            shouldThrow<IllegalStateException> { reaction.emit(DoubleTime(3.0)) }
            scheduler.updates.size shouldBe updatesBeforeEmission
        } finally {
            engine.terminate()
            worker.join(5_000)
            worker.isAlive shouldBe false
        }
    }
})
