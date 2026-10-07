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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.conditions.NeighborHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observables.CompositeDisposable
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.reactions.AbstractNodeReaction
import it.unibo.alchemist.model.reactions.ConditionalEvent
import it.unibo.alchemist.model.reactions.GenericReaction
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

    override fun getNext(): Reaction<T>? = reactions.minByOrNull { it.nextOccurrence.current }

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
    var onExecute: () -> Unit = {}
    var executions = 0
    var disposed = false

    override fun dispose() {
        disposed = true
        super.dispose()
    }

    override fun performModelMutation() {
        executions++
        onExecute()
        if (emitOnExecute) {
            emit(DoubleTime(4.0), DoubleTime(5.0))
        }
    }

    fun emit(vararg times: Time) = times.forEach(::setNextOccurrence)

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): NodeReaction<Double> =
        error("Not needed in test")
}

private class CountingInvalidationReaction(
    node: Node<Double>,
    private val inputs: List<MutableObservable<Int>>,
    private val onInvalidation: () -> Unit = {},
    val distribution: CountingDistribution = CountingDistribution(),
) : AbstractNodeReaction<Double>(node, distribution) {
    var invalidations = 0
        private set

    override fun subscribeToSchedulingInputs(subscriptions: CompositeDisposable) {
        inputs.forEach { input ->
            subscriptions.add(
                input.subscribe(invokeOnSubscription = false) {
                    schedulingInputChanged()
                },
            )
        }
    }

    override fun scheduleAfterInvalidation(currentTime: Time) {
        invalidations++
        onInvalidation()
        super.scheduleAfterInvalidation(currentTime)
    }

    override fun cloneOnNewNode(node: Node<Double>, currentTime: Time): NodeReaction<Double> =
        error("Not needed in test")
}

private class CountingDistribution : TimeDistribution<Double> {
    var samples = 0
        private set

    override fun sample(): Time = DoubleTime((++samples).toDouble())

    override fun newInstanceOn(node: Node<Double>): TimeDistribution<Double> = CountingDistribution()
}

private class CountingAction(reaction: Reaction<Double>, private val executions: MutableList<Action<Double>>) :
    AbstractAction<Double>(reaction) {
    override fun cloneAction(newReaction: Reaction<Double>): Action<Double> = CountingAction(newReaction, executions)

    override fun execute() {
        executions += this
    }
}

private class InvalidCondition(reaction: Reaction<Double>) : AbstractCondition<Double>(reaction) {
    var readySignals = 0
        private set

    init {
        setValidity(observe(false))
    }

    override fun cloneCondition(newReaction: Reaction<Double>) = InvalidCondition(newReaction)

    override fun beforeReactionFires() {
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
        reaction.conditions = listOf(InvalidCondition(reaction))
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

    "each model mutation refreshes an affected reaction once" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val inputs = List(2) { observe(0, emitOnDistinct = false) }
        val source = EmittingNodeReaction(node).apply {
            onExecute = { inputs.forEach { it.current++ } }
        }
        val target = CountingInvalidationReaction(node, inputs)
        node.addReaction(source)
        node.addReaction(target)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        engine.schedule { inputs.forEach { it.current++ } }
        engine.drainCommand()
        target.invalidations shouldBe 1
        target.distribution.samples shouldBe 1
        scheduler.updates.count { it === target } shouldBe 1
        engine.stepForTest()
        target.invalidations shouldBe 2
        target.distribution.samples shouldBe 2
        scheduler.updates.count { it === target } shouldBe 2
    }

