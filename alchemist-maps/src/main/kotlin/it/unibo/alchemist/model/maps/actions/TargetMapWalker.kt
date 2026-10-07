/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.RoutingService
import it.unibo.alchemist.model.RoutingServiceOptions
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.movestrategies.routing.OnStreets
import it.unibo.alchemist.model.maps.movestrategies.target.FollowTargetOnMap
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.movestrategies.speed.InteractWithOthers

/**
 * Walks on the streets of the map towards the target stored in a molecule of the node.
 *
 * The target is read from `trackMolecule` whenever a new target is needed. A [GeoPosition] is used as-is; for an
 * [Iterable], its first two values, as numbers or numeric strings, become the coordinates; any other value is
 * converted to a string and parsed for numbers. The rate of the owning reaction determines the distance walked at
 * each step. If `interaction` is positive and `interactingMolecule` is not null, nodes within `range` that contain
 * `interactingMolecule` slow this node down, the more the higher `interaction` is.
 *
 * @param T concentration type
 * @param O [RoutingServiceOptions] type
 * @param S [RoutingService] type
 */
open class TargetMapWalker<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>>
@JvmOverloads
constructor(
    environment: MapEnvironment<T, O, S>,
    reaction: NodeReaction<T>,
    trackMolecule: Molecule,
    interactingMolecule: Molecule?,
    speed: Double = DEFAULT_SPEED,
    interaction: Double = DEFAULT_INTERACTION,
    range: Double = DEFAULT_RANGE,
) : MoveOnMap<T, O, S>(
    environment,
    reaction,
    OnStreets(environment, environment.routingService.defaultOptions),
    if (interaction <= 0 || interactingMolecule == null) {
        ConstantSpeed(reaction, speed)
    } else {
        InteractWithOthers(environment, reaction.host, reaction, interactingMolecule, speed, range, interaction)
    },
    FollowTargetOnMap(environment, reaction.host, trackMolecule),
) {
    /**
     * Builds the walker from molecule names; a null [interactingMolecule] disables interactions.
     */
    constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        trackMolecule: String,
        interactingMolecule: String?,
        speed: Double,
        interaction: Double,
        range: Double,
    ) : this(
        environment,
        reaction,
        SimpleMolecule(trackMolecule),
        interactingMolecule?.let(::SimpleMolecule),
        speed,
        interaction,
        range,
    )

    /**
     * Builds a walker without interactions from the name of the tracked molecule.
     */
    @JvmOverloads
    constructor(
        environment: MapEnvironment<T, O, S>,
        reaction: NodeReaction<T>,
        trackMolecule: String,
        speed: Double = DEFAULT_SPEED,
    ) : this(environment, reaction, trackMolecule, null, speed, DEFAULT_INTERACTION, DEFAULT_RANGE)

    /**
     * Default values of the walker.
     */
    companion object {
        /**
         * Default speed in meters per second.
         */
        const val DEFAULT_SPEED = 1.5

        /**
         * Default interaction range.
         */
        const val DEFAULT_RANGE = 0.0

        /**
         * Default interaction factor.
         */
        const val DEFAULT_INTERACTION = 0.0
    }
}
