/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.test.loadYamlSimulation
import it.unibo.alchemist.test.startSimulation

class TestPedestriansLoading<T, P, A> : StringSpec({

    "homogeneous pedestrians loading" {
        loadYamlSimulation<T, P>("homogeneous-pedestrians.yml").startSimulation()
    }

    "heterogeneous pedestrians loading" {
        loadYamlSimulation<T, P>("heterogeneous-pedestrians.yml").startSimulation()
    }

    "cognitive pedestrians loading" {
        loadYamlSimulation<T, P>("cognitive-pedestrians.yml").startSimulation()
    }

    "can't give non-cognitive pedestrians cognitive characteristics" {
        shouldThrow<RuntimeException> {
            loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml").startSimulation()
            fail("An heterogeneous pedestrian can't have cognitive capabilities")
        }
    }

    "groups of pedestrians loading" {
        loadYamlSimulation<T, P>("groups.yml").startSimulation(
            onceInitialized = { environment ->
                environment.nodes.forEach { it.asPropertyOrNull<T, SocialProperty<T>>().shouldNotBeNull() }
            },
        )
    }
}) where P : Position<P>, P : Vector<P>, A : Transformation<P>
