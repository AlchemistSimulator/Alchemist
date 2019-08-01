package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * Move the pedestrian towards positions of the environment with a low concentration of the target molecule.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 */
open class AvoidGradient<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    pedestrian: Pedestrian2D<T>,
    targetMolecule: Molecule
) : GradientSteeringAction<T>(
    env,
    pedestrian,
    targetMolecule,
    { molecule ->
        val currentPosition = env.getPosition(pedestrian)
        val layer = env.getLayer(molecule).get()
        val currentConcentration = layer.getValue(currentPosition) as Double
        this.map { it to layer.getValue(it) as Double }
            .filter { it.second < currentConcentration }
            .minBy { it.second }?.first ?: currentPosition
    }
)