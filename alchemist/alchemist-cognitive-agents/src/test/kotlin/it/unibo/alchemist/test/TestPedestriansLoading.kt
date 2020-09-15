package it.unibo.alchemist.test

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import loadYamlSimulation
import startSimulation

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
        shouldThrow<IllegalArgumentException> {
            loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml").startSimulation()
            fail("An heterogeneous pedestrian can't have cognitive capabilities")
        }
    }

    "groups of pedestrians loading" {
        loadYamlSimulation<T, P>("groups.yml").startSimulation(
            initialized = { e ->
                e.nodes.filterIsInstance<Pedestrian<T, P, A>>().forEach {
                    println("${it.id} -> ${it.membershipGroup}")
                }
            }
        )
    }
}) where P : Position<P>, P : Vector<P>, A : GeometricTransformation<P>
