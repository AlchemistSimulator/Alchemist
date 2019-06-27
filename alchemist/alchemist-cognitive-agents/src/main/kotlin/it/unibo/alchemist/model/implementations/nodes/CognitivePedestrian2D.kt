package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.influencesphere.sensory.FieldOfView2D
import it.unibo.alchemist.model.influencesphere.sensory.HearingField2D
import it.unibo.alchemist.model.influencesphere.sensory.InfluenceSphere2D
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

open class CognitivePedestrian2D<T, P : Position2D<P>> @JvmOverloads constructor(
    override val env: Environment<T, P>,
    rg: RandomGenerator,
    age: Age,
    gender: Gender,
    danger: Molecule? = null
) : AbstractCognitivePedestrian<T, P>(env, rg, age, gender, danger) {

    @JvmOverloads constructor(
        env: Environment<T, P>,
        rg: RandomGenerator,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(env, rg, Age.fromString(age), Gender.fromString(gender), danger)

    @JvmOverloads constructor(
        env: Environment<T, P>,
        rg: RandomGenerator,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(env, rg, Age.fromYears(age), Gender.fromString(gender), danger)

    override fun influencialPeople() =
        env.getPosition(this).let {
            it.fieldOfView().peopleWithInfluence().union(it.hearingField().peopleWithInfluence())
        }

    private fun <P : Position2D<P>> P.fieldOfView(): FieldOfView2D<P> =
            FieldOfView2D(this.x, this.y, rg.nextDouble() * 360)

    private fun <P : Position2D<P>> P.hearingField(): HearingField2D<P> =
            HearingField2D(this.x, this.y)

    private fun InfluenceSphere2D<P>.peopleWithInfluence() =
        env.nodes.filter { this.isInfluenced(env.getPosition(it)) }.map { it as CognitivePedestrian<T> }
}