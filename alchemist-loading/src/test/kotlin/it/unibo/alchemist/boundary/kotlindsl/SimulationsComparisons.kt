/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.kotlindsl

import io.kotest.core.spec.style.AnnotationSpec
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance

class SimulationsComparisons {

    @AnnotationSpec.Test
    fun test01() {
        simulation2D(SAPEREIncarnation()) {
            environment {
                networkModel(ConnectWithinDistance(5.0))
                deployments {
                    deploy(point(0, 0))
                    deploy(point(1, 1))
                }
            }
        }.shouldEqual("dsl/yml/01-nodes.yml")
    }

//    @Test
//    fun test02() {
//        { DslLoaderFunctions.test02ManyNodes() }.shouldEqual("dsl/yml/02-manynodes.yml")
//    }
//
//    @Test
//    fun test03() {
//        { DslLoaderFunctions.test03Grid() }.shouldEqual("dsl/yml/03-grid.yml")
//    }
//
//    @Test
//    fun test05() {
//        { DslLoaderFunctions.test05Content() }.shouldEqual("dsl/yml/05-content.yml")
//    }
//
//    @Test
//    fun test06() {
//        { DslLoaderFunctions.test06ContentFiltered() }.shouldEqual("dsl/yml/06-filters.yml")
//    }
//
//    @Test
//    fun test07() {
//        { DslLoaderFunctions.test07Programs() }.shouldEqual("dsl/yml/07-program.yml")
//    }
//
//    @Test
//    fun test08() {
//        DslLoaderFunctions.test08ProtelisPrograms().shouldEqual("dsl/yml/08-protelisprogram.yml")
//    }
//
//    @Test
//    fun test09() {
//        { DslLoaderFunctions.test09TimeDistribution() }.shouldEqual("dsl/yml/09-timedistribution.yml")
//    }
//
//    @Test
//    fun test10() {
//        { DslLoaderFunctions.test10Environment() }.shouldEqual("dsl/yml/10-environment.yml")
//    }
//
//    @Test
//    fun test11() {
//        { DslLoaderFunctions.test11monitors() }.shouldEqual("dsl/yml/11-monitors.yml")
//    }
//
//    @Test
//    fun <T, P : Position<P>> test12() {
//        { DslLoaderFunctions.test12Layers() }.shouldEqual("dsl/yml/12-layers.yml")
//    }
//
//    @Test
//    fun <T, P : Position<P>> test13() {
//        { DslLoaderFunctions.test13GlobalReaction() }.shouldEqual("dsl/yml/13-globalreaction.yml")
//    }
//
//    @Test
//    fun <T, P : Position<P>> test14() {
//        { DslLoaderFunctions.test14Exporters() }.shouldEqual("dsl/yml/14-exporters.yml")
//    }
//
//    @Test
//    fun test15() {
//        { DslLoaderFunctions.test15Variables() }.shouldEqual("dsl/yml/15-variables.yml")
//    }
//
//    @Test
//    fun test16() {
//        { DslLoaderFunctions.test16ProgramsFilters() }
//            .shouldEqual(
//                "dsl/yml/16-programsfilters.yml",
//                targetTime = 10.0,
//            )
//    }
//
//    @Test
//    fun test17() {
//        { DslLoaderFunctions.test17CustomNodes() }.shouldEqual("dsl/yml/17-customnodes.yml")
//    }
//
//    @Test
//    fun test18() {
//        { DslLoaderFunctions.test18NodeProperties() }.shouldEqual("dsl/yml/18-properties.yml")
//    }
//
//    @Test
//    fun test20() {
//        { DslLoaderFunctions.test20Actions() }.shouldEqual(
//            "dsl/yml/20-move.yml",
//            targetTime = 10.0,
//        )
//    }
}
