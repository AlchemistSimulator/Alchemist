/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.scafi.test

import it.unibo.alchemist.model.ScafiIncarnation
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.physics.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.reactions.Event
import org.apache.commons.math3.random.MersenneTwister
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

// TODO: run with JUnit 5
// @ExtendWith(classOf[JUnitRunner])
class TestScafiIncarnation extends AnyFunSuite with Matchers {
  private val INC = new ScafiIncarnation[Any, Euclidean2DPosition]

  /** Tests the ability of [[ScafiIncarnation]] of properly building Alchemist entities for running Scafi. */
  test("build") {
    val rng = new MersenneTwister(0)
    val env = new Continuous2DEnvironment[Any](INC)
    val node = INC.createNode(rng, env, null)
    assertNotNull(node)

    val standard = INC.createTimeDistribution(rng, env, node, "3")
    assertNotNull(standard)
    standard.getRate shouldEqual 3d

    val generic = INC.createReaction(rng, env, node, standard, null)
    assertNotNull(generic)
    assertTrue(generic.isInstanceOf[Event[_]])
  }

  /** Verifies that the incarnation can properly init new concentrations. */
  test("Create concentration") {
    assertEquals("aString", INC.createConcentration("\"aString\""))
    assertEquals(1.0, INC.createConcentration("1"))
    assertEquals(true, INC.createConcentration("val a = 7 == 7; a"))
  }

  private def assertNotNull(expr: AnyRef) = expr shouldNot be(null)
  private def assertTrue(pred: Boolean) = pred shouldBe true
  private def assertEquals[T](expected: T, actual: T) = expected shouldEqual actual
}
