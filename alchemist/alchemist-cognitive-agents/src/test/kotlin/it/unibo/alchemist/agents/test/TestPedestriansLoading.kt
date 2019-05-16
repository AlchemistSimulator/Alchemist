package it.unibo.alchemist.agents.test

import io.kotlintest.fail
import io.kotlintest.should
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.interfaces.Position

class TestPedestriansLoading<T, P : Position<P>> : StringSpec({

    "homogeneous pedestrians loading" {
        TestUtils.loadYamlSimulation<T, P>("homogeneous-pedestrians.yml")
    }

    "heterogeneous pedestrians loading" {
        TestUtils.loadYamlSimulation<T, P>("heterogeneous-pedestrians.yml")
    }

    "cognitive pedestrians loading" {
        TestUtils.loadYamlSimulation<T, P>("cognitive-pedestrians.yml")
    }

    "can't give non-cognitive pedestrians cognitive characteristics" {
        try {
            TestUtils.loadYamlSimulation<T, P>("cant-give-cognitive-to-heterogeneous.yml")
            fail("An heterogeneous pedestrian can't have cognitive capabilities")
        } catch(exc: IllegalArgumentException) { }
    }
})