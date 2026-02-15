/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.kotlindsl.TestComparators.shouldEqual
import it.unibo.alchemist.model.Position
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.Locale
import kotlin.time.measureTime
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.kaikikm.threadresloader.ResourceLoader

class PerformanceComparisonTest {

    private data class PerformanceStats(
        val yamlLoadingTime: Long,
        val dslLoadingTime: Long,
        val avgYamlActionTime: Double,
        val avgDslActionTime: Double,
        val minYamlActionTime: Long,
        val minDslActionTime: Long,
        val maxYamlActionTime: Long,
        val maxDslActionTime: Long,
    )

    private fun calculateStats(
        yamlLoadingTime: Long,
        dslLoadingTime: Long,
        yamlActionTimes: List<Long>,
        dslActionTimes: List<Long>,
    ): PerformanceStats = PerformanceStats(
        yamlLoadingTime = yamlLoadingTime,
        dslLoadingTime = dslLoadingTime,
        avgYamlActionTime = yamlActionTimes.average(),
        avgDslActionTime = dslActionTimes.average(),
        minYamlActionTime = yamlActionTimes.minOrNull() ?: 0L,
        minDslActionTime = dslActionTimes.minOrNull() ?: 0L,
        maxYamlActionTime = yamlActionTimes.maxOrNull() ?: 0L,
        maxDslActionTime = dslActionTimes.maxOrNull() ?: 0L,
    )

