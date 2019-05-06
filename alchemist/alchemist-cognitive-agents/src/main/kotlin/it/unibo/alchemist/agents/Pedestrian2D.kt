package it.unibo.alchemist.agents

import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.characteristics.individual.Speed
import it.unibo.alchemist.characteristics.individual.Compliance
import it.unibo.alchemist.characteristics.Characteristic
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.sensory.InfluenceSphere2D
import kotlin.reflect.KClass

/**
 * A pedestrian in the bidimensional world
 */
class Pedestrian2D<T, P : Position2D<P>> private constructor(
    env: Environment<T, P>,
    pos: P,
    characteristics: Collection<Characteristic>,
    senses: Collection<InfluenceSphere2D<P>>
) : Pedestrian<T, P>, AbstractNode<T>(env) {

    override fun createT(): T = TODO()

    override val age = characteristics.find { it is Age } as Age?
    override val gender = characteristics.find { it is Gender } as Gender?

    /**
     * A builder of bidimensional pedestrians
     */
    class Builder<T, P : Position2D<P>>(
        val env: Environment<T, P>,
        val pos: P
    ) : Pedestrian.Builder<T, P, InfluenceSphere2D<P>> {

        private val characteristics: MutableMap<KClass<out Characteristic>, Characteristic> = mutableMapOf()
        private val senses: MutableMap<KClass<out InfluenceSphere2D<P>>, InfluenceSphere2D<P>> = mutableMapOf()

        override fun attachSensorialSphere(sense: InfluenceSphere2D<P>) = apply { senses[sense::class] = sense }

        override fun specifyAge(age: Age) = apply {
            characteristics[Age::class] = age
            characteristics[Gender::class]?.let { insertDerivedCharacteristics(age, it as Gender) }
        }

        fun specifyAge(age: Int) = specifyAge(Age.getCategory(age))

        override fun specifyGender(gender: Gender) = apply {
            characteristics[Gender::class] = gender
            characteristics[Age::class]?.let { insertDerivedCharacteristics(it as Age, gender) }
        }

        override fun build(): Pedestrian2D<T, P> =
                Pedestrian2D(env, pos, characteristics.values, senses.values).also { env.addNode(it, pos) }

        private fun insertDerivedCharacteristics(age: Age, gender: Gender) {
            characteristics[Speed::class] = Speed(age, gender)
            characteristics[Compliance::class] = Compliance(age, gender)
        }
    }
}