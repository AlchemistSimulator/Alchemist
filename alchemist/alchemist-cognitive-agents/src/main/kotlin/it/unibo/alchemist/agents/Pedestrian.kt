package it.unibo.alchemist.agents

import it.unibo.alchemist.characteristics.interfaces.Characteristic
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.sensory.InfluenceSphere

/**
 * A pedestrian is an agent with cognitive capabilities
 */
interface Pedestrian<P: Position<P>> : Node<Collection<Characteristic>> {

    /**
     * A builder of pedestrians
     */
    interface Builder<P: Position<P>, S: InfluenceSphere<P>> {

        /**
         * Equip the pedestrian with a sensorial sphere
         */
        fun attachSensorialSphere(sense: S): Builder<P, S>

        /**
         * Create the pedestrian
         */
        fun build(): Pedestrian<P>

    }

}