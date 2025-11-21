/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.boundary.dsl.model.AvailableIncarnations.SAPERE
import it.unibo.alchemist.boundary.dsl.model.SimulationContextImpl
import it.unibo.alchemist.boundary.variables.GeometricVariable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.junit.jupiter.api.Test
@Suppress("UNCHECKED_CAST")
class TestVariables {
    @Test
    fun <T, P : Position<P>> testDefaultValue() {
        val incarnation = SAPERE.incarnation<T, Euclidean2DPosition>()
        simulation(incarnation) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

            runLater {
                println("Checking variable")
                rate.shouldBeExactly(5.0)
                (this as SimulationContextImpl<T, P>).variablesContext
                    .variables.containsKey("rate").shouldBeTrue()
            }
        }.getDefault<T, P>() // needed to build the simulation
    }

    @Test
    fun <T : Any, P : Position<P>> testOverrideValue() {
        val incarnation = SAPERE.incarnation<T, Euclidean2DPosition>()
        val loader = simulation(incarnation) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
            deployments {
                rate.shouldBeExactly(20.0)
                (this@simulation as SimulationContextImpl<T, P>).variablesContext
                    .variables.containsKey("rate").shouldBeTrue()
            }
        }
        loader.getWith<T, P>(mapOf(("rate" to 20.0)))
    }

    @Suppress("NoNameShadowing")
    @Test
    fun <T, P : Position<P>> testDoubleDeclaration() {
        val incarnation = SAPERE.incarnation<T, Euclidean2DPosition>()
        simulation(incarnation) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
            println("First declaration of rate: $rate")
            shouldThrow<IllegalStateException> {
                val rate: Double by variable(GeometricVariable(2.0, 1.0, 5.0, 1))
                println("This line should not be printed: $rate")
            }
        }
    }

    @Test
    fun <T, P : Position<P>> testDependendVariable() {
        val incarnation = SAPERE.incarnation<T, Euclidean2DPosition>()
        val loader = simulation(incarnation) {
            val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
            val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

            val mSize by variable { -size }
            val sourceStart by variable { mSize / 10.0 }
            val sourceSize by variable { size / 5.0 }

            runLater {
                rate.shouldBe(2.0)
                size.shouldBe(10.0)
                mSize.shouldBe(-10.0)
                sourceStart.shouldBe(-1.0)
                sourceSize.shouldBe(2.0)
            }
        }
        loader.getWith<T, P>(mapOf("size" to 10.0))
    }
}
