package it.unibo.alchemist.test

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

class TestPedestriansLoading<T, P : Position<P>> : StringSpec({

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
        try {
            loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml").startSimulation()
            fail("An heterogeneous pedestrian can't have cognitive capabilities")
        } catch (exc: Throwable) { }
    }

    "groups of pedestrians loading" {
        loadYamlSimulation<T, P>("groups.yml").startSimulation(
            initialized = { e -> e.nodes.filterIsInstance<Pedestrian<T>>().forEach {
                println("${it.id} -> ${it.membershipGroup}")
            } }
        )
    }

    "orienting homogeneous pedestrians loading" {
        loadYamlSimulation<T, P>("orienting-homogeneous-pedestrians.yml").startSimulation()
    }

    "orienting cognitive pedestrians loading" {
        loadYamlSimulation<T, P>("orienting-cognitive-pedestrians.yml").startSimulation()
    }
})
