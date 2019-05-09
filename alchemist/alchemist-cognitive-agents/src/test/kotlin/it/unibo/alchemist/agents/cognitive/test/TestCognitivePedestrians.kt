package it.unibo.alchemist.agents.cognitive.test

import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.loader.YamlLoader
import org.jooq.lambda.Unchecked
import org.junit.Assert
import org.kaikikm.threadresloader.ResourceLoader

class TestCognitivePedestrians<T, P : Position<P>> : StringSpec({

    "cognitive pedestrian loading" {
        loadYamlSimulation<T, P>("cognitivepedestrians.yml", mapOf())
    }
})

private fun <T, P : Position<P>> loadYamlSimulation(resource: String, vars: Map<String, Double>) {
    val res = ResourceLoader.getResourceAsStream(resource)
    Assert.assertNotNull("Missing test resource $resource", res)
    val env = YamlLoader(res).getWith<T, P>(vars)
    val sim = Engine<T, P>(env, 10000)
    sim.play()
    sim.run()
    sim.error.ifPresent(Unchecked.consumer<Throwable> { e -> throw e })
}