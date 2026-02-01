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
import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.boundary.variables.GeometricVariable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.junit.jupiter.api.Test

class TestVariables {
    @Test
    fun <P : Position<P>> testDefaultValue() {
        simulation(SAPEREIncarnation()) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

            runLater {
                println("Checking variable")
                rate.shouldBeExactly(5.0)
                this.variablesContext.variables.containsKey("rate").shouldBeTrue()
            }
        }.getDefault<List<LsaMolecule>, P>() // needed to build the simulation
    }

    @Test
    fun <P : Position<P>> testOverrideValue() {
        val loader = simulation(SAPEREIncarnation()) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
            deployments {
                rate.shouldBeExactly(20.0)
                variablesContext.variables.containsKey("rate").shouldBeTrue()
            }
        }
        loader.getWith<List<LsaMolecule>, P>(mapOf("rate" to 20.0))
    }

    @Test
    @Suppress("NoNameShadowing")
    fun <P : Position<P>> testDoubleDeclaration() {
        simulation(SAPEREIncarnation()) {
            val rate: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
            println("First declaration of rate: $rate")
            shouldThrow<IllegalStateException> {
                val rate: Double by variable(GeometricVariable(2.0, 1.0, 5.0, 1))
                println("This line should not be printed: $rate")
            }
        }
    }

    @Test
    fun <P : Position<P>> testDependendVariable() {
        val loader = simulation(ProtelisIncarnation()) {
            val geometricVariable: Double by variable(GeometricVariable(10.0, 1.0, 1000.0, 4))
            val linearVariable: Double by variable(LinearVariable(1.0, 1.0, 2.0, 3.0))

            val mSize by variable { -linearVariable }
            val sourceStart by variable { mSize / geometricVariable }
            val sourceSize by variable { linearVariable / 5.0 }

            deployments {
                deploy(point(0.0, 0.0)) {
                    all {
                        molecule = "mSize"
                        concentration = mSize
                    }
                    all {
                        molecule = "sourceStart"
                        concentration = sourceStart
                    }
                    all {
                        molecule = "sourceSize"
                        concentration = sourceSize
                    }
                }
            }
        }
        loader.variables.size shouldBe 2
        loader.dependentVariables.size shouldBe 0
        loader.getWith<List<LsaMolecule>, P>(mapOf("linearVariable" to 10.0)).apply {
            environment.nodes.size shouldBe 1
            val node = environment.nodes.single()
            node.getConcentration(SimpleMolecule("mSize")) shouldBe -10.0
            node.getConcentration(SimpleMolecule("sourceStart")) shouldBe -1.0
            node.getConcentration(SimpleMolecule("sourceSize")) shouldBe 2.0
        }
    }
}
