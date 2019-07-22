package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * Move the pedestrian towards positions of the environment with a high concentration of the target molecule.
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
 */
open class FollowGradient<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    pedestrian: Pedestrian<T>,
    targetMolecule: Molecule,
    rg: RandomGenerator,
    radius: Double
) : GradientSteeringAction<T>(
    env,
    pedestrian,
    targetMolecule,
    rg,
    radius,
    { molecule -> filter { env.canNodeFitPosition(pedestrian, it) }
            .plusElement(env.getPosition(pedestrian))
            .maxBy { env.getLayer(molecule).get().getValue(it) as Double }
    }
)