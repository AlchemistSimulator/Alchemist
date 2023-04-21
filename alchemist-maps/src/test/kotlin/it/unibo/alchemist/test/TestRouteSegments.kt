/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.api.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.implementations.positions.LatLongPosition

class TestRouteSegments : StringSpec(
    {
        val incarnation = SupportedIncarnations
            .get<Any, GeoPosition>(SupportedIncarnations.getAvailableIncarnations().first())
            .get()
        val pharmacy = LatLongPosition(44.14022881997589, 12.234464874617203)
        val stadium = LatLongPosition(44.140937161857074, 12.261716117329186)
        val bertinoro = LatLongPosition(44.14883128462208, 12.135307764053827)
        val bulgarno = LatLongPosition(44.13392232154526, 12.336352744054683)
        with(OSMEnvironment(incarnation, "maps/cesena.pbf")) {
            "long routes should have more than a single segment" {
                val route = computeRoute(pharmacy, stadium)
                route.points.size shouldBeGreaterThan 3
                route.points.first() shouldBe pharmacy
                route.points.last() shouldBe stadium
                route.length() shouldBeGreaterThan pharmacy.distanceTo(stadium)
            }
            "zero length routes should be computable" {
                val route = computeRoute(pharmacy, pharmacy)
                route.points.size shouldBe 1
                route.points.first() shouldBe pharmacy
                route.points.last() shouldBe route.points.first()
            }
            "routes beginning outside the map should be still computable" {
                val route = computeRoute(bertinoro, stadium)
                route.points.size shouldBeGreaterThan 10
                route.points.first() shouldBe bertinoro
                route.points.last() shouldBe stadium
                route.length() shouldBeGreaterThan bertinoro.distanceTo(stadium)
            }
            "routes ending outside the map should be still computable" {
                val route = computeRoute(pharmacy, bulgarno)
                route.points.size shouldBeGreaterThan 10
                route.points.first() shouldBe pharmacy
                route.points.last() shouldBe bulgarno
                route.length() shouldBeGreaterThan pharmacy.distanceTo(bulgarno)
            }
            "routes traversing the map but beginning and ending outside should create a path where data is available" {
                val route = computeRoute(bertinoro, bulgarno)
                route.points.size shouldBeGreaterThan 10
                route.points.first() shouldBe bertinoro
                route.points.last() shouldBe bulgarno
                route.length() shouldBeGreaterThan bertinoro.distanceTo(bulgarno)
            }
        }
    },
)
