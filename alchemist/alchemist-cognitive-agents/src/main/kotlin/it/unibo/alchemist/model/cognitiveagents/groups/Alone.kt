package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

class Alone<T>(pedestrian: Pedestrian<T>) : GenericGroup<T>(listOf(pedestrian))