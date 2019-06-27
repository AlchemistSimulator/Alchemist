package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

open class HeterogeneousPedestrian2D<T, P : Position2D<P>>(
    env: Environment<T, P>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender
) : AbstractHeterogeneousPedestrian<T>(env, rg, age, gender) {

    constructor(
        env: Environment<T, P>,
        rg: RandomGenerator,
        age: String,
        gender: String
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender))

    constructor(
        env: Environment<T, P>,
        rg: RandomGenerator,
        age: Int,
        gender: String
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender))
}