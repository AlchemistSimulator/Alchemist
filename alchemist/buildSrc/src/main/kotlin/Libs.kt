import org.gradle.api.Project
import kotlin.String

fun Project.alchemist(module: String) = project(":alchemist-$module")
fun Project.incarnation(module: String) = alchemist("incarnation-$module")

fun modularizedLibrary(base: String, module: String = "", separator: String = "-") = when {
    module.isEmpty() -> base
    else -> base + separator + module
} + ":_"

/*
 * Shortcuts for libraries used in multiple places
 */
fun arrowKt(module: String) = modularizedLibrary("io.arrow-kt:arrow", module)
fun graphhopper(module: String) = modularizedLibrary("com.graphhopper:graphhopper", module)
fun graphStream(module: String = "") = modularizedLibrary("org.danilopianini:graphstream-gs", module)
fun jgrapht(module: String = "") = modularizedLibrary("org.jgrapht:jgrapht", module)
fun konf(module: String = "") = modularizedLibrary("com.uchuhimo:konf", module)
fun protelis(module: String = "") = modularizedLibrary("org.protelis:protelis", module)
fun scalaModule(module: String = "") = modularizedLibrary("org.scala-lang:scala", module)

object Libs {
    /**
     * http://www.antlr.org
     */
    const val antlr4: String = "org.antlr:antlr4:_"

    /**
     * http://www.antlr.org
     */
    const val antlr4_runtime: String = "org.antlr:antlr4-runtime:_"

    /**
     * https://travis-ci.org/DanySK/Thread-Inheritable-Resource-Loader-for-Java
     */
    const val thread_inheritable_resource_loader: String =
            "org.danilopianini:thread-inheritable-resource-loader:_"

    /**
     * http://code.google.com/p/concurrentlinkedhashmap
     */
    const val concurrentlinkedhashmap_lru: String =
            "com.googlecode.concurrentlinkedhashmap:concurrentlinkedhashmap-lru:_"

    /**
     * http://www.github.com/kotlintest/kotlintest
     */
    const val kotest_runner_junit5: String = "io.kotest:kotest-runner-junit5-jvm:_"
    const val kotest_assertions: String = "io.kotest:kotest-assertions-core-jvm:_"

    /**
     * https://github.com/mapsforge/mapsforge
     */
    const val mapsforge_map_awt: String = "org.mapsforge:mapsforge-map-awt:_"

    /**
     * https://github.com/jIconFont/jiconfont-javafx
     */
    const val jiconfont_javafx: String = "com.github.jiconfont:jiconfont-javafx:_"

    /**
     * http://logback.qos.ch
     */
    const val logback_classic: String = "ch.qos.logback:logback-classic:_"

    /**
     * http://www.miglayout.com/
     */
    const val miglayout_swing: String = "com.miglayout:miglayout-swing:_"

    /**
     * https://commons.apache.org/proper/commons-codec/
     */
    const val commons_codec: String = "commons-codec:commons-codec:_"

    /**
     * http://commons.apache.org/proper/commons-lang/
     */
    const val commons_lang3: String = "org.apache.commons:commons-lang3:_"

    /**
     * http://commons.apache.org/proper/commons-math/
     */
    const val commons_math3: String = "org.apache.commons:commons-math3:_"

    /**
     * https://groovy-lang.org
     */
    const val groovy_jsr223: String = "org.codehaus.groovy:groovy-jsr223:_"

    /**
     * https://github.com/DanySK/java-quadtree
     */
    const val java_quadtree: String = "org.danilopianini:java-quadtree:_"

    /**
     * https://github.com/DanySK/javalib-java7
     */
    const val javalib_java7: String = "org.danilopianini:javalib-java7:_"

    /**
     * https://github.com/blackears/svgSalamander
     */
    const val svgsalamander: String = "guru.nidi.com.kitfox:svgSalamander:1.1.2"

    /**
     * http://code.google.com/p/simplelatlng
     */
    const val simplelatlng: String = "com.javadocmd:simplelatlng:_"

    /**
     * https://github.com/JetBrains/java-annotations
     */
    const val annotations: String = "org.jetbrains:annotations:_"

    /**
     * https://github.com/DanySK/boilerplate
     */
    const val boilerplate: String = "org.danilopianini:boilerplate:_"

    /**
     * http://commons.apache.org/proper/commons-cli/
     */
    const val commons_cli: String = "commons-cli:commons-cli:_"

    const val dsiutil: String = "it.unimi.dsi:dsiutils:_"

    /**
     * https://github.com/DanySK/gson-extras
     */
    const val gson_extras: String = "org.danilopianini:gson-extras:_"

    const val jool_java_8: String = "org.jooq:jool-java-8:_"

    /**
     * https://github.com/classgraph/classgraph
     */
    const val classgraph: String = "io.github.classgraph:classgraph:_"

    /**
     * http://commons.apache.org/proper/commons-io/
     */
    const val commons_io: String = "commons-io:commons-io:_"

    /**
     * http://www.controlsfx.org/
     */
    const val controlsfx: String = "org.controlsfx:controlsfx:_"

    /**
     * https://github.com/codecentric/javafxsvg
     */
    const val javafxsvg: String = "de.codecentric.centerdevice:javafxsvg:_"

    /**
     * http://www.slf4j.org
     */
    const val slf4j_api: String = "org.slf4j:slf4j-api:_"

    /**
     * http://www.snakeyaml.org
     */
    const val snakeyaml: String = "org.yaml:snakeyaml:_"

    /**
     * https://github.com/edvin/tornadofx
     */
    const val tornadofx: String = "no.tornado:tornadofx:_"

    /**
     * https://github.com/ben-manes/caffeine
     */
    const val caffeine: String = "com.github.ben-manes.caffeine:caffeine:_"

    /**
     * https://spotbugs.github.io/
     */
    const val spotbugs: String = "com.github.spotbugs:spotbugs:_"

    /**
     * http://www.jfoenix.com/
     */
    const val jfoenix: String = "com.jfoenix:jfoenix:_"

    /**
     * https://github.com/DanySK/listset
     */
    const val listset: String = "org.danilopianini:listset:_"

    /**
     * http://trove4j.sf.net
     */
    const val trove4j: String = "net.sf.trove4j:trove4j:_"

    /**
     * https://github.com/google/guava
     */
    const val guava: String = "com.google.guava:guava:_"

    /**
     * http://github.com/davidmoten/rtree
     */
    const val rtree: String = "com.github.davidmoten:rtree:_"

    /**
     * https://github.com/google/gson
     */
    const val gson: String = "com.google.code.gson:gson:_"

    /**
     * https://github.com/DanySK/jirf
     */
    const val jirf: String = "org.danilopianini:jirf:_"

    /**
     * https://github.com/jenetics/jpx
     */
    const val jpx: String = "io.jenetics:jpx:_"

    /**
     * https://github.com/jIconFont/jiconfont-google_material_design_icons
     */
    const val jiconfont_google_material_design_icons: String = "com.github.jiconfont:jiconfont-google_material_design_icons:_"

    /**
     * https://github.com/ssaring/sportstracker/tree/master/leafletmap
     */
    const val ssaring_sportstracker_leafletmap: String = "org.danilopianini:de.saring.leafletmap:_"

    /**
     * https://search.maven.org/search?q=a:org.oxygen.oxygen-icons
     */
    const val oxygen: String = "net.anwiba.commons.swing.icons:org.oxygen.oxygen-icons:_"
}
