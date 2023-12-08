/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui

import it.unibo.alchemist.boundary.fxui.viewports.NodeViewPort
import it.unibo.alchemist.boundary.ui.impl.AbstractWormhole2D
import it.unibo.alchemist.boundary.ui.impl.PointAdapter
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position2D
import javafx.scene.Node
import java.util.function.Function

/**
 * An implementation of [AbstractWormhole2D] for JavaFX.
 *
 * @param P the type of the position
 */
open class WormholeFX<P : Position2D<P>>(
    environment: Environment<*, P>,
    node: Node,
) : AbstractWormhole2D<P>(
    environment,
    NodeViewPort(node),
    Function<NodeViewPort, PointAdapter<P>> {
        PointAdapter.from(it.node.boundsInLocal.width / 2, it.node.boundsInLocal.height / 2)
    },
)
