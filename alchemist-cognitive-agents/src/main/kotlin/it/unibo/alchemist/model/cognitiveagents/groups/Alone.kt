package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitiveagents.Group
/**
 * Group representing a node alone.
 */
class Alone<T>(node: Node<T>) : Group<T>, MutableList<Node<T>> by mutableListOf(node)
