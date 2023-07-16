/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.Group
import it.unibo.alchemist.model.cognitive.SocialProperty
import it.unibo.alchemist.model.cognitive.groups.Alone
import it.unibo.alchemist.model.cognitive.groups.GenericGroup
import it.unibo.alchemist.model.properties.AbstractNodeProperty

/**
 * Base implementation of a [SocialProperty].
 */
data class Social<T> @JvmOverloads constructor(
    override val node: Node<T>,
    override val group: Group<T> = Alone(node),
) : AbstractNodeProperty<T>(node), SocialProperty<T> {

    override fun cloneOnNewNode(node: Node<T>) = Social(node, GenericGroup(listOf(node)))

    override fun toString(): String = "${super.toString()}$group"
}
