import it.unibo.alchemist.model.implementations.actions.RunScafiProgram
import it.unibo.alchemist.model.ScafiIncarnation
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.reactions.{ChemicalReaction, Event}
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.MersenneTwister
import org.scalatest.{FunSuite, Matchers}

/**
  * @author Roberto Casadei
  *
  */

class TestScafiIncarnation extends FunSuite with Matchers {
  private val INC = new ScafiIncarnation

  /**
    * Tests the ability of {@link ScafiIncarnation} of properly building
    * Alchemist entities for running Scafi.
    */
  test("build") {
    val rng = new MersenneTwister(0)
    val env = new Continuous2DEnvironment[Any]
    val node = INC.createNode(rng, env, null)
    assertNotNull(node)

    val standard = INC.createTimeDistribution(rng, env, node, "3")
    assertNotNull(standard)
    standard.getRate shouldEqual 3d

    val generic = INC.createReaction(rng, env, node, standard, null)
    assertNotNull(generic)
    assertTrue(generic.isInstanceOf[Event[_]])
  }

  private def testIsScafiProgram(program: Reaction[Any]) {
    assertNotNull(program)
    assertTrue(program.isInstanceOf[Event[_]])
    assertTrue(program.getConditions.isEmpty)
    assertFalse(program.getActions.isEmpty)
    assertEquals(1, program.getActions.size)

    val prog = program.getActions.get(0)
    assertNotNull(prog)
    assertTrue(prog.isInstanceOf[RunScafiProgram])
  }

  /**
    * Verifies that the incarnation can properly init new concentrations.
    */
  test("Create concentration") {
    assertEquals("aString", INC.createConcentration("\"aString\""))
    assertEquals(1.0, INC.createConcentration("1"))
    assertEquals(true, INC.createConcentration("val a = 7 == 7; a"))
  }

  private def assertNotNull(expr: AnyRef) = expr shouldNot be(null)
  private def assertTrue(pred: Boolean) = pred shouldBe(true)
  private def assertFalse(pred: Boolean) = assertTrue(!pred)
  private def assertEquals[T](expected: T, actual: T) = expected shouldEqual(actual)
}
