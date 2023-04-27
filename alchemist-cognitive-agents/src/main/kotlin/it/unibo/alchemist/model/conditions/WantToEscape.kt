/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEscape<T, S : Vector<S>, A : Transformation<S>>(
    node: Node<T>,
) : AbstractCondition<T>(node) {

    override fun getContext(): Context = Context.LOCAL

    override fun getPropensityContribution(): Double = 0.0

    override fun isValid(): Boolean =
        node.asProperty<T, CognitiveProperty<T>>().cognitiveModel.wantsToEscape()
}
