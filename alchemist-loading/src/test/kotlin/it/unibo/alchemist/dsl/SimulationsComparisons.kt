/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.dsl

import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Test

class SimulationsComparisons {

    @Test
    fun <T, P : Position<P>> test01() {
        { DslLoaderFunctions.test01Nodes<T, P>() }.shouldEqual("dsl/yml/01-nodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test02() {
        { DslLoaderFunctions.test02ManyNodes<T, P>() }.shouldEqual("dsl/yml/02-manynodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test03() {
        { DslLoaderFunctions.test03Grid<T, P>() }.shouldEqual("dsl/yml/03-grid.yml")
    }

    @Test
    fun <T, P : Position<P>> test05() {
        { DslLoaderFunctions.test05Content<T, P>() }.shouldEqual("dsl/yml/05-content.yml")
    }

    @Test
    fun <T, P : Position<P>> test06() {
        { DslLoaderFunctions.test06ContentFiltered<T, P>() }.shouldEqual("dsl/yml/06-filters.yml")
    }

    @Test
    fun <T, P : Position<P>> test07() {
        { DslLoaderFunctions.test07Programs<T, P>() }.shouldEqual("dsl/yml/07-program.yml")
    }

    @Test
    fun <T, P : Position<P>> test08() {
        DslLoaderFunctions.test08ProtelisPrograms<T, P>().shouldEqual("dsl/yml/08-protelisprogram.yml")
    }

    @Test
    fun <T, P : Position<P>> test09() {
        { DslLoaderFunctions.test09TimeDistribution<T, P>() }.shouldEqual("dsl/yml/09-timedistribution.yml")
    }

    @Test
    fun <T, P : Position<P>> test10() {
        { DslLoaderFunctions.test10Environment<T>() }.shouldEqual("dsl/yml/10-environment.yml")
    }

    @Test
    fun <T, P : Position<P>> test11() {
        { DslLoaderFunctions.test11monitors() }.shouldEqual("dsl/yml/11-monitors.yml")
    }

    @Test
    fun <T, P : Position<P>> test12() {
        { DslLoaderFunctions.test12Layers() }.shouldEqual("dsl/yml/12-layers.yml")
    }

    @Test
    fun <T, P : Position<P>> test13() {
        { DslLoaderFunctions.test13GlobalReaction() }.shouldEqual("dsl/yml/13-globalreaction.yml")
    }

    @Test
    fun <T, P : Position<P>> test14() {
        { DslLoaderFunctions.test14Exporters<T, P>() }.shouldEqual("dsl/yml/14-exporters.yml")
    }

    @Test
    fun <T, P : Position<P>> test15() {
        { DslLoaderFunctions.test15Variables<T, P>() }.shouldEqual("dsl/yml/15-variables.yml")
    }

    @Test
    fun <T, P : Position<P>> test16() {
        { DslLoaderFunctions.test16ProgramsFilters<T, P>() }
            .shouldEqual(
                "dsl/yml/16-programsfilters.yml",
                targetTime = 10.0,
            )
    }

    @Test
    fun <T, P : Position<P>> test17() {
        { DslLoaderFunctions.test17CustomNodes<T, P>() }.shouldEqual("dsl/yml/17-customnodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test18() {
        { DslLoaderFunctions.test18NodeProperties<T, P>() }.shouldEqual("dsl/yml/18-properties.yml")
    }

    @Test
    fun <T, P : Position<P>> test20() {
        { DslLoaderFunctions.test20Actions<T, P>() }.shouldEqual(
            "dsl/yml/20-move.yml",
            targetTime = 10.0,
        )
    }
}
