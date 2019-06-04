package it.unibo.alchemist.agents.homogeneous

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D

open class HomogeneousPedestrian2D<T, P : Position2D<P>>(
    env: Environment<T, P>
) : AbstractHomogeneousPedestrian<T>(env)