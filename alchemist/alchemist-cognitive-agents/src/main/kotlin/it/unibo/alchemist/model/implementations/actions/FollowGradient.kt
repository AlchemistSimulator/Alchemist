package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

/**
 * Moves the pedestrian towards positions of the environment with a high concentration of the target molecule.
 */
open class FollowGradient<T, P : Position2D<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    targetMolecule: Molecule,
    rg: RandomGenerator,
    radius: Double
) : GradientSteeringAction<T, P>(
    env,
    pedestrian,
    targetMolecule,
    rg,
    radius,
    { molecule -> this.map { it to env.getLayer(molecule).get().getValue(it) as Double }.maxBy { it.second }?.first }
)