    private fun printResults(header: String, stats: PerformanceStats) {
        println("\n=== $header ===")
        println("YAML Loader:")
        println("  Loading Phase:")
        println("    Time: ${stats.yamlLoadingTime} ms")
        println("  Action Phase:")
        println("    Average: ${String.format(Locale.US, "%.2f", stats.avgYamlActionTime)} ms")
        println("    Min: ${stats.minYamlActionTime} ms")
        println("    Max: ${stats.maxYamlActionTime} ms")
        println(
            "  Total Average: ${String.format(
                Locale.US,
                "%.2f",
                stats.yamlLoadingTime + stats.avgYamlActionTime,
            )} ms",
        )
        println("\nDSL Loader:")
        println("  Loading Phase:")
        println("    Time: ${stats.dslLoadingTime} ms")
        println("  Action Phase:")
        println("    Average: ${String.format(Locale.US, "%.2f", stats.avgDslActionTime)} ms")
        println("    Min: ${stats.minDslActionTime} ms")
        println("    Max: ${stats.maxDslActionTime} ms")
        println(
            "  Total Average: ${String.format(Locale.US, "%.2f", stats.dslLoadingTime + stats.avgDslActionTime)} ms",
        )
    }

    private fun printSpeedup(stats: PerformanceStats, dslFasterMsg: String, yamlFasterMsg: String) {
        val totalYamlTime = stats.yamlLoadingTime + stats.avgYamlActionTime
        val totalDslTime = stats.dslLoadingTime + stats.avgDslActionTime
        val speedup = totalYamlTime / totalDslTime
        println("\nSpeedup (Total): ${String.format(Locale.US, "%.2f", speedup)}x")
        if (speedup > 1.0) {
            println("$dslFasterMsg ${String.format(Locale.US, "%.2f", speedup)}x faster than YAML")
        } else {
            println("$yamlFasterMsg ${String.format(Locale.US, "%.2f", 1.0 / speedup)}x faster than DSL")
        }
        val loadingSpeedup = stats.yamlLoadingTime.toDouble() / stats.dslLoadingTime.toDouble()
        println("Loading Phase Speedup: ${String.format(Locale.US, "%.2f", loadingSpeedup)}x")
        val actionSpeedup = stats.avgYamlActionTime / stats.avgDslActionTime
        println("Action Phase Speedup: ${String.format(Locale.US, "%.2f", actionSpeedup)}x")
    }

    private fun runPerformanceTest(
        testHeader: String,
        yamlResource: String,
        dslResource: String,
        iterations: Int,
        resultsHeader: String,
        dslFasterMsg: String,
        yamlFasterMsg: String,
        yamlLoaderAction: (Loader) -> Unit,
        dslLoaderAction: (Loader) -> Unit,
    ): PerformanceStats {
        val originalOut = System.out
        val originalErr = System.err
        val nullStream = PrintStream(ByteArrayOutputStream())
        println("\n=== $testHeader ===")
        println("Resource: $yamlResource")
        println("Iterations: $iterations\n")
        val yamlActionTimes = mutableListOf<Long>()
        val dslActionTimes = mutableListOf<Long>()
        val dslUrl = ResourceLoader.getResource(dslResource)!!
        val ymlUrl = ResourceLoader.getResource(yamlResource)!!
        System.setOut(nullStream)
        System.setErr(nullStream)
        var yamlLoader: Loader? = null
        val yamlLoadingTime = measureTime {
            yamlLoader = LoadAlchemist.from(ymlUrl)
        }
        assertNotNull(yamlLoader)
        var dslLoader: Loader? = null
        val dslLoadingTime = measureTime {
            dslLoader = LoadAlchemist.from(dslUrl)
        }
        assertNotNull(dslLoader)
        System.setOut(originalOut)
        System.setErr(originalErr)
        repeat(iterations) {
            System.setOut(nullStream)
            System.setErr(nullStream)
            val yamlActionTime = measureTime {
                yamlLoaderAction(yamlLoader!!)
            }
            val dslActionTime = measureTime {
                dslLoaderAction(dslLoader!!)
            }
            System.setOut(originalOut)
            System.setErr(originalErr)
            yamlActionTimes.add(yamlActionTime.inWholeMilliseconds)
            dslActionTimes.add(dslActionTime.inWholeMilliseconds)
        }
        val stats = calculateStats(
            yamlLoadingTime.inWholeMilliseconds,
            dslLoadingTime.inWholeMilliseconds,
            yamlActionTimes,
            dslActionTimes,
        )
        printResults(resultsHeader, stats)
        printSpeedup(stats, dslFasterMsg, yamlFasterMsg)
        println("\n=== Test completed ===\n")
        return stats
    }
    private fun runTestWith(
        name: String,
        action: (Loader) -> Unit,
        testName: String,
        iterations: Int = 10,
    ): PerformanceStats {
        val dslResource = "dsl/kts/$name.alchemist.kts"
        val yamlResource = "dsl/yml/$name.yml"
        return runPerformanceTest(
            testHeader = testName,
            yamlResource = yamlResource,
            dslResource = dslResource,
            iterations = iterations,
            resultsHeader = "Results",
            dslFasterMsg = "DSL loading is",
            yamlFasterMsg = "YAML loading is",
            yamlLoaderAction = action,
            dslLoaderAction = action,
        )
    }

    @Test
    fun <T, P : Position<P>> `performance comparison between YAML and DSL loaders`() {
        runTestWith(
            "19-performance",
            testName = "Performance Test: YAML vs DSL Loader",
            action = { it.getDefault<T, P>() },
            iterations = 20,
        )
    }

    @Test
    fun `performance comparison - loading phase only`() {
        val stats = mutableListOf<PerformanceStats>()
        repeat(50) {
            stats += runTestWith(
                "19-performance",
                action = {},
                testName = "Performance Test: Loading Phase Only ",
            )
        }
        val avgYamlLoadingTime = stats.map { it.yamlLoadingTime }.average()
        val avgDslLoadingTime = stats.map { it.dslLoadingTime }.average()
        println("\n=== Loading Phase Only Average Results ===")
        println(
            "YAML Loader Average Loading Time: ${String.format(
                Locale.US,
                "%.2f",
                avgYamlLoadingTime,
            )} ms",
        )
        println(
            "DSL Loader Average Loading Time: ${String.format(
                Locale.US,
                "%.2f",
                avgDslLoadingTime,
            )} ms",
        )
    }

    @Test
    fun `verify both loaders produce equivalent results`() {
        val yamlResource = "dsl/yml/19-performance.yml"
        val dslResource = "/dsl/kts/19-performance.alchemist.kts"
        val dslUrl = requireNotNull(this.javaClass.getResource(dslResource)) {
            "Resource $dslResource not found on test classpath"
        }
        val dslFile = File(dslUrl.toURI())
        val yamlLoader = LoadAlchemist.from(ResourceLoader.getResource(yamlResource)!!)
        val dslLoader = LoadAlchemist.from(dslFile)
        assertNotNull(yamlLoader)
        assertNotNull(dslLoader)
        dslLoader.shouldEqual<Any, Nothing>(yamlResource, 0L)
    }
}
