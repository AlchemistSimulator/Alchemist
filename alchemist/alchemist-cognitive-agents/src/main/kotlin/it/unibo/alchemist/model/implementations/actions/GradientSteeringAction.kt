package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.utils.surrounding
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import org.apache.commons.math3.random.RandomGenerator

private const val QUANTITY_OF_POINTS = 6

/**
 * Generic implementation of an action influenced by the concentration of a given molecule in the environment.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 * @param rg
 *          the simulation {@link RandomGenerator}.
 * @param radius
 *          the distance all the positions where the molecule concentration is checked
 *          must have from the current pedestrian position.
 * @param formula
 *          the logic according to the target position is determined from all the positions checked.
 */
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
            this.surrounding(env, rg, radius, QUANTITY_OF_POINTS)
                .formula(targetMolecule) ?: this
        }
    },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
)