package it.unibo.alchemist.test

import io.kotlintest.fail
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.Position

class TestPedestriansLoading<T, P : Position<P>> : StringSpec({

    "homogeneous pedestrians loading" {
        loadYamlSimulation<T, P>("homogeneous-pedestrians.yml")
    }

    "heterogeneous pedestrians loading" {
        loadYamlSimulation<T, P>("heterogeneous-pedestrians.yml")
    }

    "cognitive pedestrians loading" {
        loadYamlSimulation<T, P>("cognitive-pedestrians.yml")
    }

    "can't give non-cognitive pedestrians cognitive characteristics" {
        try {
            loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml")
            fail("An heterogeneous pedestrian can't have cognitive capabilities")
        } catch (exc: Throwable) { }
    }
})