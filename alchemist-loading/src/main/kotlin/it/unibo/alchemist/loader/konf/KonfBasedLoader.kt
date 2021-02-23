/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import arrow.core.Either
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.konf.EntityMapper.buildAny
import it.unibo.alchemist.loader.konf.EntityMapper.buildIncarnation
import it.unibo.alchemist.loader.konf.types.ConstructorOrString
import it.unibo.alchemist.loader.konf.types.JVMConstructor
import it.unibo.alchemist.loader.konf.types.VariableDescriptor
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.lang.reflect.Modifier

class KonfBasedLoader(spec: String, specType: SupportedSpecType) : Loader {

    private val rawIncarnation: Incarnation<*, *>
    private val environmentSpec: JVMConstructor
    private val dependentVariables: Map<String, DependentVariable<*>>
    private val freeVariables: Map<String, Variable<*>>
    private val constants: Map<String, Any>


    init {
        val config = with (specType) {
            Config { addSpec(AlchemistSpec) }
//                .enable(Feature.FAIL_ON_UNKNOWN_PATH)
                .from.selectedSpecType.string(spec)
        }
        val factory = ObjectFactory.makeBaseFactory()
        rawIncarnation = config[AlchemistSpec.incarnation].buildIncarnation<Any?, Nothing>(factory)
        environmentSpec = config[AlchemistSpec.environment]
        /*
         * Compute constants and dependent variables
         */
        val (freeVariableDescriptors, dependentVariableDescriptors) = config[AlchemistSpec.variables].entries
            .asSequence()
            .map { (name, descriptor) ->
                name to descriptor.build(factory)
            }
            .partition { (_, descriptor) -> descriptor is Either.Left<*> }
        println(dependentVariableDescriptors)
        constants = emptyMap()
        dependentVariables = emptyMap()
        @Suppress("UNCHECKED_CAST")
        freeVariables = freeVariableDescriptors
            .asSequence()
            .map { it as Pair<String, Either.Left<Variable<*>>> }
            .map { (name, descriptor) -> name to descriptor.a }
            .toMap()
//        val constants: Map<String, Any> = Maps.newLinkedHashMapWithExpectedSize(originalVars.size)
//        val depVariables: Map<String, DependentVariable<*>> = Maps.newLinkedHashMapWithExpectedSize(originalVars.size)
//        var previousConstants: Int
//        var previousDepVars: Int
//        do {
//            previousConstants = constants.size
//            previousDepVars = depVariables.size
//            val iter: MutableIterator<Map.Entry<String, Map<String, Any>>> = originalClone.entries.iterator()
//            while (iter.hasNext()) {
//                val entry = iter.next()
//                val name = entry.key
//                try {
//                    val dv: DependentVariable<*> = Objects.requireNonNull(depVarBuilder.build(entry.value))
//                    try {
//                        val value = dv.getWith(constants)
//                        iter.remove()
//                        constants[name] = value
//                        depVariables.remove(name)
//                    } catch (e: IllegalStateException) {
//                        YamlLoader.L.debug(
//                            """
//                        {} value could not be computed: maybe it depends on another,not yet initialized variable.
//                        Reason: {}
//                        """.trimIndent(),
//                            name,
//                            e
//                        )
//                        depVariables.put(name, dv)
//                    }
//                    YamlLoader.L.debug("Constant initialized in {}", constants)
//                } catch (e: IllegalAlchemistYAMLException) {
//                    YamlLoader.L.debug(
//                        """
//                    {} could not be created: its constructor may be requiring an uninitialized variable.
//                    Reason: {}
//                    """.trimIndent(),
//                        name,
//                        e
//                    )
//                }
//            }
//        } while (previousConstants != constants.size || previousDepVars != depVariables.size)
//        assert(constants.size + depVariables.size <= originalVars.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T, P: Position<P>> getIncarnation(): Incarnation<T, P> = rawIncarnation as Incarnation<T, P>

    override fun <T : Any?, P : Position<P>?> getDefault(): Environment<T, P> {
//        val factory = ObjectFactory.makeBaseFactory()
        TODO()
//        return environmentSpec.buildAny(factory)
    }

    override fun getDependentVariables(): Map<String, DependentVariable<*>> = dependentVariables

    override fun getVariables(): Map<String, Variable<*>> = freeVariables

    override fun <T : Any?, P : Position<P>?> getWith(values: Map<String, *>?): Environment<T, P> {
        TODO("Not yet implemented")
    }

    override fun getConstants(): Map<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getDataExtractors(): MutableList<Extractor> {
        TODO("Not yet implemented")
    }

    override fun getRemoteDependencies(): MutableList<String> {
        TODO("Not yet implemented")
    }

    companion object {
        val logger = LoggerFactory.getLogger(KonfBasedLoader::class.java)
    }
}

object AlchemistSpec : ConfigSpec("") {
    val incarnation by required<ConstructorOrString>()
    val environment by optional(JVMConstructor.create(Continuous2DEnvironment::class.java.simpleName))
    val variables by optional<Map<String, VariableDescriptor<*>>>(emptyMap())
}

object JVMConstructorspec : ConfigSpec("") {
    val type by required<String>()
    val parameters by optional<List<Any>>(emptyList())
}

@Suppress("UNCHECKED_CAST")
object EntityMapper {

    fun <T, P : Position<P>> ConstructorOrString.buildIncarnation(factory: Factory): Incarnation<T, P> =
        string?.let { factory.convert(Incarnation::class.java, it).orElseThrow() as Incarnation<T, P> }
            ?: constructor!!.buildAny(factory)

    inline fun <reified T : Any> ConstructorOrString.buildAny(factory: Factory): T =
        if (string != null) {
            factory.convert(T::class.java, string).orElseThrow()
        } else {
//            constructor!!.buildAny<T>(factory)
            TODO()
        }

}


// private class

fun main() {
    val config = Config {
        addSpec(AlchemistSpec)
    }
//        .enable(Feature.FAIL_ON_UNKNOWN_PATH)
        .from.yaml.string(
            """
#incarnation: protelis
incarnation:
  type: Asd
  parameters:
    pippo: 
    paperino: [asd]
environment:
  type: Continuous2DEnvironment
            """.trimIndent()
        )
    println(config[AlchemistSpec.incarnation])
    println(config[AlchemistSpec.environment])
}
