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

class SimulationsComparisons {

    @Test
    fun <T, P : Position<P>> test01() {
        { DslLoaderFunctions.test01Nodes<T, P>() }.shouldEqual("dsl/01-nodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test02() {
        { DslLoaderFunctions.test02ManyNodes<T, P>() }.shouldEqual("dsl/02-manynodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test03() {
        { DslLoaderFunctions.test03Grid<T, P>() }.shouldEqual("dsl/03-grid.yml")
    }

    @Test
    fun <T, P : Position<P>> test05() {
        { DslLoaderFunctions.test05Content<T, P>() }.shouldEqual("dsl/05-content.yml")
    }

    @Test
    fun <T, P : Position<P>> test06() {
        { DslLoaderFunctions.test06ContentFiltered<T, P>() }.shouldEqual("dsl/06-filters.yml")
    }

    @Test
    fun <T, P : Position<P>> test07() {
        { DslLoaderFunctions.test07Programs<T, P>() }.shouldEqual("dsl/07-program.yml")
    }

    @Test
    fun <T, P : Position<P>> test08() {
        DslLoaderFunctions.test08ProtelisPrograms<T, P>().shouldEqual("dsl/08-protelisprogram.yml")
    }

    @Test
    fun <T, P : Position<P>> test09() {
        { DslLoaderFunctions.test09TimeDistribution<T, P>() }.shouldEqual("dsl/09-timedistribution.yml")
    }

    @Test
    fun <T, P : Position<P>> test10() {
        { DslLoaderFunctions.test10Environment<T, P>() }.shouldEqual("dsl/10-environment.yml")
    }

    @Test
    fun <T, P : Position<P>> test11() {
        { DslLoaderFunctions.test11monitors<T, P>() }.shouldEqual("dsl/11-monitors.yml")
    }

    @Test
    fun <T, P : Position<P>> test12() {
        { DslLoaderFunctions.test12Layers<T, P>() }.shouldEqual("dsl/12-layers.yml")
    }

    @Test
    fun <T, P : Position<P>> test13() {
        { DslLoaderFunctions.test13GlobalReaction<T, P>() }.shouldEqual("dsl/13-globalreaction.yml")
    }

    @Test
    fun <T, P : Position<P>> test14() {
        { DslLoaderFunctions.test14Exporters<T, P>() }.shouldEqual("dsl/14-exporters.yml")
    }

    @Test
    fun <T, P : Position<P>> test15() {
        { DslLoaderFunctions.test15Variables<T, P>() }.shouldEqual("dsl/15-variables.yml")
    }

    @Test
    fun <T, P : Position<P>> test16() {
        { DslLoaderFunctions.test16ProgramsFilters<T, P>() }.shouldEqual("dsl/16-programsfilters.yml")
    }

    @Test
    fun <T, P : Position<P>> test17() {
        { DslLoaderFunctions.test17CustomNodes<T, P>() }.shouldEqual("dsl/17-customnodes.yml")
    }
}
