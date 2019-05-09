package it.unibo.alchemist.agents.heterogeneous

import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D

open class HeterogeneousPedestrian2D<T, P : Position2D<P>>(env: Environment<T, P>, age: Age, gender: Gender)
    : AbstractHeterogeneousPedestrian<T>(env, age, gender)