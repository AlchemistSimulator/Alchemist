package it.unibo.alchemist.agents.cognitive

import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.TimeDistribution

class MaleAdult<T, P : Position2D<P>>(env: Environment<T, P>, timeDistribution: TimeDistribution<T>)
    : CognitivePedestrian2D<T, P>(env, timeDistribution, Age.ADULT, Gender.MALE)