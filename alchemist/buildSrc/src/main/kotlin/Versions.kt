/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val logback_classic: String = "1.3.0-alpha4" 

    const val com_eden_orchidplugin_gradle_plugin: String = "0.17.1" 

    const val caffeine: String = "2.7.0" 

    const val com_github_cb372: String = "0.9.3" // available: "0.28.0"

    const val rtree: String = "0.8.7"

    const val com_github_maiflai_scalatest_gradle_plugin: String = "0.25" 

    const val com_github_spotbugs_gradle_plugin: String = "1.6.9" // available: "2.0.0"

    const val spotbugs: String = "3.1.12" 

    const val gson: String = "2.8.5" 

    const val guava: String = "28.0-jre"

    const val concurrentlinkedhashmap_lru: String = "1.4.2" 

    const val com_gradle_build_scan_gradle_plugin: String = "2.3" 

    const val com_graphhopper: String = "0.12.0" // available: "0.13.0-tardur1"

    const val simplelatlng: String = "1.3.1" 

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.4" 

    const val miglayout_swing: String = "5.2" 

    const val ktlint: String = "0.32.0" // available: "0.33.0"

    const val konf: String = "0.13.3" 

    const val commons_cli: String = "1.4" 

    const val commons_codec: String = "1.12" 

    const val commons_io: String = "2.6" 

    const val javafxsvg: String = "1.3.0" 

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" 

    const val classgraph: String = "4.8.37" // available: "4.8.41"

    const val io_github_javaeden_orchid: String = "0.17.1" 

    const val jpx: String = "1.4.0" 

    const val kotlintest_runner_junit5: String = "3.3.2" 

    const val scafi_core_2_12: String = "0.3.2" // available: "53ddebd1"

    const val trove4j: String = "3.0.3" 

    const val org_antlr: String = "4.6" // available: "4.7.2"

    const val bcel: String = "6.3.1" 

    const val commons_lang3: String = "3.9" 

    const val commons_math3: String = "3.6.1" 

    const val org_apache_ignite: String = "2.7.0" // available: "2.7.5"

    const val groovy: String = "2.5.7" 

    const val controlsfx: String = "9.0.0" // available: "11.0.0"

    const val org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin: String = "0.2.2" 

    const val org_danilopianini_publish_on_central_gradle_plugin: String = "0.1.1" 

    const val boilerplate: String = "0.2.1" 

    const val gson_extras: String = "0.2.1" 

    const val java_quadtree: String = "0.1.2" 

    const val javalib_java7: String = "0.6.1" 

    const val jirf: String = "0.2.0" 

    const val listset: String = "0.2.4" 

    const val thread_inheritable_resource_loader: String = "0.3.0" 

    const val org_jetbrains_dokka_gradle_plugin: String = "0.9.17" // available: "0.9.18"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.40"

    const val org_jetbrains_kotlin: String = "1.3.40"

    const val annotations: String = "17.0.0" 

    const val jgrapht_core: String = "1.3.1" 

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "8.1.0"

    const val jool_java_8: String = "0.9.14" 

    const val org_junit_jupiter: String = "5.4.2" 

    const val mapsforge_map_awt: String = "0.11.0" 

    const val org_openjfx: String = "11" // available: "13-ea+9"

    const val parboiled_java: String = "1.3.0" // available: "1.3.1"

    const val pegdown: String = "1.6.0" 

    const val org_protelis: String = "12.2.0"

    const val org_scala_lang: String = "2.12.2" // available: "2.13.0"

    const val scalatest_2_12: String = "3.0.1" // available: "3.2.0-SNAP10"

    const val slf4j_api: String = "1.8.0-beta2" 

    const val snakeyaml: String = "1.24" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.4.1"

        const val currentVersion: String = "5.4.1"

        const val nightlyVersion: String = "5.6-20190627000039+0000"

        const val releaseCandidate: String = "5.5-rc-4"
    }
}
