package it.unibo.alchemist.implementations

import it.unibo.alchemist.model.implementations.nodes.ProtelisNode
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.Position2D

@Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class Camera<P : Position2D<P>>(
  private val env: PhysicsEnvironment<Object, P>
) : ProtelisNode(env) {

    fun move(pos: P) {
        env.moveNodeToPosition(this as Node<Object>, pos)
    }

    fun say(m:String) {
        println("[$id] $m")
    }


}