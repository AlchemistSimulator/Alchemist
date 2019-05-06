package it.unibo.alchemist.agents

import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.sensory.InfluenceSphere

/**
 * A pedestrian is an agent with cognitive capabilities
 */
interface Pedestrian<T, P : Position<P>> : Node<T> {

    val age: Age?

    val gender: Gender?

    /**
     * A builder of pedestrians
     */
    interface Builder<T, P : Position<P>, S : InfluenceSphere<P>> {

        /**
         * Equip the pedestrian with a sensorial sphere
         */
        fun attachSensorialSphere(sense: S): Builder<T, P, S>

        /**
         * Give relevance to the age of the pedestrian
         */
        fun specifyAge(age: Age): Builder<T, P, S>

        /**
         * Give relevance to the gender of the pedestrian
         */
        fun specifyGender(gender: Gender): Builder<T, P, S>

        /**
         * Create the pedestrian
         */
        fun build(): Pedestrian<T, P>
    }
}