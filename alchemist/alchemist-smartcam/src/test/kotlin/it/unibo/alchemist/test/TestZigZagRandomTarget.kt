package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.movestrategies.ZigZagRandomTarget
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.random.ISAACRandom

/**
 * This tests [ZigZagRandomTarget] and it's complicated because it takes in account both double comparison problems and
 * the rare event in which a new random direction is the same as the previous one.
 */
class TestZigZagRandomTarget : StringSpec() {
    private val maxDistance = 10000.0
    private val minChangeInDirection = 0.1
    private lateinit var zigZag: ZigZagRandomTarget<Any>
    private lateinit var currentPosition: Euclidean2DPosition
    private var initialDirectionAngle = 0.0

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        currentPosition = Euclidean2DPosition(0.0, 0.0)
        zigZag = ZigZagRandomTarget({ currentPosition }, { x, y -> Euclidean2DPosition(x, y) }, ISAACRandom(0), maxDistance, minChangeInDirection)
        initialDirectionAngle = angleTo(zigZag.target)
    }

    init {
        "Target should not change while advancing toward it" {
            val steps = 10
            for (i in 1 until steps - 1) {
                advance(maxDistance / steps)
                shouldNotHaveChangedDirection()
            }
        }

        "Target should change after maxDistance" {
            advance(maxDistance + 0.1) // go past maxDistance
            shouldHaveChangedDirection()
        }

        "Consecutive queries without having moved should result in a change in direction" {
            shouldHaveChangedDirection()
            shouldHaveChangedDirection()
        }
    }

    private fun advance(distance: Double) {
        currentPosition += Euclidean2DPosition(cos(initialDirectionAngle) * distance, sin(initialDirectionAngle) * distance)
    }

    private fun angleTo(target: Euclidean2DPosition) = (target - currentPosition).asAngle()

    private fun shouldNotHaveChangedDirection() = angleTo(zigZag.target) shouldBe (initialDirectionAngle plusOrMinus minChangeInDirection / 2)

    private fun shouldHaveChangedDirection() = angleTo(zigZag.target) shouldNotBe (initialDirectionAngle plusOrMinus minChangeInDirection)
}
