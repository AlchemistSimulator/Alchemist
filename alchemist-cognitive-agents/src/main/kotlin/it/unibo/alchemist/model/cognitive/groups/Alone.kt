/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.groups

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.Group

/**
 * Group representing a node alone.
 */
class Alone<T>(node: Node<T>) : Group<T>, MutableList<Node<T>> by mutableListOf(node)
