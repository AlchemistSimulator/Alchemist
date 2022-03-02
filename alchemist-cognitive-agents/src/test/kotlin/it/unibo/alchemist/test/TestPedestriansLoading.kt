package it.unibo.alchemist.test

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.testsupport.loadYamlSimulation
import it.unibo.alchemist.testsupport.startSimulation
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapabilityOrNull
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.SocialProperty

class TestPedestriansLoading<T, P, A> : StringSpec({

    val filterSocialNode: (Node<T>) -> Boolean = { it.asCapabilityOrNull<T, SocialProperty<T>>() != null }

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
            onceInitialized = { e ->
                e.nodes.filter(filterSocialNode).forEach {
                    println("${it.id} -> ${ it.asCapability<T, SocialProperty<T>>().group }")
                }
            }
        )
    }
}) where P : Position<P>, P : Vector<P>, A : GeometricTransformation<P>
