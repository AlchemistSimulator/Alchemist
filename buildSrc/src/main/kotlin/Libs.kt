import org.gradle.api.Project
import kotlin.String

/**
 * Statically defined libraries used by the project.
 */
@Suppress("UndocumentedPublicProperty")
object Libs {
    const val boilerplate: String = "org.danilopianini:boilerplate:_"
    const val conrec: String = "org.danilopianini:conrec:_"
    const val controlsfx: String = "org.controlsfx:controlsfx:_"
    const val java_quadtree: String = "org.danilopianini:java-quadtree:_"
    const val javalib_java7: String = "org.danilopianini:javalib-java7:_"
    const val jirf: String = "org.danilopianini:jirf:_"
    const val jool: String = "org.jooq:jool:_"
    const val listset: String = "org.danilopianini:listset:_"
    const val mapsforge_map_awt: String = "org.mapsforge:mapsforge-map-awt:_"
    const val slf4j_api: String = "org.slf4j:slf4j-api:_"
    const val snakeyaml: String = "org.yaml:snakeyaml:_"
    const val ssaring_sportstracker_leafletmap: String = "org.danilopianini:de.saring.leafletmap:_"
    const val svgsalamander: String = "guru.nidi.com.kitfox:svgSalamander:1.1.2"
    const val thread_inheritable_resource_loader: String = "org.danilopianini:thread-inheritable-resource-loader:_"

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

    /**
     * Returns the identifier of the desired GraphStream [module].
     */
    fun graphStream(module: String = "") = modularizedLibrary("org.graphstream:gs", module)

    /**
     * Returns the identifier of the desired JGraphT [module].
     */
    fun jgrapht(module: String = "") = modularizedLibrary("org.jgrapht:jgrapht", module)

    /**
     * Returns the identifier of the desired Protelis [module].
     */
    fun protelis(module: String = "") = modularizedLibrary("org.protelis:protelis", module)

    /**
     * Returns the identifier of the desired Scala [module].
     */
    fun scalaModule(module: String = "") = modularizedLibrary("org.scala-lang:scala", module)
}
