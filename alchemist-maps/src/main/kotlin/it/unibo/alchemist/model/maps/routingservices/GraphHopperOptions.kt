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
import com.graphhopper.routing.weighting.Weighting
import com.graphhopper.util.CustomModel
import com.graphhopper.util.GHUtility
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
 * GraphHopper expects a valid [profile] (including information about vehicle and graph-weighting strategy),
 * a [vehicleClass] defining which roads are accessible by the vehicle,
 * and the selction of an [algorithm].
 */
class GraphHopperOptions private constructor(
    val profile: Profile,
    val vehicleClass: String,
    val algorithm: String,
) : RoutingServiceOptions<GraphHopperOptions> {
    private constructor(customModel: GraphHopperCustomModel, algorithm: String) : this(
        customModel.profile,
        customModel.vehicleClass,
        algorithm.takeIf { it in graphHopperAlgorithms } ?: throw IllegalArgumentException(
            "Invalid GraphHopper algorithm. Valid choices are: $graphHopperAlgorithms",
        ),
    )

    private constructor(profile: String, algorithm: String) : this(
        graphHopperCustomModels.find { it.name == profile } ?: throw IllegalArgumentException(
            "Invalid GraphHopper profile. Valid profiles are: ${graphHopperCustomModels.map { it.name }}",
        ),
        algorithm,
    )

    private constructor(info: Pair<String, String>) : this(info.first, info.second)

    private data class GraphHopperCustomModel(
        val name: String,
        val vehicleClass: String,
    ) {
        val customModel: CustomModel by lazy { GHUtility.loadCustomModelFromJar("$name.json") }
        val profile: Profile by lazy {
            Profile(name).setWeighting("custom").setCustomModel(customModel)
        }
    }

    /**
     * Utilities for GraphHopper routing.
     */
    companion object {
        private val profiles: LoadingCache<Pair<String, String>, GraphHopperOptions> =
            Caffeine.newBuilder()
                .build(::GraphHopperOptions)

        /**
         * All the non-abstract subclasses of [RoutingAlgorithm] available in the runtime.
         */
        val graphHopperAlgorithms: List<String> =
            listOf(
                ALT_ROUTE,
                ASTAR,
                ASTAR_BI,
                DIJKSTRA,
                DIJKSTRA_BI,
                DIJKSTRA_ONE_TO_MANY,
                ROUND_TRIP,
            )

        /**
         * All the non-abstract subclasses of [Weighting] available in the runtime.
         */
        private val graphHopperCustomModels: List<GraphHopperCustomModel> =
            listOf(
                "bike" to "bike",
                "bus" to "bus",
                "car" to "car",
                "car4wd" to "car",
                "foot" to "foot",
                "hike" to "foot",
                "motorcycle" to "car",
                "mtb" to "mtb",
                "racingbike" to "racingbike",
                "truck" to "car",
            ).map { (name, vehicleClass) -> GraphHopperCustomModel(name, vehicleClass) }

        /**
         * All the available [Profile]s in the runtime.
         */
        val allProfiles: List<Profile> = graphHopperCustomModels.map { it.profile }

        /**
         * All the available [Profile]s in the runtime.
         */
        val allCustomModels: List<CustomModel> = graphHopperCustomModels.map { it.customModel }

        /**
         * Default [GraphHopperOptions]: foot as vehicle, fastest as weighting, and dijkstrabi as algorithm.
         */
        val defaultOptions: GraphHopperOptions

        init {
            fun error(subject: String) =
                "Unable to find any valid GraphHopper $subject. " +
                    "This is most likely due to using an unsupported version of GraphHopper"
            require(graphHopperAlgorithms.isNotEmpty()) { error("algorithm") }
            require(graphHopperCustomModels.isNotEmpty()) { error("custom model") }
            defaultOptions = optionsFor()
        }

        /**
         * Retrieves or creates the set of options for the required [vehicle] (default: foot),
         * [weighting] (default: fastest), and [algorithm] (default: dijstrabi).
         */
        fun optionsFor(
            vehicle: String = "foot",
            weighting: String = "fastest",
            algorithm: String = DIJKSTRA_BI,
        ) = optionsFor(profile = "${vehicle}_$weighting", algorithm)

        /**
         * Retrieves or creates the set of options for the required [profile] (default: foot_fastest)
         * and [algorithm] (default: dijstrabi).
         */
        @JvmOverloads
        fun optionsFor(
            profile: String = "foot",
            algorithm: String = DIJKSTRA_BI,
        ): GraphHopperOptions {
            return profiles.get(profile to algorithm) ?: throw IllegalArgumentException(
                "The requested profile ($profile, $algorithm) could not be created.",
            )
        }
    }
}
