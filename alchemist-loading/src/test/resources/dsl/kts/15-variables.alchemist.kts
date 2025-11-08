import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation

val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
    val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
    val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

    val mSize by variable { -size }
    val sourceStart by variable { mSize / 10.0 }
    val sourceSize by variable { size / 5.0 }

    networkModel = ConnectWithinDistance(0.5)
    deployments {
        deploy(
            grid(
                mSize, mSize, size, size,
                0.25, 0.25, 0.1, 0.1,
            ),
        ) {
            inside(RectangleFilter(sourceStart, sourceStart, sourceSize, sourceSize)) {
                molecule = "token, 0, []"
            }
            programs {
                all {
                    timeDistribution(rate.toString())
                    program = "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}"
                }
                all {
                    program = "{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}"
                }
            }
        }
    }
}


