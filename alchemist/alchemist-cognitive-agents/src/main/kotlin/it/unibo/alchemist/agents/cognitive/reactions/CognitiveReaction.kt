package it.unibo.alchemist.agents.cognitive.reactions

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian
import it.unibo.alchemist.characteristics.cognitive.CognitiveCharacteristic
import it.unibo.alchemist.model.implementations.reactions.AbstractReaction
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Environment

class CognitiveReaction<T>(
    ped: CognitivePedestrian<T>,
    private val characteristic: CognitiveCharacteristic,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(ped, timeDistribution) {

    override fun cloneOnNewNode(n: Node<T>?, currentTime: Time?) = TODO()

    override fun getRate() = timeDistribution.rate

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) =
        characteristic.update(rate)
}