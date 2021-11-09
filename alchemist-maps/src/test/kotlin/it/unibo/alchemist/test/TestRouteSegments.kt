package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import it.unibo.alchemist.model.implementations.positions.LatLongPosition
import it.unibo.alchemist.model.interfaces.GeoPosition

class TestRouteSegments : StringSpec(
    {
        "long routes should have more than a single segment" {
            val incarnation = SupportedIncarnations
                .get<Any, GeoPosition>(SupportedIncarnations.getAvailableIncarnations().first())
                .get()
            with(OSMEnvironment(incarnation, "maps/cesena.pbf")) {
                val pharmacy = LatLongPosition(44.14022881997589, 12.234464874617203)
                val stadium = LatLongPosition(44.140937161857074, 12.261716117329186)
                val route = computeRoute(pharmacy, stadium)
                route.points.size shouldBeGreaterThan 3
                route.points.first() shouldBe pharmacy
                route.points.last() shouldBe stadium
                route.length() shouldBeGreaterThan pharmacy.distanceTo(stadium)
            }
        }
    }
)