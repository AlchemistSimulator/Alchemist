package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.agents.cognitive.CognitivePedestrian2D
import it.unibo.alchemist.characteristics.individual.Age
import it.unibo.alchemist.characteristics.individual.Gender
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D

class MaleAdult<T, P : Position2D<P>>(env: Environment<T, P>)
    : CognitivePedestrian2D<T, P>(env, Age.ADULT, Gender.MALE)