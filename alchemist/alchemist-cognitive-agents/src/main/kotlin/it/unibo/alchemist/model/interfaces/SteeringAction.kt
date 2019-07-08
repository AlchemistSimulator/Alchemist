package it.unibo.alchemist.model.interfaces

interface SteeringAction<T, P : Position<P>> : Action<T> {

    fun getNextPosition(): P

    fun target(): P
}