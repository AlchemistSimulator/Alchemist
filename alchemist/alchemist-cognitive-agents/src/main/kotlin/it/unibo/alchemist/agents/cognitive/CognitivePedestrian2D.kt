package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.sensory.FieldOfView2D
import it.unibo.alchemist.sensory.HearingField2D
import it.unibo.alchemist.sensory.InfluenceSphere2D
import org.apache.commons.math3.random.RandomGenerator

open class CognitivePedestrian2D<T, P : Position2D<P>>(
    private val env: Environment<T, P>,
    private val rg: RandomGenerator,
    age: String,
    gender: String
) : AbstractCognitivePedestrian<T, P>(env, rg, age, gender) {

    override fun influencialPeople() =
            env.getPosition(this).let {
                it.fieldOfView().peopleWithInfluence().union(it.hearingField().peopleWithInfluence())
            }

    private fun <P : Position2D<P>> P.fieldOfView(): FieldOfView2D<P> =
            FieldOfView2D(this.x, this.y,
                    rg.nextDouble()*360,
                    120.0,
                    10.0)

    private fun <P : Position2D<P>> P.hearingField(): HearingField2D<P> =
            HearingField2D(this.x, this.y, 5.0)

    private fun InfluenceSphere2D<P>.peopleWithInfluence() =
            env.nodes.filter { this.isInfluenced(env.getPosition(it)) }.map { it as CognitivePedestrian<T> }
}