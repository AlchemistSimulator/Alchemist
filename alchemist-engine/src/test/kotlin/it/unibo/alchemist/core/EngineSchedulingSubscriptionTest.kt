/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.ReactionHost
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.conditions.NeighborHasConcentration
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.reactions.AbsoluteEvent
import it.unibo.alchemist.model.reactions.ConditionalEvent
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.times.DoubleTime
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

private class TestEngine<T, P : Position<out P>>(environment: Environment<T, P>, scheduler: Scheduler<T>) :
    Engine<T, P>(environment, scheduler) {
    fun initializeForTest() = initialize()

    fun stepForTest() = doStep()

    fun drainCommands() {
        while (commands.isNotEmpty()) {
            processCommand(commands.poll())
        }
    }
}

class EngineSchedulingSubscriptionTest {
    private val environment = Continuous2DEnvironment(BiochemistryIncarnation())
    private val node = GenericNode(environment)

    // The real scheduler, spied to verify calls; every removal must follow the disposal of the engine subscription.
    private val scheduler = spyk(ArrayIndexedPriorityQueue<Double>()).apply {
        every { removeReaction(any()) } answers {
            assertEquals(0, firstArg<Reaction<Double>>().nextOccurrence.observers.size)
            callOriginal()
        }
    }
    private val engine = TestEngine(environment, scheduler)

    private fun Node<Double>.place(x: Number = 0, y: Number = 0) {
        environment.addNode(this, environment.makePosition(x, y))
    }

    private fun Reaction<Double>.isScheduled() = this in scheduler.tree

    /** A reaction whose scheduling is driven by the test through [emit]. */
    private fun mockReaction(
        hostedBy: ReactionHost<Double> = node,
        firstOccurrence: Time = DoubleTime(1.0),
        executable: Boolean = true,
    ): Reaction<Double> {
        val occurrence = observe(firstOccurrence)
        val eligibility = observe(executable)
        return mockk(relaxed = true) {
            every { host } returns hostedBy
            every { nextOccurrence } returns occurrence
            every { canExecute } returns eligibility
        }
    }

    private fun Reaction<Double>.emit(vararg times: Time) {
        // mockReaction backs the next occurrence with a mutable observable.
        val occurrence = nextOccurrence as MutableObservable<Time>
        times.forEach { occurrence.current = it }
    }

    /** Reports [inputs] changes to the engine, as reactions do for their scheduling inputs. */
    private fun Reaction<Double>.invalidatedBy(vararg inputs: Observable<*>) = inputs.forEach { input ->
        input.subscribe(invokeOnSubscription = false) { engine.reactionInvalidated(this) }
    }

    /** A time distribution sampling 1, 2, 3, ... */
    private fun countingDistribution(): TimeDistribution<Double> {
        var samples = 0
        return mockk(relaxed = true) {
            every { sample() } answers { DoubleTime((++samples).toDouble()) }
        }
    }

    private fun mockAction(owner: Reaction<Double>): Action<Double> = mockk(relaxed = true) {
        every { reaction } returns owner
        every { cloneAction(any()) } answers { mockAction(firstArg()) }
    }

