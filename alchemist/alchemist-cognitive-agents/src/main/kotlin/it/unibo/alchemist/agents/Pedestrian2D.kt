package it.unibo.alchemist.agents

import it.unibo.alchemist.characteristics.interfaces.Characteristic
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.Environment

import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.sensory.InfluenceSphere2D

/**
 * A pedestrian in the bidimensional world
 */
class Pedestrian2D<P: Position2D<P>> private constructor(env: Environment<Collection<Characteristic>, P>,
                                                         pos: P,
                                                         senses: Collection<InfluenceSphere2D<P>>)
    : Pedestrian<P>, AbstractNode<Collection<Characteristic>>(env) {

    override fun createT(): Collection<Characteristic> = listOf()

    /**
     * A builder of bidimensional pedestrians
     */
    class Builder<P: Position2D<P>>(val env: Environment<Collection<Characteristic>, P>,
                                    val pos: P,
                                    val senses: MutableCollection<InfluenceSphere2D<P>> = mutableSetOf())
        : Pedestrian.Builder<P, InfluenceSphere2D<P>> {

        override fun attachSensorialSphere(sense: InfluenceSphere2D<P>) = apply { senses.add(sense) }

        override fun build(): Pedestrian2D<P> = Pedestrian2D(env, pos, senses).also { env.addNode(it, pos) }

    }

}