    "dirty reactions retain first-invalidation order" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val input = observe(0)
        val refreshOrder = mutableListOf<Int>()
        val reactions = List(8) { index ->
            CountingInvalidationReaction(node, listOf(input), { refreshOrder += index }).also(node::addReaction)
        }
        environment.addNode(node, environment.makePosition(0, 0))
        val engine = TestEngine(environment, RecordingScheduler())
        engine.initializeForTest()
        engine.schedule { input.current++ }
        engine.drainCommand()
        refreshOrder shouldBe reactions.indices.toList()
    }

    "topology changes gate a neighborhood-dependent reaction exactly when its validity changes" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        environment.linkingRule = ConnectWithinDistance(1.0)
        val molecule = Biomolecule("M")
        val center = GenericNode(environment)
        val peer = GenericNode(environment).apply { setConcentration(molecule, 1.0) }
        val distribution = CountingDistribution()
        val reaction = GenericReaction(center, distribution).apply {
            conditions = listOf(NeighborHasConcentration(this, environment, molecule, 1.0))
        }
        center.addReaction(reaction)
        environment.addNode(center, environment.makePosition(0, 0))
        environment.addNode(peer, environment.makePosition(5, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        fun mutate(mutation: () -> Unit) {
            engine.schedule(mutation)
            engine.drainCommand()
        }
        fun assertScheduling(finite: Boolean, samples: Int, updates: Int) {
            reaction.nextOccurrence.current.isFinite shouldBe finite
            distribution.samples shouldBe samples
            scheduler.updates.count { it === reaction } shouldBe updates
        }
        assertScheduling(finite = false, samples = 0, updates = 0)
        // A neighbor with the molecule moves into range.
        mutate { environment.moveNodeTo(peer, environment.makePosition(0.5, 0)) }
        assertScheduling(finite = true, samples = 1, updates = 1)
        // Movement that keeps the neighborhood valid neither redraws nor reindexes.
        mutate { environment.moveNodeTo(peer, environment.makePosition(0.6, 0)) }
        assertScheduling(finite = true, samples = 1, updates = 1)
        // The neighbor leaves and comes back.
        mutate { environment.moveNodeTo(peer, environment.makePosition(5, 0)) }
        assertScheduling(finite = false, samples = 1, updates = 2)
        mutate { environment.moveNodeTo(peer, environment.makePosition(0.5, 0)) }
        assertScheduling(finite = true, samples = 2, updates = 3)
        // The neighbor's concentration changes while topology stays the same.
        mutate { peer.setConcentration(molecule, 2.0) }
        assertScheduling(finite = false, samples = 2, updates = 4)
        mutate { peer.setConcentration(molecule, 1.0) }
        assertScheduling(finite = true, samples = 3, updates = 5)
        // Node removal and addition.
        mutate { environment.removeNode(peer) }
        assertScheduling(finite = false, samples = 3, updates = 6)
        val newcomer = GenericNode(environment).apply { setConcentration(molecule, 1.0) }
        mutate { environment.addNode(newcomer, environment.makePosition(0, 0.5)) }
        assertScheduling(finite = true, samples = 4, updates = 7)
        // An environment-wide topology change.
        mutate { environment.linkingRule = ConnectWithinDistance(0.1) }
        assertScheduling(finite = false, samples = 4, updates = 8)
    }

    "a reaction removed during a model mutation is not refreshed" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val input = observe(0)
        val target = CountingInvalidationReaction(node, listOf(input))
        val source = EmittingNodeReaction(node).apply {
            onExecute = {
                input.current++
                node.removeReaction(target)
            }
        }
        node.addReaction(source)
        node.addReaction(target)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        engine.stepForTest()
        target.invalidations shouldBe 0
        engine.drainCommand()
        scheduler.reactions shouldNotContain target
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
        node.reactions.current shouldNotContain event
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
        environment.reactions.current shouldNotContain event
        event.nextOccurrence.observers.size shouldBe 0
    }

    "events of both hosts execute once and leave no scheduling state or clonable trace" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val executions = mutableListOf<Action<Double>>()
        val distribution = CountingDistribution()
        val absolute = AbsoluteEvent<Double>(node, DoubleTime(0.5)).apply {
            actions = listOf(CountingAction(this, executions))
        }
        val conditional = ConditionalEvent(node, distribution).apply {
            actions = listOf(CountingAction(this, executions))
        }
        val environmentEvent = AbsoluteEvent<Double>(environment, DoubleTime(3.0)).apply {
            actions = listOf(CountingAction(this, executions))
        }
        node.addReaction(absolute)
        node.addReaction(conditional)
        environment.addReaction(environmentEvent)
        environment.addNode(node, environment.makePosition(0, 0))
        val events = listOf(absolute, conditional, environmentEvent)
        val clonedBeforeFiring = node.cloneNode(Time.ZERO).reactions.current
        clonedBeforeFiring.size shouldBe 1
        clonedBeforeFiring.single().shouldBeInstanceOf<ConditionalEvent<Double>>()
        clonedBeforeFiring.single() shouldNotBe conditional
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        repeat(events.size) {
            engine.stepForTest()
            engine.drainCommand()
        }
        executions.size shouldBe events.size
        engine.step shouldBe events.size.toLong()
        distribution.samples shouldBe 1
        events.forEach { event ->
            scheduler.reactions shouldNotContain event
            scheduler.updates shouldNotContain event
            event.nextOccurrence.observers.size shouldBe 0
        }
        node.reactions.current.shouldBeEmpty()
        environment.reactions.current.shouldBeEmpty()
        node.cloneNode(DoubleTime(3.0)).reactions.current.shouldBeEmpty()
        engine.stepForTest()
        executions.size shouldBe events.size
        engine.step shouldBe events.size.toLong()
    }

    "an absolute event expires at its occurrence when its conditions are invalid" {
        val environment = Continuous2DEnvironment(BiochemistryIncarnation())
        val node = GenericNode(environment)
        val event = AbsoluteEvent<Double>(node, Time.ZERO)
        val condition = InvalidCondition(event)
        event.conditions = listOf(condition)
        node.addReaction(event)
        environment.addNode(node, environment.makePosition(0, 0))
        val scheduler = RecordingScheduler<Double>()
        val engine = TestEngine(environment, scheduler)
        engine.initializeForTest()
        event.nextOccurrence.current shouldBe Time.ZERO
        event.canExecute.current shouldBe true
        engine.stepForTest()
        engine.drainCommand()
        engine.step shouldBe 1L
        condition.readySignals shouldBe 0
        scheduler.reactions shouldNotContain event
        node.reactions.current shouldNotContain event
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
