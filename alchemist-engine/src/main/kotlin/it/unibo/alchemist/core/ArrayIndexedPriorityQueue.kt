/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import gnu.trove.impl.Constants
import gnu.trove.map.TObjectIntMap
import gnu.trove.map.hash.TObjectIntHashMap
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max

/**
 * This class implements the indexed priority queue through an Array.
 *
 * @param T concentration type
 */
class ArrayIndexedPriorityQueue<T> : Scheduler<T> {
    override val next: Reaction<T>? get() = tree.firstOrNull()

    /**
     * The binary heap backing this queue, in array form.
     */
    val tree: List<Reaction<T>>
        field = ArrayList<Reaction<T>>()

    private val indexes: TObjectIntMap<Reaction<T>> =
        TObjectIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY)
    private val times = ArrayList<Time>()

    override fun addReaction(reaction: Reaction<T>) {
        tree.add(reaction)
        times.add(reaction.nextOccurrence.current)
        val index = tree.lastIndex
        indexes.put(reaction, index)
        updateEffectively(reaction, index)
    }

    override fun removeReaction(reaction: Reaction<T>) {
        val index = indexes[reaction]
        val last = tree.lastIndex
        if (index == last) {
            tree.removeAt(index)
            indexes.remove(reaction)
            times.removeAt(index)
        } else {
            val swapped = tree[last]
            indexes.put(swapped, index)
            tree[index] = swapped
            times[index] = swapped.nextOccurrence.current
            tree.removeAt(last)
            times.removeAt(last)
            indexes.remove(reaction)
            updateEffectively(swapped, index)
        }
    }

    override fun toString(): String = buildString {
        var pow = 0
        var exp = 0
        times.forEachIndexed { i, time ->
            val tabulars = (floor(ln(times.size.toDouble()) / LN_2) - floor(ln(i + 1.0) / LN_2)).toInt() + 1
            append("\t".repeat(max(0, tabulars))).append(time)
            if (i == pow) {
                exp++
                pow += 1 shl exp
                append('\n')
            }
        }
    }

    override fun updateReaction(reaction: Reaction<T>) {
        val index = indexes[reaction]
        if (index != NO_ENTRY) {
            times[index] = reaction.nextOccurrence.current
            updateEffectively(reaction, index)
        }
    }

    /**
     * Shifts the children preceding [time] into the hole at [index], returning where the hole ends.
     */
    private tailrec fun down(index: Int, time: Time): Int {
        val left = 2 * index + 1
        if (left < tree.size) {
            val right = left + 1
            val minIndex = if (right < tree.size && times[right] < times[left]) right else left
            if (time > times[minIndex]) {
                move(minIndex, index)
                return down(minIndex, time)
            }
        }
        return index
    }

    private fun move(from: Int, to: Int) {
        val moved = tree[from]
        indexes.put(moved, to)
        tree[to] = moved
        times[to] = times[from]
    }

    /**
     * Shifts the parents following [time] into the hole at [index], returning where the hole ends.
     */
    private tailrec fun up(index: Int, time: Time): Int {
        val parentIndex = (index - 1) / 2
        return when {
            index > 0 && time < times[parentIndex] -> {
                move(parentIndex, index)
                up(parentIndex, time)
            }
            else -> index
        }
    }

    private fun updateEffectively(reaction: Reaction<T>, index: Int) {
        // times[index] always holds the reaction's current occurrence here.
        val time = times[index]
        val raised = up(index, time)
        val destination = if (raised == index) down(index, time) else raised
        if (destination != index) {
            indexes.put(reaction, destination)
            tree[destination] = reaction
            times[destination] = time
        }
    }

    private companion object {
        private const val NO_ENTRY = -1
        private val LN_2 = ln(2.0)
    }
}
