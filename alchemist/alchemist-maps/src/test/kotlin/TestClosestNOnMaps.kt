
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ClosestN
import it.unibo.alchemist.model.implementations.nodes.AbstractNode
import it.unibo.alchemist.model.interfaces.GeoPosition

class TestClosestNOnMaps : StringSpec({
    "Use ClosestN on maps" {
        val environment = OSMEnvironment<Any>("maps/cesena.pbf")
        environment.linkingRule = ClosestN<Any, GeoPosition>(10)
        environment.addNode(object : AbstractNode<Any>(environment) {
            override fun createT() = "Nothing"
        }, environment.makePosition(44.139169, 12.237816))
    }
})
