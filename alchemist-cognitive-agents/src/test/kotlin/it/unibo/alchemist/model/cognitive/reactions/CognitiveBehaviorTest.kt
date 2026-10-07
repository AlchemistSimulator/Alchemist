/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.cognitive.CognitiveModel
import it.unibo.alchemist.model.cognitive.properties.CognitiveProperty
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test

class CognitiveBehaviorTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    @Test
    fun `the cognitive model advances only when the reaction fires`() {
        val model = mockk<CognitiveModel>(relaxed = true)
        val node = GenericNode(environment)
        node.addProperty(mockk<CognitiveProperty<Any>> { every { cognitiveModel } returns model })
        environment.addNode(node, environment.makePosition(0, 0))
        val behavior = CognitiveBehavior<Any, Euclidean2DPosition, Euclidean2DTransformation>(node, DiracComb(1.0))
        val validity = observe(true)
        behavior.conditions = listOf(
            mockk<Condition<Any>>(relaxed = true) {
                every { reaction } returns behavior
                every { isValid } returns validity
            },
        )
        behavior.initializationComplete(Time.ZERO, environment)
        validity.current = false
        validity.current = true
        verify(exactly = 0) { model.update(any()) }
        behavior.execute()
        verify(exactly = 1) { model.update(any()) }
    }
}
