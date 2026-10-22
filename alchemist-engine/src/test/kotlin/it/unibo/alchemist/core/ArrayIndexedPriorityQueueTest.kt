/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import io.mockk.mockk
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ArrayIndexedPriorityQueueTest {
    private val queue = ArrayIndexedPriorityQueue<Double>()

    @Test
    fun `an empty queue has no next reaction`() {
        assertNull(queue.next)
    }

    @Test
    fun `anticipating a reaction makes it the next one`() {
        val reactions = (1..7).map { reactionAt(it.toDouble()) }
        reactions.forEach(queue::addReaction)
        reactions.last().rescheduleAt(0.5)
        queue.updateReaction(reactions.last())
        assertSame(reactions.last(), queue.next)
    }

    @Test
    fun `equal times are all served`() {
        val reactions = List(10) { reactionAt(1.0) }
        reactions.forEach(queue::addReaction)
        assertEquals(reactions.toSet(), drain().toSet())
    }

    @Test
    fun `postponing the next reaction promotes the following one`() {
        val first = reactionAt(1.0)
        val second = reactionAt(2.0)
        listOf(first, second, reactionAt(3.0)).forEach(queue::addReaction)
        first.rescheduleAt(10.0)
        queue.updateReaction(first)
        assertSame(second, queue.next)
    }

    @Test
    fun `random operations always serve the earliest reaction`() {
        val random = Random(SEED)
        val scheduled = mutableListOf<FakeReaction>()
        repeat(OPERATIONS) { _ ->
            when {
                scheduled.isEmpty() || random.nextInt(3) == 0 ->
                    reactionAt(random.nextTime()).also(scheduled::add).also(queue::addReaction)
                random.nextBoolean() -> {
                    val reaction = scheduled.random(random)
                    reaction.rescheduleAt(random.nextTime())
                    queue.updateReaction(reaction)
                }
                else -> queue.removeReaction(scheduled.removeAt(random.nextInt(scheduled.size)))
            }
            assertEquals(scheduled.minOfOrNull { it.time }, queue.next?.time)
            assertEquals(scheduled.toSet(), queue.tree.toSet())
        }
        assertEquals(scheduled.map { it.time }.sorted(), drainTimes())
    }

    @Test
    fun `removing a reaction from the middle keeps the remaining ones in order`() {
        val reactions = (1..15).map { reactionAt(it.toDouble()) }
        reactions.forEach(queue::addReaction)
        val removed = reactions.filterIndexed { index, _ -> index % 3 == 1 }
        removed.forEach(queue::removeReaction)
        assertEquals((reactions - removed.toSet()).map { it.time }, drainTimes())
    }

    @Test
    fun `removing the last reaction empties the queue`() {
        val reaction = reactionAt(1.0)
        queue.addReaction(reaction)
        queue.removeReaction(reaction)
        assertNull(queue.next)
        assertEquals(emptyList(), queue.tree)
    }

    @Test
    fun `removing the next reaction promotes the following one`() {
        val reactions = listOf(4.0, 1.0, 3.0, 2.0, 5.0).map(::reactionAt)
        reactions.forEach(queue::addReaction)
        queue.removeReaction(reactions[1])
        assertSame(reactions[3], queue.next)
    }

    @Test
    fun `the next reaction is the earliest one regardless of insertion order`() {
        val reactions = listOf(5.0, 3.0, 8.0, 1.0, 4.0, 9.0, 2.0).map(::reactionAt)
        reactions.forEach(queue::addReaction)
        assertSame(reactions[3], queue.next)
    }

    @Test
    fun `updating a reaction that is not in the queue does nothing`() {
        val scheduled = reactionAt(2.0)
        queue.addReaction(scheduled)
        queue.updateReaction(reactionAt(1.0))
        assertSame(scheduled, queue.next)
        assertEquals(listOf(scheduled), queue.tree)
    }

    private fun drain(): List<Reaction<Double>> = generateSequence { queue.next?.also(queue::removeReaction) }.toList()

    private fun drainTimes(): List<Time> = drain().map { it.time }

    private fun reactionAt(time: Double) = FakeReaction(time)

    private fun Random.nextTime(): Double = nextInt(TIME_RANGE).toDouble()

    private val Reaction<Double>.time: Time get() = nextOccurrence.current

    /**
     * A reaction with a real, reschedulable occurrence and identity equality.
     * Mocking every reaction would make mockk record each call, exhausting the heap in the randomized test.
     */
    private class FakeReaction(time: Double) : Reaction<Double> by UNUSED {
        override val nextOccurrence: MutableObservable<Time> = observe(DoubleTime(time))

        fun rescheduleAt(time: Double) {
            nextOccurrence.current = DoubleTime(time)
        }
    }

    private companion object {
        private const val OPERATIONS = 5_000
        private const val SEED = 42

        // A narrow range of integer times produces many ties.
        private const val TIME_RANGE = 50

        // Serves the reaction members the queue never uses.
        private val UNUSED: Reaction<Double> = mockk()
    }
}
