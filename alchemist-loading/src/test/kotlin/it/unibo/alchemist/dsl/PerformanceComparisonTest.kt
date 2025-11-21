package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.model.Position
import java.io.File
import java.io.PrintStream
import java.util.Locale
import kotlin.time.measureTime
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.kaikikm.threadresloader.ResourceLoader

class PerformanceComparisonTest {

    private data class PerformanceStats(
        val avgYamlTime: Double,
        val avgDslTime: Double,
        val minYamlTime: Long,
        val minDslTime: Long,
        val maxYamlTime: Long,
        val maxDslTime: Long,
    )

    private fun calculateStats(yamlTimes: List<Long>, dslTimes: List<Long>): PerformanceStats = PerformanceStats(
        avgYamlTime = yamlTimes.average(),
        avgDslTime = dslTimes.average(),
        minYamlTime = yamlTimes.minOrNull() ?: 0L,
        minDslTime = dslTimes.minOrNull() ?: 0L,
        maxYamlTime = yamlTimes.maxOrNull() ?: 0L,
        maxDslTime = dslTimes.maxOrNull() ?: 0L,
    )

    private fun printResults(header: String, stats: PerformanceStats) {
        println("\n=== $header ===")
        println("YAML Loader:")
        println("  Average: ${String.format(Locale.US, "%.2f", stats.avgYamlTime)} ms")
        println("  Min: ${stats.minYamlTime} ms")
        println("  Max: ${stats.maxYamlTime} ms")
        println("\nDSL Loader:")
        println("  Average: ${String.format(Locale.US, "%.2f", stats.avgDslTime)} ms")
        println("  Min: ${stats.minDslTime} ms")
        println("  Max: ${stats.maxDslTime} ms")
    }

    private fun printSpeedup(stats: PerformanceStats, dslFasterMsg: String, yamlFasterMsg: String) {
        val speedup = stats.avgYamlTime / stats.avgDslTime
        println("\nSpeedup: ${String.format(Locale.US, "%.2f", speedup)}x")
        if (speedup > 1.0) {
            println("$dslFasterMsg ${String.format(Locale.US, "%.2f", speedup)}x faster than YAML")
        } else {
            println("$yamlFasterMsg ${String.format(Locale.US, "%.2f", 1.0 / speedup)}x faster than DSL")
        }
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
    ) {
        val originalOut = System.out
        val originalErr = System.err
        val nullStream = PrintStream(java.io.ByteArrayOutputStream())

        println("\n=== $testHeader ===")
        println("Resource: $yamlResource")
        println("Iterations: $iterations\n")

        val yamlTimes = mutableListOf<Long>()
        val dslTimes = mutableListOf<Long>()

        val dslUrl = ResourceLoader.getResource(dslResource)!!
        val ymlUrl = ResourceLoader.getResource(yamlResource)!!

        repeat(iterations) {
            System.setOut(nullStream)
            System.setErr(nullStream)

            val yamlTime = measureTime {
                val yamlLoader = LoadAlchemist.from(ymlUrl)
                assertNotNull(yamlLoader)
                yamlLoaderAction(yamlLoader)
            }

            val dslTime = measureTime {
                val dslLoader = LoadAlchemist.from(dslUrl)
                assertNotNull(dslLoader)
                dslLoaderAction(dslLoader)
            }

            System.setOut(originalOut)
            System.setErr(originalErr)

            yamlTimes.add(yamlTime.inWholeMilliseconds)
            dslTimes.add(dslTime.inWholeMilliseconds)
        }

        yamlTimes.forEachIndexed { index, time ->
            println("Iteration ${index + 1}: YAML=${time}ms, DSL=${dslTimes[index]}ms")
        }

        val stats = calculateStats(yamlTimes, dslTimes)
        printResults(resultsHeader, stats)
        printSpeedup(stats, dslFasterMsg, yamlFasterMsg)

        println("\n=== Test completed ===\n")
    }

    @Test
    fun <T, P : Position<P>> `performance comparison between YAML and DSL loaders`() {
        runPerformanceTest(
            testHeader = "Performance Test: YAML vs DSL Loader",
            yamlResource = "dsl/yml/19-performance.yml",
            dslResource = "dsl/kts/19-performance.alchemist.kts",
            iterations = 5,
            resultsHeader = "Results",
            dslFasterMsg = "DSL is",
            yamlFasterMsg = "YAML is",
            yamlLoaderAction = { it.getDefault<T, P>() },
            dslLoaderAction = { it.getDefault<T, P>() },
        )
    }

    @Test
    fun `performance comparison - loading phase only`() {
        runPerformanceTest(
            testHeader = "Performance Test: Loading Phase Only (YAML vs DSL)",
            yamlResource = "dsl/yml/19-performance.yml",
            dslResource = "dsl/kts/19-performance.alchemist.kts",
            iterations = 10,
            resultsHeader = "Results (Loading Phase Only)",
            dslFasterMsg = "DSL loading is",
            yamlFasterMsg = "YAML loading is",
            yamlLoaderAction = {},
            dslLoaderAction = {},
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

        val dslLoaderFunction = { dslLoader }
        dslLoaderFunction.shouldEqual(yamlResource, includeRuntime = false)
    }
}
