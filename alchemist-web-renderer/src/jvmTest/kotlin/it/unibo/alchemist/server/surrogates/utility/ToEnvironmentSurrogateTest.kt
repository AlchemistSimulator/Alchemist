/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.TestUtility.webRendererTestEnvironments
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration
import it.unibo.alchemist.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate
import org.junit.jupiter.api.fail

class ToEnvironmentSurrogateTest<T, P> : StringSpec({

    "ToEnvironmentSurrogate should map an Environment to an EnvironmentSurrogate" {
        webRendererTestEnvironments<T, P>().forEach {
            val environmentSurrogate = it.toEnvironmentSurrogate(
                toEmptyConcentration,
                toSuitablePositionSurrogate(it.dimensions)
            )
            it.dimensions shouldBe environmentSurrogate.dimensions
            it.nodes.size shouldBe environmentSurrogate.nodes.size
            it.nodes.forEach { node ->
                val surrogateNode = environmentSurrogate.nodes.find { surrogateNode -> node.id == surrogateNode.id }
                if (surrogateNode != null) {
                    checkToNodeSurrogate(it, node, surrogateNode)
                } else {
                    fail("Can't find a corresponding SurrogateNode")
                }
            }
        }
    }
})where T : Any, P : Position<P>, P : Vector<P>
