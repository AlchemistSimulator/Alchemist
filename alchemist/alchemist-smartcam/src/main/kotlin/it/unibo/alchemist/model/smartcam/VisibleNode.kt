package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import org.protelis.lang.datatype.impl.ArrayTupleImpl

/**
 * Represents a node seen by the [See] action.
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
     * Absolute position.
     */
    val position = with(env.getPosition(seen)) { ArrayTupleImpl(getCoordinate(0), getCoordinate(1)) }

    override fun equals(other: Any?): Boolean {
        if (other is VisibleTarget<*>) {
            return other.id == id
        }
        return false
    }

    override fun hashCode() =
        id.hashCode()

    override fun toString() =
        "Visible#$id"
}