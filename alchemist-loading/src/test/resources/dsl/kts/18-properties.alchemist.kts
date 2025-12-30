import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation

val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
    deployments {
        deploy(
            circle(
                1000,
                0.0,
                0.0,
                15.0,
            ),
        ) {
            properties {
                val filter = RectangleFilter(-3.0, -3.0, 2.0, 2.0)
                val filter2 = RectangleFilter(3.0, 3.0, 2.0, 2.0)
                inside(filter) {
                    +testNodeProperty("a")
                }
                // otherwise
                inside(filter2) {
                    +testNodeProperty("b")
                }
            }
        }
    }
}

