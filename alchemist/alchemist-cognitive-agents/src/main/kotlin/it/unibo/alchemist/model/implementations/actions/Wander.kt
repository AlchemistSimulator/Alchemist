package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator

class Wander<T, P : Position<P>>(
    private val env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    rg: RandomGenerator,
    radius: Double
) : AbstractSteeringAction<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy { with(env) {
        getPosition(pedestrian) + makePosition(*(1..dimensions).map { (rg.nextDouble(-1.0, 1.0)) * radius }.toTypedArray())
    } },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)

private fun RandomGenerator.nextDouble(from: Double, to: Double) = nextDouble() * (from - to) - to