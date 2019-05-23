package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian2D
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import org.apache.commons.math3.random.RandomGenerator

class Family<T, P : Position2D<P>>(
    env: Environment<T, P>,
    rg: RandomGenerator
) : AbstractGroup<T>(setOf(
    CognitivePedestrian2D(env, rg, "adult", "male"),
    CognitivePedestrian2D(env, rg, "adult", "female"),
    CognitivePedestrian2D(env, rg, "child", "female"),
    CognitivePedestrian2D(env, rg, "child", "male")
))
