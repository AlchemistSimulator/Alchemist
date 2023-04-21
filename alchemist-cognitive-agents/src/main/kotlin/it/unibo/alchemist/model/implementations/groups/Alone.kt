package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.interfaces.Group
/**
 * Group representing a node alone.
 */
class Alone<T>(node: Node<T>) : Group<T>, MutableList<Node<T>> by mutableListOf(node)
