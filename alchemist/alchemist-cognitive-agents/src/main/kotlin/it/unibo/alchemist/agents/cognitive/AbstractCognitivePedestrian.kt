package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.AbstractHeterogeneousPedestrian
import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position

abstract class AbstractCognitivePedestrian<T, P : Position<P>> (
    env: Environment<T, P>,
    age: Age,
    gender: Gender
) : CognitivePedestrian<T>, AbstractHeterogeneousPedestrian<T>(env, age, gender) {

    override val dangerBeliefLevel = { TODO() }

    override val fearLevel = { TODO() }

    override val probabilityOfHelping = {
        toHelp: HeterogeneousPedestrian<T> -> TODO()
    }
}