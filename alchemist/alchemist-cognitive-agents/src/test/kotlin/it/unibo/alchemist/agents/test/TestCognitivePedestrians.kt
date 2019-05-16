package it.unibo.alchemist.agents.test

import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.loader.YamlLoader
import org.jooq.lambda.Unchecked
import org.junit.Assert
import org.kaikikm.threadresloader.ResourceLoader

class TestCognitivePedestrians<T, P : Position<P>> : StringSpec({

    "homogeneous pedestrian loading" {
        TestUtils.loadYamlSimulation<T, P>("homogeneouspedestrians.yml")
    }

    "heterogeneous pedestrian loading" {
        TestUtils.loadYamlSimulation<T, P>("heterogeneouspedestrians.yml")
    }

    "cognitive pedestrian loading" {
        TestUtils.loadYamlSimulation<T, P>("cognitivepedestrians.yml")
    }
})