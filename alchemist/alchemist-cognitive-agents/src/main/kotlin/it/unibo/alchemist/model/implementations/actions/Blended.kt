package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

class Blended<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    actions: List<SteeringAction<T, P>>
) : AbstractSteeringAction<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy {
        with(actions.map { it.getNextPosition() to it.target().getDistanceTo(env.getPosition(pedestrian)) }) {
            val totalDistance = this.map { it.second }.sum()
            this.map { env.makePosition(*(it.first * (1 - (it.second / totalDistance)))) }
                .fold(env.getPosition(pedestrian)) { acc, p -> acc + p }
        }
    },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)

private operator fun <P : Position<P>> P.times(n: Double) =
    this.cartesianCoordinates.map { it * n }.toTypedArray()