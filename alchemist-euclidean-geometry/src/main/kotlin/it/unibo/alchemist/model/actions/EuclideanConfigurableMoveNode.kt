/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.movestrategies.speed.GloballyConstantSpeed

/**
 * An [AbstractConfigurableMoveNode] specialized for Euclidean spaces.
 *
 * This class provides a default [interpolatePositions] implementation that
 * moves the node toward a target without exceeding a provided maximum step length.
 *
 * @param T the concentration type.
 * @param P the [Position] type used for spatial coordinates and vectors.
 * @param environment the [Environment] executing the simulation.
 * @param node the [Node] executing this [Action].
 * @param routingStrategy the [RoutingStrategy] selected for this action.
 * @param targetSelectionStrategy the [TargetSelectionStrategy] selected for this action.
 * @param speedSelectionStrategy the [SpeedSelectionStrategy] selected for this action.
 */
open class EuclideanConfigurableMoveNode<T, P>(
    environment: Environment<T, P>,
    node: Node<T>,
    routingStrategy: RoutingStrategy<T, P>,
    targetSelectionStrategy: TargetSelectionStrategy<T, P>,
    speedSelectionStrategy: SpeedSelectionStrategy<T, P>,
) : AbstractConfigurableMoveNode<T, P>(
    environment,
    node,
    routingStrategy,
    targetSelectionStrategy,
    speedSelectionStrategy,
) where P : Position<P>, P : Vector<P> {
    /**
     * Secondary constructor that uses a [GloballyConstantSpeed].
     *
     * @param environment the [Environment] executing the simulation.
     * @param node the [Node] executing this [Action].
     * @param reaction the reaction executing this [Action].
     * @param routingStrategy the [RoutingStrategy] selected for this action.
     * @param targetSelectionStrategy the [TargetSelectionStrategy] selected for this action.
     * @param speed the maximum speed used to create a [GloballyConstantSpeed].
     * @return an [AbstractConfigurableMoveNode] implementation using [GloballyConstantSpeed].
     */
    constructor(
        environment: Environment<T, P>,
        node: Node<T>,
        reaction: Reaction<T>,
        routingStrategy: RoutingStrategy<T, P>,
        targetSelectionStrategy: TargetSelectionStrategy<T, P>,
        speed: Double,
    ) : this (
        environment,
        node,
        routingStrategy,
        targetSelectionStrategy,
        GloballyConstantSpeed(reaction, speed),
    )

    /**
     * Computes the next position toward [target], constrained by [maxWalk].
     *
     * @param current the current position.
     * @param target the target position to reach.
     * @param maxWalk the maximum distance that can be traveled in this step.
     * @return the next relative position as a [P]. If [maxWalk] is greater than the distance
     *         to the [target], the returned position is exactly [target].
     */
    override fun interpolatePositions(current: P, target: P, maxWalk: Double): P =
        (target - current).coerceAtMost(maxWalk)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): Action<T> = EuclideanConfigurableMoveNode(
        environment,
        node,
        routingStrategy,
        targetSelectionStrategy,
        speedSelectionStrategy,
    )
}
