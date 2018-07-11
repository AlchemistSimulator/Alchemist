/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
import java.io.InputStream

import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.Environment
import org.scalatest.{FunSuite, Matchers}
import org.slf4j.event.Level
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsScalaMapConverter
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.interfaces.Position

@SuppressFBWarnings(value = Array("SE_BAD_FIELD"), justification="We are not going to Serialize test classes")
class TestInSimulator[P <: Position[P]] extends FunSuite with Matchers {
  
  test("Basic test"){
    testNoVar("/plain_vanilla.yml")
  }
  test("Gradient"){
    val env = testNoVar[Any]("/test_gradient.yml")
    env.getNodes.asScala.foreach(node => {
      val contents = node.getContents.asScala
      (contents.get(new SimpleMolecule("test.scafiprograms.ScafiGradientProgram")).get.asInstanceOf[Double]) should (be >= 0.0 and be <= 100.0)
    })
  }

  test("Environment"){
    val env = testNoVar[Any]("/test_env.yml")
    env.getNodes.asScala.foreach(node => {
      val contents = node.getContents.asScala
      def inputMolecule = (contents.get(new SimpleMolecule("number")).get.asInstanceOf[Int])
      def outputMolecule = (contents.get(new SimpleMolecule("number2")).get.asInstanceOf[Int])
      if(node.getId==0) {
        inputMolecule shouldBe (77)
        outputMolecule shouldBe (177)
      } else {
        inputMolecule shouldBe (-500)
        outputMolecule shouldBe (-400)
      }
    })
  }

  private def testNoVar[T](resource: String, maxSteps: Long = 1000): Environment[T, P] = {
    testLoading(resource, Map(), maxSteps)
  }

  private def testLoading[T](resource: String, vars: Map[String, java.lang.Double], maxSteps: Long = 1000): Environment[T, P] = {
    import scala.collection.JavaConverters._
    import ch.qos.logback.classic.{Logger,Level}
    LoggerFactory.getLogger("ROOT").asInstanceOf[Logger].setLevel(Level.ERROR)
    val res: InputStream = classOf[TestInSimulator[P]].getResourceAsStream(resource)
    res shouldNot be(null)
    val env: Environment[T, P] = new YamlLoader(res).getWith(vars.asJava)
    val sim = new Engine[T, P](env, maxSteps)
    sim.play()
    sim.run()
    env
  }
}
