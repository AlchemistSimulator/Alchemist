package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.agents.heterogeneous.AbstractHeterogeneousPedestrian
import it.unibo.alchemist.agents.heterogeneous.HeterogeneousPedestrian
import it.unibo.alchemist.characteristics.cognitive.*
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position

abstract class AbstractCognitivePedestrian<T, P : Position<P>> (
    env: Environment<T, P>,
    age: Age,
    gender: Gender
) : CognitivePedestrian<T>, AbstractHeterogeneousPedestrian<T>(env, age, gender) {

    private val dangerBelief = BeliefDanger()
    private val fear = Fear()
    private val desireEvacuate = DesireEvacuate()
    private val desireWalkRandomly = DesireWalkRandomly()
    private val helpAttitude = HelpAttitude()

    override val dangerBeliefLevel = { dangerBelief.level }

    override val fearLevel = { fear.level }

    override val desireEvacuateLevel = { desireEvacuate.level }

    override val desireWalkRandomlyLevel = { desireWalkRandomly.level }

    override val probabilityOfHelping = {
        toHelp: HeterogeneousPedestrian<T> -> helpAttitude.level(toHelp)
    }
}