/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.routingservices

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.graphhopper.config.Profile
import com.graphhopper.routing.RoutingAlgorithm
import com.graphhopper.routing.util.VehicleEncodedValuesFactory
import com.graphhopper.routing.weighting.Weighting
import com.graphhopper.util.CustomModel
import com.graphhopper.util.Parameters.Algorithms.ALT_ROUTE
import com.graphhopper.util.Parameters.Algorithms.ASTAR
import com.graphhopper.util.Parameters.Algorithms.ASTAR_BI
import com.graphhopper.util.Parameters.Algorithms.DIJKSTRA
import com.graphhopper.util.Parameters.Algorithms.DIJKSTRA_BI
import com.graphhopper.util.Parameters.Algorithms.DIJKSTRA_ONE_TO_MANY
import com.graphhopper.util.Parameters.Algorithms.ROUND_TRIP
import it.unibo.alchemist.model.RoutingServiceOptions

/**
 * Available configuration options for routing through GraphHopper.
 * GraphHopper expects a valid [profile] (including information about vehicle and graph-weighting strategy) and
 * the selction of an [algorithm].
 */
class GraphHopperOptions private constructor(
    val profile: Profile,
    val algorithm: String,
) : RoutingServiceOptions<GraphHopperOptions> {

    private constructor(profile: String, algorithm: String) : this(
        allProfiles.find { it.name == profile } ?: throw IllegalArgumentException(
            "Invalid GraphHopper profile. Valid profiles are: ${allProfiles.map { it.name }}",
        ),
        algorithm.takeIf { it in graphHopperAlgorithms } ?: throw IllegalArgumentException(
            "Invalid GraphHopper algorithm. Valid choices are: $graphHopperAlgorithms",
        ),
    )

    private constructor(info: Pair<String, String>) : this(info.first, info.second)

    /**
     * The GraphHopper weights to use.
     * [distanceInfluence] is the number of seconds per km to penalize longer routes with.
     */
    private data class GraphHopperWeighting(
        val name: String,
        val distanceInfluence: Double, // seconds per km to penalize longer routes with
    ) {
        val customModel: CustomModel = CustomModel().setDistanceInfluence(distanceInfluence)
    }

    companion object {

        private val profiles: LoadingCache<Pair<String, String>, GraphHopperOptions> = Caffeine.newBuilder()
            .build(::GraphHopperOptions)

        /**
         * All the non-abstract subclasses of [RoutingAlgorithm] available in the runtime.
         */
        val graphHopperAlgorithms: List<String> = listOf(
            ALT_ROUTE,
            ASTAR,
            ASTAR_BI,
            DIJKSTRA,
            DIJKSTRA_BI,
            DIJKSTRA_ONE_TO_MANY,
            ROUND_TRIP,
        )

        /**
         * All the non-abstract subclasses of [VehicleEncodedValuesFactory] available in the runtime.
         */
        val graphHopperVehicles: List<String> = listOf(
            VehicleEncodedValuesFactory.BIKE,
            VehicleEncodedValuesFactory.CAR,
            VehicleEncodedValuesFactory.FOOT,
            VehicleEncodedValuesFactory.MOUNTAINBIKE,
            VehicleEncodedValuesFactory.RACINGBIKE,
            VehicleEncodedValuesFactory.ROADS,
            VehicleEncodedValuesFactory.WHEELCHAIR,
        )

        /**
         * All the non-abstract subclasses of [Weighting] available in the runtime.
         */
        private val graphHopperWeightings: List<GraphHopperWeighting> = listOf(
            GraphHopperWeighting("fastest", 0.0),
            GraphHopperWeighting("short_fastest", 30.0),
            GraphHopperWeighting("shortest", 3600.0),
        )

        /**
         * All the available profiles for GraphHopper navigation.
         */
        val allProfiles: List<Profile> = graphHopperVehicles
            .flatMap { vehicle -> graphHopperWeightings.map { weighting -> profileFor(vehicle, weighting) } }

        /**
         * Default [GraphHopperOptions]: foot as vehicle, fastest as weighting, and dijkstrabi as algorithm.
         */
        val defaultOptions: GraphHopperOptions

        init {
            fun error(subject: String) = "Unable to find any valid GraphHopper $subject. " +
                "This is most likely due to using an unsupported version of GraphHopper"
            require(graphHopperAlgorithms.isNotEmpty()) { error("algorithm") }
            require(graphHopperVehicles.isNotEmpty()) { error("vehicle") }
            require(graphHopperWeightings.isNotEmpty()) { error("weighting") }
            defaultOptions = optionsFor()
        }

        /**
         * Retrieves or creates the set of options for the required [vehicle] (default: foot),
         * [weighting] (default: fastest), and [algorithm] (default: dijstrabi).
         */
        fun optionsFor(vehicle: String = "foot", weighting: String = "fastest", algorithm: String = DIJKSTRA_BI) =
            optionsFor(profile = "${vehicle}_$weighting", algorithm)

        /**
         * Retrieves or creates the set of options for the required [profile] (default: foot_fastest)
         * and [algorithm] (default: dijstrabi).
         */
        @JvmOverloads
        fun optionsFor(profile: String = "foot_fastest", algorithm: String = DIJKSTRA_BI): GraphHopperOptions {
            return profiles.get(profile to algorithm) ?: throw IllegalArgumentException(
                "The requested profile ($profile, $algorithm) could not be created.",
            )
        }

        private fun profileFor(
            vehicle: String,
            weighting: GraphHopperWeighting,
        ): Profile = Profile("${vehicle}_${weighting.name}")
            .setVehicle(vehicle)
            .setWeighting("custom")
            .setCustomModel(weighting.customModel)
    }
}
