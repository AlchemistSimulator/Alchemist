package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

open class HeterogeneousPedestrian2D<T, P : Position2D<P>>(
    env: Environment<T, P>,
    rg: RandomGenerator,
    age: String,
    gender: String
) : AbstractHeterogeneousPedestrian<T>(env, rg, age, gender)