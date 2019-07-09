package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import kotlin.math.cos
import kotlin.math.sin

open class GradientSteeringAction<T, P : Position2D<P>>(
    env: Environment<T, P>,
    pedestrian: CognitivePedestrian<T>,
    targetMolecule: Molecule,
    formula: Iterable<P>.(Layer<T, P>?) -> P?
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy {
        with(env.getPosition(pedestrian)) {
            this.surrounding(env, 1.0, 12)
                .formula(env.getLayer(targetMolecule).get()) ?: this
        }
    },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)

private fun <P : Position2D<P>> P.surrounding(env: Environment<*, P>, radius: Double, quantity: Int): List<P> =
    (1..quantity).map { it * Math.PI * 2 / quantity }
            .map { env.makePosition(this.x + radius, y).rotate(env, this, it) }

private fun <P : Position2D<P>> P.rotate(
    env: Environment<*, P>,
    center: P = env.makePosition(0.0, 0.0),
    radians: Double
): P = with(this - center) {
    env.makePosition(x * cos(radians) - y * sin(radians), y * cos(radians) + x * sin(radians))
} + center