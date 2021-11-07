import org.gradle.api.Project
import kotlin.String

/**
 * Statically defined libraries used by the project.
 */
@Suppress("UndocumentedPublicProperty")
object Libs {
    const val annotations: String = "org.jetbrains:annotations:_"
    const val boilerplate: String = "org.danilopianini:boilerplate:_"
    const val conrec: String = "org.danilopianini:conrec:_"
    const val classgraph: String = "io.github.classgraph:classgraph:_"
    const val controlsfx: String = "org.controlsfx:controlsfx:_"
    const val dsiutil: String = "it.unimi.dsi:dsiutils:_"
    const val groovy_jsr223: String = "org.codehaus.groovy:groovy-jsr223:_"
    const val gson: String = "com.google.code.gson:gson:_"
    const val gson_extras: String = "org.danilopianini:gson-extras:_"
    const val guava: String = "com.google.guava:guava:_"
    const val java_quadtree: String = "org.danilopianini:java-quadtree:_"
    const val javafxsvg: String = "de.codecentric.centerdevice:javafxsvg:_"
    const val javalib_java7: String = "org.danilopianini:javalib-java7:_"
    const val jfoenix: String = "com.jfoenix:jfoenix:_"
    const val jirf: String = "org.danilopianini:jirf:_"
    const val jool: String = "org.jooq:jool:_"
    const val jpx: String = "io.jenetics:jpx:_"
    const val kotest_assertions: String = "io.kotest:kotest-assertions-core-jvm:_"
    const val kotest_runner_junit5: String = "io.kotest:kotest-runner-junit5-jvm:_"
    const val listset: String = "org.danilopianini:listset:_"
    const val mapsforge_map_awt: String = "org.mapsforge:mapsforge-map-awt:_"
    const val miglayout_swing: String = "com.miglayout:miglayout-swing:_"
    const val oxygen: String = "net.anwiba.commons.swing.icons:org.oxygen.oxygen-icons:_"
    const val simplelatlng: String = "com.javadocmd:simplelatlng:_"
    const val slf4j_api: String = "org.slf4j:slf4j-api:_"
    const val snakeyaml: String = "org.yaml:snakeyaml:_"
    const val ssaring_sportstracker_leafletmap: String = "org.danilopianini:de.saring.leafletmap:_"
    const val svgsalamander: String = "guru.nidi.com.kitfox:svgSalamander:1.1.2"
    const val thread_inheritable_resource_loader: String = "org.danilopianini:thread-inheritable-resource-loader:_"
    const val tornadofx: String = "no.tornado:tornadofx:_"
    const val trove4j: String = "net.sf.trove4j:trove4j:_"

    /**
     * Returns a reference to an alchemist sub-project [module].
     */
    fun Project.alchemist(module: String) = project(":alchemist-$module")

    /**
     * Returns a reference to an alchemist sub-project incarnation [module].
     */
    fun Project.incarnation(module: String) = alchemist("incarnation-$module")

    private fun modularizedLibrary(base: String, module: String = "", separator: String = "-") = when {
        module.isEmpty() -> base
        else -> base + separator + module
    } + ":_"

    private fun oldApache(module: String) = "commons-$module:commons-$module:_"

    /**
     * Returns the identifier of the desired ArrowKt [module].
     */
    fun arrowKt(module: String) = modularizedLibrary("io.arrow-kt:arrow", module)

    /**
     * Returns the identifier of the desired Apache-Commons [module].
     */
    fun apacheCommons(module: String) = when (module) {
        in setOf("cli", "io", "codec") -> oldApache(module)
        else -> modularizedLibrary("org.apache.commons:commons", module)
    }

    /**
     * Returns the identifier of the desired GraphHopper [module].
     */
    fun graphhopper(module: String) = modularizedLibrary("com.graphhopper:graphhopper", module)

    /**
     * Returns the identifier of the desired GraphStream [module].
     */
    fun graphStream(module: String = "") = modularizedLibrary("org.graphstream:gs", module)

    /**
     * Returns the identifier of the desired JGraphT [module].
     */
    fun jgrapht(module: String = "") = modularizedLibrary("org.jgrapht:jgrapht", module)

    /**
     * Returns the identifier of the desired JUnit [module].
     */
    fun junit(module: String) = modularizedLibrary("org.junit.jupiter:junit-jupiter", module)

    /**
     * Returns the identifier of the desired Protelis [module].
     */
    fun protelis(module: String = "") = modularizedLibrary("org.protelis:protelis", module)

    /**
     * Returns the identifier of the desired Scala [module].
     */
    fun scalaModule(module: String = "") = modularizedLibrary("org.scala-lang:scala", module)
}
