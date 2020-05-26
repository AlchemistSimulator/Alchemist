package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * A generic, leaderless group of pedestrians.
 */
class Friends<T> : GenericGroup<T, Pedestrian<T>>()
