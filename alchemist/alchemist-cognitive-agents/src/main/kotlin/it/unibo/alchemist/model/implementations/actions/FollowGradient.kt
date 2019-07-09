package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D

open class FollowGradient<T, P : Position2D<P>>(
    env: Environment<T, P>,
    pedestrian: CognitivePedestrian<T>,
    targetMolecule: Molecule
) : GradientSteeringAction<T, P>(
    env,
    pedestrian,
    targetMolecule,
    { layer -> map { it to layer?.getValue(it) as Double }.maxBy { it.second }?.first }
)