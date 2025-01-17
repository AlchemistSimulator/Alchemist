/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.server.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.TestUtility.webRendererTestEnvironments
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.toEnvironmentSurrogate
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector
import org.junit.jupiter.api.fail

class ToEnvironmentSurrogateTest<T, P> :
    StringSpec({

        "ToEnvironmentSurrogate should map an Environment to an EnvironmentSurrogate" {
            webRendererTestEnvironments<T, P>().forEach {
                val environment = it.environment
                val environmentSurrogate =
                    environment.toEnvironmentSurrogate(
                        toEmptyConcentration,
                        toSuitablePositionSurrogate(environment.dimensions),
                    )
                environment.dimensions shouldBe environmentSurrogate.dimensions
                environment.nodes.size shouldBe environmentSurrogate.nodes.size
                environment.nodes.forEach { node ->
                    val surrogateNode = environmentSurrogate.nodes.find { surrogateNode -> node.id == surrogateNode.id }
                    if (surrogateNode != null) {
                        checkToNodeSurrogate(environment, node, surrogateNode)
                    } else {
                        fail("Can't find a corresponding SurrogateNode")
                    }
                }
            }
        }
    })where T : Any, P : Position<P>, P : Vector<P>
