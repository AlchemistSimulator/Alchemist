/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter

/**
 * Context interface for configuring node content (molecules and concentrations).
 *
 * This context is used within [DeploymentContext] blocks to define the initial
 * content of nodes deployed at specific positions.
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [it.unibo.alchemist.model.Position].
 *
 * @see [DeploymentContext] for the parent context
 * @see [it.unibo.alchemist.model.Incarnation.createMolecule]
 * @see [it.unibo.alchemist.model.Incarnation.createConcentration]
 */
interface ContentContext<T, P : Position<P>> {
    /**
     * The optional position filter applied to this content context.
     *
     * If set, content is only applied to nodes at positions matching this filter.
     */
    val filter: PositionBasedFilter<P>?

    /**
     * The molecule name to inject into nodes.
     *
     * The molecule is created using the incarnation's molecule factory.
     *
     * @see [it.unibo.alchemist.model.Incarnation.createMolecule]
     */
    var molecule: String?

    /**
     * The concentration value for the molecule.
     *
     * The concentration is created using the incarnation's concentration factory.
     *
     * @see [it.unibo.alchemist.model.Incarnation.createConcentration]
     */
    var concentration: T?
}
