package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Node
/**
 * Group representing a node alone.
 */
class Alone<T>(node: Node<T>) : Group<T>, MutableList<Node<T>> by mutableListOf(node)
