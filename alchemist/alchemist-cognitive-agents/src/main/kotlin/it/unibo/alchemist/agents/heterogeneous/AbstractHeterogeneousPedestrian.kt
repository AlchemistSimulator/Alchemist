package it.unibo.alchemist.agents.heterogeneous

import it.unibo.alchemist.agents.homogeneous.AbstractHomogeneousPedestrian
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Compliance
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.characteristics.individual.Speed
import it.unibo.alchemist.model.interfaces.Environment

abstract class AbstractHeterogeneousPedestrian<T>(
    env: Environment<T, *>,
    final override val age: Age,
    final override val gender: Gender
) : HeterogeneousPedestrian<T>, AbstractHomogeneousPedestrian<T>(env) {

    override val speed = Speed(age, gender)

    override val compliance = Compliance(age, gender)
}