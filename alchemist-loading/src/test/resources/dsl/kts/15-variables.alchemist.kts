/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts

simulation(SAPEREIncarnation<Euclidean2DPosition>()) {
    val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
    val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

    val mSize by variable { -size }
    val sourceStart by variable { mSize / 10.0 }
    val sourceSize by variable { size / 5.0 }
    terminators { +AfterTime<List<ILsaMolecule>, Euclidean2DPosition>(DoubleTime(1.0)) }
    networkModel = ConnectWithinDistance(0.5)
    deployments {
        deploy(
            grid(
                mSize,
                mSize,
                size,
                size,
                0.25,
                0.25,
                0.1,
                0.1,
            ),
        ) {
            inside(Rectangle<Euclidean2DPosition>(sourceStart, sourceStart, sourceSize, sourceSize)) {
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
