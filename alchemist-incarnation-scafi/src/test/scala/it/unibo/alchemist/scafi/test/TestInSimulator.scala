/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.scafi.test

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.Environment
import it.unibo.scafi.space.{Point2D, Point3D}
import it.unibo.scafi.space.Point3D.toPoint2D
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL
import scala.concurrent.duration.{FiniteDuration, NANOSECONDS}
import scala.jdk.CollectionConverters._
import scala.math.Ordering.Double.TotalOrdering

@SuppressFBWarnings(value = Array("SE_BAD_FIELD"), justification = "We are not going to Serialize test classes")
class TestInSimulator[P <: Position[P]] extends AnyFunSuite with Matchers {

  test("Basic test") {
    testNoVar("/plain_vanilla.yml")
  }

  test("Basic failure test") {
    assertThrows[Exception] {
      testNoVar("/plain_error.yml")
    }
  }

  test("Gradient") {
    val env = testNoVar[Any]("/test_gradient.yml")
    env.getNodes
      .iterator()
      .asScala
      .foreach { node =>
        val contents = node.getContents.asScala
        contents(new SimpleMolecule("it.unibo.alchemist.scafi.test.ScafiGradientProgram"))
          .asInstanceOf[Double] should (be >= 0.0 and be <= 100.0)
      }
  }

  test("Environment") {
    val env = testNoVar[Any]("/test_env.yml")
    env.getNodes
      .iterator()
      .asScala
      .foreach { node =>
        import ScafiEnvProgram._
        val contents = node.getContents.asScala
        def getValue[T](name: String) = contents(new SimpleMolecule(name)).asInstanceOf[T]
        def inputMolecule = getValue[Int]("number")
        def outputMolecule = getValue[Int]("number2")

        if (node.getId == 0) {
          inputMolecule shouldBe 77
          outputMolecule shouldBe 177
          val p = getValue[Point3D](MOL_POSITION)
          p.x shouldEqual 4.0 +- 0.0001
          p.y shouldEqual 4.0 +- 0.0001
          p.z shouldEqual 0.0
          getValue[java.time.Instant](MOL_TIMESTAMP).getEpochSecond > 0
          getValue[Long](MOL_DELTA_MANUAL_MILLIS) shouldEqual 2000L +- 1L
          getValue[FiniteDuration](MOL_DELTATIME) shouldEqual FiniteDuration(2_000_000_000L, NANOSECONDS)
          getValue[Option[Double]](MOL_NBR_RANGE).get shouldEqual Math.sqrt(4 + 4) +- 0.1
          getValue[Option[FiniteDuration]](MOL_NBR_LAG).map(_.toMillis).get shouldBe 2000L +- 1999L
          toPoint2D(getValue[Option[Point3D]](MOL_NBR_VECTOR).get) shouldEqual Point2D(-2.0, -2.0)
        } else {
          inputMolecule shouldBe -500
          outputMolecule shouldBe -400
        }
      }
  }

  test("Multiple programs") {
    testNoVar("/test_multiple_program.yml")
  }

  test("Empty molecule concentration") {
    testNoVar("/empty_molecule_initialization.yml")
  }

  private def testNoVar[T](resource: String, maxSteps: Long = 1000): Environment[T, P] =
    testLoading(resource, Map(), maxSteps)

  private def testLoading[T](
      resource: String,
      vars: Map[String, java.lang.Double],
      maxSteps: Long = 1000
  ): Environment[T, P] = {
    import scala.jdk.CollectionConverters._
    val res: URL = classOf[TestInSimulator[P]].getResource(resource)
    res shouldNot be(null)
    val env: Environment[T, P] = LoadAlchemist.from(res).getWith[T, P](vars.asJava).getEnvironment
    val sim = new Engine[T, P](env, maxSteps)
    sim.play()
    sim.run()
    if (sim.getError.isPresent) throw new Exception(sim.getError.get().getMessage)
    env
  }
}
