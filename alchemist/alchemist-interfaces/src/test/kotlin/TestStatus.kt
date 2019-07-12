/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.data.forall
import io.kotlintest.should
import io.kotlintest.shouldNot
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.core.interfaces.Status.INIT
import it.unibo.alchemist.core.interfaces.Status.PAUSED
import it.unibo.alchemist.core.interfaces.Status.READY
import it.unibo.alchemist.core.interfaces.Status.RUNNING
import it.unibo.alchemist.core.interfaces.Status.TERMINATED

/**
 */
class TestStatus : StringSpec({
    "subsequent statuses should be reachable, previous ones should not" {
        val allStatuses = Status.values().toSet()
        forall(
            row(INIT, allStatuses),
            row(READY, allStatuses.minusElement(INIT)),
            row(PAUSED, setOf(RUNNING, TERMINATED)),
            row(RUNNING, setOf(PAUSED, TERMINATED)),
            row(TERMINATED, emptySet())
        ) { initial, states ->
            val reachable = states + initial
            reachable.forEach { it should beReachableFrom(initial) }
            allStatuses.minus(reachable).forEach {
                it shouldNot beReachableFrom(initial)
            }
        }
    }
}) {
    companion object {
        fun beReachableFrom(initial: Status) = object : Matcher<Status> {
            override fun test(value: Status) = Result(value.isReachableFrom(initial),
                "$value should be reachable from $initial",
                "$value should not be reachable from $initial")
        }
    }
}
