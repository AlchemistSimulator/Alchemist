/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import arrow.core.Option
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.rx.model.adapters.ObservableNeighborhood
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.spawnNode
import it.unibo.alchemist.rx.model.utils.TestEnvironmentFactory.withObservableTestEnvironment

class ObservableNeighborhoodTest : FunSpec({
    test("neighborhoods should be created and properly observable through the environment") {
        withObservableTestEnvironment {
            val origin = spawnNode(0.0, 0.0)

            val updates: MutableList<Option<ObservableNeighborhood<Double>>> = mutableListOf()
            observeNeighborhood(origin).onChange(this) { updates.add(it) }

            updates[0].getOrNull().shouldNotBeNull()
            updates[0].getOrNull()!!.center shouldBe origin
            updates[0].getOrNull()!!.neighbors.shouldBeEmpty()

            val neighbors = listOf(
                spawnNode(0.0, 1.0),
                spawnNode(1.0, 0.0),
                spawnNode(1.0, 1.0),
            )

            updates[1].getOrNull()?.neighbors shouldContainExactlyInAnyOrder neighbors.take(1)
            updates[2].getOrNull()?.neighbors shouldContainExactlyInAnyOrder neighbors.take(2)
            updates[3].getOrNull()?.neighbors shouldContainExactlyInAnyOrder neighbors.take(3)
        }
    }
})
