/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.routingservices

import com.github.benmanes.caffeine.cache.Caffeine
import com.graphhopper.config.Profile
import com.graphhopper.routing.RoutingAlgorithm
import com.graphhopper.routing.util.FlagEncoder
import com.graphhopper.routing.weighting.Weighting
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.model.interfaces.RoutingServiceOptions

/**
 * Available configuration options for routing through GraphHopper.
 * GraphHopper expects a valid [profile] (including information about vehicle and graph-weighting strategy) and
 * the selction of an [algorithm].
 */
data class GraphHopperOptions private constructor(
    val profile: Profile,
    val algorithm: String,
) : RoutingServiceOptions<GraphHopperOptions> {

    private constructor(profile: String, algorithm: String) : this(
        allProfiles.find { it.name == profile } ?: throw IllegalArgumentException(
            "Invalid GraphHopper profile. Valid profiles are: ${allProfiles.map { it.name }}"
        ),
        algorithm.takeIf { it in graphHopperAlgorithms } ?: throw IllegalArgumentException(
            "Invalid GraphHopper algorithm. Valid choices are: $graphHopperAlgorithms"
        )
    )

    private constructor(info: Pair<String, String>) : this(info.first, info.second)

    companion object {
        /**
         * Default [GraphHopperOptions]: foot as vehicle, fastest as weighting, and dijkstrabi as algorithm.
         */
        val defaultOptions: GraphHopperOptions = optionsFor()

        /**
         * All the non-abstract subclasses of [RoutingAlgorithm] available in the runtime.
         */
        val graphHopperAlgorithms: List<String> =
            extractFromGraphHopper<RoutingAlgorithm>("util")

        /**
         * All the non-abstract subclasses of [FlagEncoder] available in the runtime.
         */
        val graphHopperVehicles: List<String> =
            extractFromGraphHopper<FlagEncoder>("util")

        /**
         * All the non-abstract subclasses of [Weighting] available in the runtime.
         */
        val graphHopperWeightings: List<String> =
            extractFromGraphHopper<Weighting>("weighting")

        /**
         * All the available profiles for GraphHopper navigation.
         */
        val allProfiles: List<Profile> = graphHopperVehicles.flatMap { vehicle ->
            graphHopperVehicles.map { weighting ->
                profileFor(vehicle, weighting)
            }
        }

        private val profiles = Caffeine.newBuilder()
            .build<Pair<String, String>, GraphHopperOptions>(::GraphHopperOptions)

        init {
            fun error(subject: String) = "Unable to find any valid GraphHopper $subject. " +
                "This is most likely due to using an unsupported version of GraphHopper"
            require(graphHopperVehicles.isNotEmpty()) {
                error("vehicle")
            }
            require(graphHopperWeightings.isNotEmpty()) {
                error("weighting")
            }
        }

        /**
         * Retrieves or creates the set of options for the required [vehicle] (default: foot),
         * [weighting] (default: fastest), and [algorithm] (default: dijstrabi).
         */
        fun optionsFor(vehicle: String = "foot", weighting: String = "fastest", algorithm: String = "dijkstrabi") =
            optionsFor(profile = "${vehicle}_$weighting", algorithm)

        /**
         * Retrieves or creates the set of options for the required [profile] (default: foot_fastest)
         * and [algorithm] (default: dijstrabi).
         */
        @JvmOverloads fun optionsFor(
            profile: String = "foot_fastest",
            algorithm: String = "dijkstrabi"
        ): GraphHopperOptions =
            profiles[profile to algorithm] ?: throw IllegalArgumentException(
                "The requested profile ($profile, $algorithm) could not be created."
            )

        private inline fun <reified T : Any> extractFromGraphHopper(subPackage: String) =
            ClassPathScanner.subTypesOf<T>("com.graphhopper.routing.$subPackage")
                .map { it.kotlin }
                .filter { it.isAbstract }
                .mapNotNull { clazz ->
                    clazz.constructors.find { constructor -> constructor.parameters.all { it.isOptional } }
                }
                .map { it.call().toString() }

        private fun profileFor(vehicle: String, weighting: String,): Profile = Profile("${vehicle}_$weighting")
            .setVehicle(vehicle)
            .setWeighting(weighting)
    }
}
