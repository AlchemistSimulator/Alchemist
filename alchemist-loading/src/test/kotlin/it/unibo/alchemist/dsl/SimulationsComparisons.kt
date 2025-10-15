/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.dsl

import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Test

/**
 * Example test demonstrating the new refactored comparison architecture
 */
class SimulationsComparisons {

    @Test
    fun <T, P : Position<P>> test01() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test01Nodes<T, P>() }.shouldEqual("dsl/01-nodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test02() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test02ManyNodes<T, P>() }.shouldEqual("dsl/02-manynodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test03() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test03Grid<T, P>() }.shouldEqual("dsl/03-grid.yml")
    }

    @Test
    fun <T, P : Position<P>> test05() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test05Content<T, P>() }.shouldEqual("dsl/05-content.yml")
    }

    @Test
    fun <T, P : Position<P>> test06() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test06ContentFiltered<T, P>() }.shouldEqual("dsl/06-filters.yml")
    }

    @Test
    fun <T, P : Position<P>> test07() {
        // Compare with YAML using static comparison only (includeRuntime = false)

        { DslLoaderFunctions.test07Programs<T, P>() }.shouldEqual("dsl/07-program.yml")
    }

    @Test
    fun <T, P : Position<P>> test08() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        DslLoaderFunctions.test08ProtelisPrograms<T, P>().shouldEqual("dsl/08-protelisprogram.yml")
    }

    @Test
    fun <T, P : Position<P>> test09() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test09TimeDistribution<T, P>() }.shouldEqual("dsl/09-timedistribution.yml")
    }

    @Test
    fun <T, P : Position<P>> test10() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test10Environment<T, P>() }.shouldEqual("dsl/10-environment.yml")
    }

    @Test
    fun <T, P : Position<P>> test11() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test11monitors<T, P>() }.shouldEqual("dsl/11-monitors.yml")
    }

    @Test
    fun <T, P : Position<P>> test12() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test12Layers<T, P>() }.shouldEqual("dsl/12-layers.yml")
    }

    @Test
    fun <T, P : Position<P>> test13() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test13GlobalReaction<T, P>() }.shouldEqual("dsl/13-globalreaction.yml")
    }

    @Test
    fun <T, P : Position<P>> test14() {
        // Compare with YAML using static comparison only (includeRuntime = false)
        { DslLoaderFunctions.test14Exporters<T, P>() }.shouldEqual("dsl/14-exporters.yml")
    }
}
