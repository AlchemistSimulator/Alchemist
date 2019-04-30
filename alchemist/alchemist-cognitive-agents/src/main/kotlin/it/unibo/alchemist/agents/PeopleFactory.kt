package it.unibo.alchemist.agents

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.sensory.FieldOfView2D
import it.unibo.alchemist.sensory.HearingField2D
import kotlin.random.Random

const val DEFAULT_SIGHT_DISTANCE = 10.0
const val DEFAULT_SIGHT_APERTURE = 120.0
const val DEFAULT_HEARING_RADIUS = 5.0

/**
 * Factory for the creation of pedestrians with different capabilities
 */
class PeopleFactory {

    companion object BiDimensional {

        private fun <P : Position2D<P>> P.default2DFoV(): FieldOfView2D<P> =
                FieldOfView2D(this.x, this.y,
                        Random.nextDouble(0.0, 360.0),
                        DEFAULT_SIGHT_APERTURE,
                        DEFAULT_SIGHT_DISTANCE)

        private fun <P : Position2D<P>> P.default2DHearingField(): HearingField2D<P> =
                HearingField2D(this.x, this.y, DEFAULT_HEARING_RADIUS)

        /**
         * Pedestrian without any sensorial sphere
         */
        fun <P : Position2D<P>> dummy(env: Environment<*, P>, pos: P) =
                Pedestrian2D.Builder(env, pos).build()

        /**
         * Pedestrian only capable of seeing
         */
        fun <P : Position2D<P>> deaf(env: Environment<*, P>, pos: P) =
                Pedestrian2D.Builder(env, pos)
                        .attachSensorialSphere(pos.default2DFoV())
                        .build()

        /**
         * Pedestrian only capable of listening
         */
        fun <P : Position2D<P>> blind(env: Environment<*, P>, pos: P) =
                Pedestrian2D.Builder(env, pos)
                        .attachSensorialSphere(pos.default2DHearingField())
                        .build()

        /**
         * Pedestrian capable of seeing and listening
         */
        fun <P : Position2D<P>> normal(env: Environment<*, P>, pos: P) =
                Pedestrian2D.Builder(env, pos)
                        .attachSensorialSphere(pos.default2DFoV())
                        .attachSensorialSphere(pos.default2DHearingField())
                        .build()
    }
}