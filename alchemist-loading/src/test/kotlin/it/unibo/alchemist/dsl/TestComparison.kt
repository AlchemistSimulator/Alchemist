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

class TestComparison {

    @Test
    fun <T, P : Position<P>> test01Nodes() {
        DslLoaderFunctions.test01Nodes<T, P>().shouldEqual("dsl/01-nodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test02ManyNodes() {
        DslLoaderFunctions.test02ManyNodes<T, P>().shouldEqual("dsl/02-manynodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test03Grid() {
        DslLoaderFunctions.test03Grid<T, P>().shouldEqual("dsl/03-grid.yml")
    }

    @Test
    fun <T, P : Position<P>> test05Content() {
        DslLoaderFunctions.test05Content<T, P>().shouldEqual("dsl/05-content.yml")
    }

    @Test
    fun <T, P : Position<P>> test06ContentFiltered() {
        DslLoaderFunctions.test06ContentFiltered<T, P>().shouldEqual("dsl/06-filters.yml")
    }

    @Test
    fun <T, P : Position<P>> test07Programs() {
        DslLoaderFunctions.test07Programs<T, P>().shouldEqual("dsl/07-program.yml")
    }

    @Test
    fun <T, P : Position<P>> test08ProtelisPrograms() {
        DslLoaderFunctions.test08ProtelisPrograms<T, P>().shouldEqual("dsl/08-protelisprogram.yml")
    }

    @Test
    fun <T, P : Position<P>> test09TimeDistribution() {
        DslLoaderFunctions.test09TimeDistribution<T, P>().shouldEqual("dsl/09-timedistribution.yml")
    }

    @Test
    fun <T, P : Position<P>> test10Environment() {
        DslLoaderFunctions.test10Environment<T, P>().shouldEqual("dsl/10-environment.yml")
    }

    @Test
    fun <T, P : Position<P>> test11Monitors() {
        DslLoaderFunctions.test11monitors<T, P>().shouldEqual("dsl/11-monitors.yml")
    }

    @Test
    fun <T, P : Position<P>> test12Layers() {
        DslLoaderFunctions.test12Layers<T, P>().shouldEqual("dsl/12-layers.yml")
    }
}
