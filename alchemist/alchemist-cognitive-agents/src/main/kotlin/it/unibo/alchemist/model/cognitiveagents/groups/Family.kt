package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.implementations.nodes.CognitivePedestrian2D
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

class Family<T, P : Position2D<P>>(
    env: Environment<T, P>,
    rg: RandomGenerator,
    numChildren: Int
) : AbstractGroup<T>(setOf(
        CognitivePedestrian2D(env, rg, "adult", "male"), // father
        CognitivePedestrian2D(env, rg, "adult", "female"), // mother
    *(0..numChildren).map {
        CognitivePedestrian2D(env, rg, "child", if (rg.nextDouble() < 0.5) "male" else "female")
    }.toTypedArray()
))
