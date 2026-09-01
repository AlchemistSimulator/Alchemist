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
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.reactions.AbstractNodeReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private class RecordingScheduler<T> : Scheduler<T> {
    val reactions = mutableListOf<Reaction<T>>()
    val updates = mutableListOf<Reaction<T>>()
    var updateBeforeAdd = false
    var throwOnAdd = false

    override fun addReaction(reaction: Reaction<T>) {
        reactions += reaction
        if (throwOnAdd) {
            reactions.remove(reaction)
            error("synthetic scheduler failure")
        }
    }

    override fun getNext(): Reaction<T>? = reactions.firstOrNull()

    override fun removeReaction(reaction: Reaction<T>) {
        reaction.nextOccurrence.observers.size shouldBe 0
        reactions.remove(reaction)
    }

    override fun updateReaction(reaction: Reaction<T>) {
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
    var executions = 0
    var disposed = false

    override fun dispose() {
        disposed = true
        super.dispose()
    }

    override fun executeReaction() {
        executions++
        if (emitOnExecute) {
            emit(DoubleTime(4.0), DoubleTime(5.0))
        }
    }

    fun emit(vararg times: Time) = times.forEach(::setNextOccurrence)

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): NodeReaction<Double> =
        error("Not needed in test")
}

private class InvalidCondition(node: Node<Double>) : AbstractCondition<Double>(node) {
    var readySignals = 0
        private set

    init {
        setValidity(MutableObservable.observe(false))
    }

    override fun reactionReady() {
        readySignals++
    }
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

    "an infinite scheduler head is quiescent and is not consumed as a step" {
        val (environment, _, reaction) = fixture()
        reaction.conditions = listOf(InvalidCondition(reaction.node))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        reaction.nextOccurrence.current shouldBe Time.INFINITY
        engine.stepForTest()

        engine.time shouldBe Time.ZERO
        engine.step shouldBe 0L
        reaction.executions shouldBe 0
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

    "a successful event is unregistered and removed from its node without an infinite update" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val event = AbsoluteEvent<Double>(node, Time.ZERO)
        node.addReaction(event)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        engine.stepForTest()
        engine.drainCommand()

        scheduler.reactions shouldNotContain event
        scheduler.updates shouldNotContain event
        node.reactions shouldNotContain event
        event.nextOccurrence.observers.size shouldBe 0
    }

    "a successful environment-hosted event uses the same removal path" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val event = AbsoluteEvent<Double>(environment, Time.ZERO)
        environment.addReaction(event)
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        engine.stepForTest()
        engine.drainCommand()

        scheduler.reactions shouldNotContain event
        scheduler.updates shouldNotContain event
        environment.reactions shouldNotContain event
        event.nextOccurrence.observers.size shouldBe 0
    }

    "an absolute event expires at its occurrence when its conditions are invalid" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val condition = InvalidCondition(node)
        val event = AbsoluteEvent<Double>(node, Time.ZERO).apply {
            conditions = listOf(condition)
        }
        node.addReaction(event)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()

        event.nextOccurrence.current shouldBe Time.ZERO
        event.canExecute().current shouldBe true
        engine.stepForTest()
        engine.drainCommand()

        engine.step shouldBe 1L
        condition.readySignals shouldBe 0
        scheduler.reactions shouldNotContain event
        node.reactions shouldNotContain event
    }

    "runtime host mutations synchronize scheduler membership for both host types" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        val nodeReaction = EmittingNodeReaction(node)
        val environmentReaction = AbsoluteEvent<Double>(environment, DoubleTime(2.0))

        node.addReaction(nodeReaction)
        engine.drainCommand()
        environment.addReaction(environmentReaction)
        engine.drainCommand()
        scheduler.reactions shouldContain nodeReaction
        scheduler.reactions shouldContain environmentReaction

        node.removeReaction(nodeReaction)
        engine.drainCommand()
        environment.removeReaction(environmentReaction)
        engine.drainCommand()
        scheduler.reactions shouldNotContain nodeReaction
        scheduler.reactions shouldNotContain environmentReaction
    }

    "runtime node membership synchronizes each hosted reaction" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        val node = GenericNode(environment)
        val reaction = EmittingNodeReaction(node)
        node.addReaction(reaction)

        environment.addNode(node, environment.makePosition(0, 0))
        engine.drainCommand()
        scheduler.reactions shouldContain reaction

        environment.removeNode(node)
        engine.drainCommand()
        scheduler.reactions shouldNotContain reaction
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
