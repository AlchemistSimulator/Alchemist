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
import it.unibo.alchemist.model.cognitive.GroupWithLeader

/**
 * A [Family] is modeled as a group of pedestrians with a leader.
 */
class Family<T>(
    comparator: Comparator<Node<T>> = Comparator { a, b -> a.id.compareTo(b.id) },
) : GenericGroup<T, Node<T>>(),
    GroupWithLeader<T, Node<T>> {

    override val leader: Node<T> = checkNotNull(members.minWithOrNull(comparator)) {
        "Can't determine a leader."
    }
}
