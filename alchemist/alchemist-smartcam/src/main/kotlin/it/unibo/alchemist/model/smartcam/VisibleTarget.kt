package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import org.protelis.lang.datatype.impl.ArrayTupleImpl

/**
 * Represents an interesting object detected by the [See] action.
 */
data class VisibleTarget<T>(
    private val observer: Node<T>,
    private val seen: Node<T>,
    private val env: Environment<T, *>
) {

    /**
     * Identifier.
     */
    val id = seen.id
    /**
     * Target absolute position.
     */
    val position = with(env.getPosition(seen)) { ArrayTupleImpl(getCoordinate(0), getCoordinate(1)) }
    /**
     * Distance of the target from the observer.
     */
    val distance = env.getDistanceBetweenNodes(observer, seen)

    override fun equals(other: Any?): Boolean {
        if (other is VisibleTarget<*>) {
            return other.id == id
        }
        return false
    }

    override fun hashCode() =
        id.hashCode()

    override fun toString() =
        "Target#$id"
}