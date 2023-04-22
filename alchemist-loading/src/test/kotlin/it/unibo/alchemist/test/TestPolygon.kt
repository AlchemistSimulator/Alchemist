/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.loader.deployments.Polygon
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.environments.OSMEnvironment
import org.apache.commons.math3.random.MersenneTwister

private const val DEPLOYMENT_SIZE = 10_000
private fun <P : Position2D<P>> incarnation() = SupportedIncarnations.get<Any, P>("sapere").get()
class TestPolygon : StringSpec({
    fun <T, P : Position2D<P>> Environment<T, P>.runTestWithCoordinates() {
        val randomGenerator = MersenneTwister(0)
        val displacement = Polygon(this, randomGenerator, DEPLOYMENT_SIZE, points)
        displacement.stream().count() shouldBe DEPLOYMENT_SIZE
        val displacementPoints = Polygon(this, randomGenerator, DEPLOYMENT_SIZE, pointsPair)
        displacementPoints.stream().count() shouldBe DEPLOYMENT_SIZE
    }
    "test deployment on 2D space" {
        Continuous2DEnvironment<Any>(incarnation()).runTestWithCoordinates()
    }
    "test deployment on Venice lagoon" {
        OSMEnvironment(incarnation(), "venezia.pbf").runTestWithCoordinates()
    }
}) {
    companion object {
        val points = Gson().fromJson<List<List<Double>>>(
            """[
                [ 12.2504425, 45.2038121 ],
                [ 12.2641754, 45.2207426 ],
                [ 12.2806549, 45.2381516 ],
                [ 12.2895813, 45.2570053 ],
                [ 12.2957611, 45.276336 ],
                [ 12.2991943, 45.3029049 ],
                [ 12.3046875, 45.3212544 ],
                [ 12.3040009, 45.331875 ],
                [ 12.3040009, 45.3453893 ],
                [ 12.3156738, 45.3502151 ],
                [ 12.3232269, 45.3622776 ],
                [ 12.3300934, 45.3719259 ],
                [ 12.3348999, 45.3830193 ],
                [ 12.3445129, 45.395557 ],
                [ 12.3300934, 45.3998964 ],
                [ 12.3136139, 45.4018249 ],
                [ 12.3122406, 45.4105023 ],
                [ 12.311554, 45.4167685 ],
                [ 12.3012543, 45.4278531 ],
                [ 12.2902679, 45.4408627 ],
                [ 12.2772217, 45.4355628 ],
                [ 12.2703552, 45.4206242 ],
                [ 12.2744751, 45.3994143 ],
                [ 12.2676086, 45.3738553 ],
                [ 12.2614288, 45.3579354 ],
                [ 12.2497559, 45.3429763 ],
                [ 12.2408295, 45.3198059 ],
                [ 12.2346497, 45.2975921 ],
                [ 12.2408295, 45.2802014 ],
                [ 12.233963, 45.257972 ],
                [ 12.2504425, 45.2038121 ]]
            """,
            object : TypeToken<List<List<Double>>>() {}.type,
        ).map { listOf(it[1], it[0]) }

        val pointsPair = points.map { Pair(it[1], it[0]) }
    }
}
