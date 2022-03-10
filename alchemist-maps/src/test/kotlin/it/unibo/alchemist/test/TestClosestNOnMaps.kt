package it.unibo.alchemist.test
import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ClosestN
import it.unibo.alchemist.model.implementations.nodes.GenericNode
import it.unibo.alchemist.model.interfaces.GeoPosition

class TestClosestNOnMaps : StringSpec({
    "Use ClosestN on maps" {
        val environment = OSMEnvironment(
            SupportedIncarnations.get<Any, GeoPosition>("protelis").orElseGet { TODO() },
            "maps/cesena.pbf"
        )
        environment.linkingRule = ClosestN(10)
        environment.addNode(
            object : GenericNode<Any>(environment) {
                override fun createT() = "Nothing"
            },
            environment.makePosition(44.139169, 12.237816)
        )
    }
})