    @Test
    fun `initial registration does not update before scheduler add`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        engine.initializeForTest()
        verify(exactly = 1) { scheduler.addReaction(reaction) }
        verify(exactly = 0) { scheduler.updateReaction(reaction) }
    }

    @Test
    fun `an infinite scheduler head is quiescent and is not consumed as a step`() {
        val reaction = mockReaction(firstOccurrence = Time.INFINITY, executable = false).also(node::addReaction)
        node.place()
        engine.initializeForTest()
        engine.stepForTest()
        assertEquals(Time.ZERO, engine.time)
        assertEquals(0L, engine.step)
        verify(exactly = 0) { reaction.execute() }
        verify(exactly = 0) { scheduler.updateReaction(reaction) }
    }

    @Test
    fun `each next occurrence emission updates the active scheduler entry`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        engine.initializeForTest()
        reaction.emit(DoubleTime(1.5), DoubleTime(2.0))
        verify(exactly = 2) { scheduler.updateReaction(reaction) }
        every { reaction.execute() } answers { reaction.emit(DoubleTime(4.0), DoubleTime(5.0)) }
        engine.stepForTest()
        verify(exactly = 4) { scheduler.updateReaction(reaction) }
    }

    @Test
    fun `each model mutation refreshes an affected reaction once`() {
        val inputs = List(2) { observe(0, emitOnDistinct = false) }
        val source = mockReaction(firstOccurrence = DoubleTime(0.5)).also(node::addReaction)
        every { source.execute() } answers { inputs.forEach { input -> input.current++ } }
        val target = mockReaction().also(node::addReaction)
        node.place()
        engine.initializeForTest()
        target.invalidatedBy(*inputs.toTypedArray())
        engine.schedule { inputs.forEach { it.current++ } }
        engine.drainCommands()
        verify(exactly = 1) { target.updateSchedulingAfterInvalidation(any()) }
        engine.stepForTest()
        verify(exactly = 2) { target.updateSchedulingAfterInvalidation(any()) }
    }

    @Test
    fun `dirty reactions retain first-invalidation order`() {
        val input = observe(0)
        val refreshOrder = mutableListOf<Int>()
        val reactions = List(8) { index ->
            mockReaction().also { reaction ->
                every { reaction.updateSchedulingAfterInvalidation(any()) } answers { refreshOrder += index }
                node.addReaction(reaction)
            }
        }
        node.place()
        engine.initializeForTest()
        reactions.forEach { it.invalidatedBy(input) }
        engine.schedule { input.current++ }
        engine.drainCommands()
        assertEquals(reactions.indices.toList(), refreshOrder)
    }

    @Test
    fun `topology changes gate a neighborhood-dependent reaction exactly when its validity changes`() {
        environment.linkingRule = ConnectWithinDistance(1.0)
        val molecule = Biomolecule("M")
        val peer = GenericNode(environment).apply { setConcentration(molecule, 1.0) }
        val distribution = countingDistribution()
        val reaction = GenericReaction(node, distribution).apply {
            conditions = listOf(NeighborHasConcentration(this, environment, molecule, 1.0))
        }
        node.addReaction(reaction)
        node.place()
        peer.place(5, 0)
        engine.initializeForTest()
        fun mutate(mutation: () -> Unit) {
            engine.schedule(mutation)
            engine.drainCommands()
        }
        fun assertScheduling(finite: Boolean, samples: Int, updates: Int) {
            assertEquals(finite, reaction.nextOccurrence.current.isFinite)
            verify(exactly = samples) { distribution.sample() }
            verify(exactly = updates) { scheduler.updateReaction(reaction) }
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
        mutate { newcomer.place(0, 0.5) }
        assertScheduling(finite = true, samples = 4, updates = 7)
        // An environment-wide topology change.
        mutate { environment.linkingRule = ConnectWithinDistance(0.1) }
        assertScheduling(finite = false, samples = 4, updates = 8)
    }

    @Test
    fun `a reaction removed during a model mutation is not refreshed`() {
        val input = observe(0)
        val target = mockReaction().also(node::addReaction)
        val source = mockReaction(firstOccurrence = DoubleTime(0.5)).also(node::addReaction)
        every { source.execute() } answers {
            input.current++
            node.removeReaction(target)
        }
        node.place()
        engine.initializeForTest()
        target.invalidatedBy(input)
        engine.stepForTest()
        verify(exactly = 0) { target.updateSchedulingAfterInvalidation(any()) }
        engine.drainCommands()
        assertFalse(target.isScheduled())
    }

    @Test
    fun `removal disposes the subscription before scheduler removal`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        engine.initializeForTest()
        engine.reactionRemoved(reaction)
        engine.drainCommands()
        reaction.emit(DoubleTime(4.0))
        assertFalse(reaction.isScheduled())
        verify(exactly = 1) { scheduler.removeReaction(reaction) }
        verify(exactly = 0) { scheduler.updateReaction(reaction) }
        verify { reaction.dispose() }
    }

    @Test
    fun `a successful event is unregistered and removed from its node without an infinite update`() {
        val event = AbsoluteEvent<Double>(node, Time.ZERO)
        node.addReaction(event)
        node.place()
        engine.initializeForTest()
        engine.stepForTest()
        engine.drainCommands()
        assertFalse(event.isScheduled())
        verify(exactly = 0) { scheduler.updateReaction(event) }
        assertFalse(event in node.reactions.current)
        assertEquals(0, event.nextOccurrence.observers.size)
    }

    @Test
    fun `a successful environment-hosted event uses the same removal path`() {
        val event = AbsoluteEvent<Double>(environment, Time.ZERO)
        environment.addReaction(event)
        engine.initializeForTest()
        engine.stepForTest()
        engine.drainCommands()
        assertFalse(event.isScheduled())
        verify(exactly = 0) { scheduler.updateReaction(event) }
        assertFalse(event in environment.reactions.current)
        assertEquals(0, event.nextOccurrence.observers.size)
    }

    @Test
    fun `events of both hosts execute once and leave no scheduling state or clonable trace`() {
        val distribution = countingDistribution()
        val absolute = AbsoluteEvent<Double>(node, DoubleTime(0.5))
        val conditional = ConditionalEvent(node, distribution)
        val environmentEvent = AbsoluteEvent<Double>(environment, DoubleTime(3.0))
        val events = listOf(absolute, conditional, environmentEvent)
        val actions = events.map { event -> mockAction(event).also { event.actions = listOf(it) } }
        node.addReaction(absolute)
        node.addReaction(conditional)
        environment.addReaction(environmentEvent)
        node.place()
        val clonedBeforeFiring = node.cloneNode(Time.ZERO).reactions.current
        assertEquals(1, clonedBeforeFiring.size)
        assertIs<ConditionalEvent<Double>>(clonedBeforeFiring.single())
        assertNotSame(conditional, clonedBeforeFiring.single())
        engine.initializeForTest()
        repeat(events.size) {
            engine.stepForTest()
            engine.drainCommands()
        }
        actions.forEach { verify(exactly = 1) { it.execute() } }
        assertEquals(events.size.toLong(), engine.step)
        verify(exactly = 1) { distribution.sample() }
        events.forEach { event ->
            assertFalse(event.isScheduled())
            verify(exactly = 0) { scheduler.updateReaction(event) }
            assertEquals(0, event.nextOccurrence.observers.size)
        }
        assertTrue(node.reactions.current.isEmpty())
        assertTrue(environment.reactions.current.isEmpty())
        assertTrue(node.cloneNode(DoubleTime(3.0)).reactions.current.isEmpty())
        engine.stepForTest()
        actions.forEach { verify(exactly = 1) { it.execute() } }
        assertEquals(events.size.toLong(), engine.step)
    }

    @Test
    fun `an absolute event expires at its occurrence when its conditions are invalid`() {
        val event = AbsoluteEvent<Double>(node, Time.ZERO)
        val condition = mockk<Condition<Double>>(relaxed = true) {
            every { reaction } returns event
            every { isValid } returns observe(false)
        }
        event.conditions = listOf(condition)
        node.addReaction(event)
        node.place()
        engine.initializeForTest()
        assertEquals(Time.ZERO, event.nextOccurrence.current)
        assertTrue(event.canExecute.current)
        engine.stepForTest()
        engine.drainCommands()
        assertEquals(1L, engine.step)
        verify(exactly = 0) { condition.beforeReactionFires() }
        assertFalse(event.isScheduled())
        assertFalse(event in node.reactions.current)
    }

    @Test
    fun `runtime host mutations synchronize scheduler membership for both host types`() {
        node.place()
        engine.initializeForTest()
        val nodeReaction = mockReaction()
        val environmentReaction = mockReaction(hostedBy = environment)
        node.addReaction(nodeReaction)
        environment.addReaction(environmentReaction)
        engine.drainCommands()
        assertTrue(nodeReaction.isScheduled())
        assertTrue(environmentReaction.isScheduled())
        node.removeReaction(nodeReaction)
        environment.removeReaction(environmentReaction)
        engine.drainCommands()
        assertFalse(nodeReaction.isScheduled())
        assertFalse(environmentReaction.isScheduled())
    }

    @Test
    fun `reactions added and removed within one mutation are never initialized`() {
        node.place()
        engine.initializeForTest()
        val transientNode = GenericNode(environment)
        val reactions = listOf(mockReaction(), mockReaction(hostedBy = environment), mockReaction(transientNode))
        val (nodeReaction, environmentReaction, transientNodeReaction) = reactions
        transientNode.addReaction(transientNodeReaction)
        engine.schedule {
            node.addReaction(nodeReaction)
            node.removeReaction(nodeReaction)
            environment.addReaction(environmentReaction)
            environment.removeReaction(environmentReaction)
            transientNode.place(1, 1)
            environment.removeNode(transientNode)
        }
        engine.drainCommands()
        reactions.forEach { reaction ->
            verify(exactly = 0) { reaction.initializationComplete(any(), any()) }
            verify(exactly = 0) { scheduler.addReaction(reaction) }
            assertEquals(0, reaction.nextOccurrence.observers.size)
        }
    }

    @Test
    fun `reactions added before initialization are scheduled once and can be removed afterwards`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        engine.initializeForTest()
        engine.drainCommands()
        verify(exactly = 1) { scheduler.addReaction(reaction) }
        node.removeReaction(reaction)
        engine.drainCommands()
        assertFalse(reaction.isScheduled())
    }

    @Test
    fun `runtime node membership synchronizes each hosted reaction`() {
        engine.initializeForTest()
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        engine.drainCommands()
        assertTrue(reaction.isScheduled())
        environment.removeNode(node)
        engine.drainCommands()
        assertFalse(reaction.isScheduled())
        verify { reaction.dispose() }
    }

    @Test
    fun `duplicate registration is rejected`() {
        node.addReaction(mockReaction())
        node.place()
        engine.initializeForTest()
        assertFailsWith<IllegalStateException> { engine.initializeForTest() }
    }

    @Test
    fun `failed scheduler registration propagates without an engine subscription`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        every { scheduler.addReaction(any()) } throws IllegalStateException("synthetic scheduler failure")
        assertFailsWith<IllegalStateException> { engine.initializeForTest() }
        assertEquals(0, reaction.nextOccurrence.observers.size)
        assertFalse(reaction.isScheduled())
        verify(exactly = 0) { reaction.dispose() }
    }

    @Test
    fun `running-engine callbacks are confined to the simulation thread`() {
        val reaction = mockReaction().also(node::addReaction)
        node.place()
        val worker = Thread(engine::run)
        worker.start()
        try {
            assertEquals(Status.READY, engine.waitFor(Status.READY, 5, TimeUnit.SECONDS))
            assertTrue(reaction.isScheduled())
            assertFailsWith<IllegalStateException> { reaction.emit(DoubleTime(3.0)) }
            verify(exactly = 0) { scheduler.updateReaction(reaction) }
        } finally {
            engine.terminate()
            worker.join(5_000)
            assertFalse(worker.isAlive)
        }
    }
}
