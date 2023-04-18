/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.core.interfaces.Status.INIT
import it.unibo.alchemist.core.interfaces.Status.PAUSED
import it.unibo.alchemist.core.interfaces.Status.READY
import it.unibo.alchemist.core.interfaces.Status.RUNNING
import it.unibo.alchemist.core.interfaces.Status.TERMINATED

/**
 * Tests that the state machine is coherent.
 */
class TestStatus : StringSpec({
    "subsequent statuses should be reachable, previous ones should not" {
        val allStatuses = Status.values().toSet()
        forAll(
            row(INIT, allStatuses),
            row(READY, allStatuses.minusElement(INIT)),
            row(PAUSED, setOf(RUNNING, TERMINATED)),
            row(RUNNING, setOf(PAUSED, TERMINATED)),
            row(TERMINATED, emptySet()),
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
            override fun test(value: Status) = MatcherResult(
                value.isReachableFrom(initial),
                { "$value should be reachable from $initial" },
                { "$value should not be reachable from $initial" },
            )
        }
    }
}
