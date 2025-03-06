/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.fail

class TestPedestriansLoading<T, P, A> where P : Position<P>, P : Vector<P>, A : Transformation<P> {
    @Test
    fun `Homogeneous pedestrians loading`() {
        loadYamlSimulation<T, P>("homogeneous-pedestrians.yml").startSimulation()
    }

    @Test
    fun `Heterogeneous pedestrians loading`() {
        loadYamlSimulation<T, P>("heterogeneous-pedestrians.yml").startSimulation()
    }

    @Test
    fun `Cognitive pedestrians loading`() {
        loadYamlSimulation<T, P>("cognitive-pedestrians.yml").startSimulation()
    }

    @Test
    fun `Can't give non-cognitive pedestrians cognitive characteristics`() {
        assertFailsWith<RuntimeException>("An heterogeneous pedestrian can't have cognitive capabilities") {
            loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml").startSimulation()
            fail("An exception was expected but not thrown")
        }
    }

    @Test
    fun `Groups of pedestrians loading`() {
        loadYamlSimulation<T, P>("groups.yml").startSimulation(
            onceInitialized = { environment ->
                environment.nodes.forEach { node ->
                    assertNotNull(
                        node.asPropertyOrNull<T, SocialProperty<T>>(),
                        "Each pedestrian should have a social property",
                    )
                }
            },
        )
    }
}
