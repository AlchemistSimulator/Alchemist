package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

open class GradientSteeringAction<T, P : Position2D<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    targetMolecule: Molecule,
    rg: RandomGenerator,
    radius: Double,
    formula: Iterable<P>.(Molecule) -> P?
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy {
        with(env.getPosition(pedestrian)) {
            this.surrounding(env, rg, radius, 6)
                .formula(targetMolecule) ?: this
        }
    },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)

private fun <P : Position2D<P>> P.surrounding(
    env: Environment<*, P>,
    rg: RandomGenerator,
    radius: Double,
    quantity: Int
): List<P> =
    (1..quantity).map { it * Math.PI * 2 / quantity }
            .shuffled(rg)
            .map { env.makePosition(this.x + radius, y).rotate(env, this, it) }

private fun <P : Position2D<P>> P.rotate(
    env: Environment<*, P>,
    center: P = env.makePosition(0.0, 0.0),
    radians: Double
): P = with(this - center) {
    env.makePosition(x * cos(radians) - y * sin(radians), y * cos(radians) + x * sin(radians))
} + center

// Fisher–Yates shuffle
private fun <R> Iterable<R>.shuffled(rg: RandomGenerator): Iterable<R> = toMutableList().apply {
    for (i in size - 1 downTo 1) {
        Collections.swap(this, i, rg.nextInt(i + 1))
    }
}