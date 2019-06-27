package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
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
    ) : this(
        env,
        rg,
        when {
            HeterogeneousPedestrian.CHILD_KEYWORDS.contains(age) -> Age.CHILD
            HeterogeneousPedestrian.ADULT_KEYWORDS.contains(age) -> Age.ADULT
            HeterogeneousPedestrian.ELDERLY_KEYWORDS.contains(age) -> Age.ELDERLY
            else -> throw IllegalArgumentException("$age is not a valid age")
        },
        when {
            HeterogeneousPedestrian.MALE_KEYWORDS.contains(gender) -> Gender.MALE
            HeterogeneousPedestrian.FEMALE_KEYWORDS.contains(gender) -> Gender.FEMALE
            else -> throw IllegalArgumentException("$gender is not a valid gender")
        }
    )

    constructor(
        env: Environment<T, P>,
        rg: RandomGenerator,
        age: Int,
        gender: String
    ) : this(
        env,
        rg,
        Age.getCategory(age),
        when {
            HeterogeneousPedestrian.MALE_KEYWORDS.contains(gender) -> Gender.MALE
            HeterogeneousPedestrian.FEMALE_KEYWORDS.contains(gender) -> Gender.FEMALE
            else -> throw IllegalArgumentException("$gender is not a valid gender")
        }
    )
}