/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.Feature
import com.uchuhimo.konf.source.yaml
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.konf.EntityMapper.buildIncarnation
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Position
import org.danilopianini.jirf.Factory
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.reflect.Modifier

class KonfBasedLoader(spec: String, specType: SupportedSpecType) : Loader {

    private val rawIncarnation: Incarnation<*, *>
    @Suppress("UNCHECKED_CAST")
    fun <T, P: Position<P>> getIncarnation(): Incarnation<T, P> = rawIncarnation as Incarnation<T, P>

    init {
        val config = with (specType) {
            Config { addSpec(AlchemistSpec) }
                .enable(Feature.FAIL_ON_UNKNOWN_PATH)
                .from.selectedSpecType.string(spec)
        }
        val factory = ObjectFactory.makeBaseFactory()
        rawIncarnation = config[AlchemistSpec.incarnation].buildIncarnation<Any?, Nothing>(factory)
    }


    override fun <T : Any?, P : Position<P>?> getDefault(): Environment<T, P> {
        TODO("Not yet implemented")
    }

    override fun getDependentVariables(): MutableMap<String, DependentVariable<*>> {
        TODO("Not yet implemented")
    }

    override fun getVariables(): MutableMap<String, Variable<*>> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?, P : Position<P>?> getWith(values: MutableMap<String, *>?): Environment<T, P> {
        TODO("Not yet implemented")
    }

    override fun getConstants(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getDataExtractors(): MutableList<Extractor> {
        TODO("Not yet implemented")
    }

    override fun getDependencies(): MutableList<String> {
        TODO("Not yet implemented")
    }

    companion object {
        val logger = LoggerFactory.getLogger(KonfBasedLoader::class.java)
    }
}

object AlchemistSpec : ConfigSpec("") {
    val incarnation by required<ConstructorOrString>()
    val environment by optional(JVMConstructor.createConstructor(Continuous2DEnvironment::class.java.simpleName))
}

object JVMConstructorspec : ConfigSpec("") {
    val type by required<String>()
    val parameters by optional<List<Any>>(emptyList())
}

@Suppress("UNCHECKED_CAST")
private object EntityMapper {

    fun <T, P : Position<P>> ConstructorOrString.buildIncarnation(factory: Factory): Incarnation<T, P> =
        string?.let { factory.convert(Incarnation::class.java, it).orElseThrow() as Incarnation<T, P> }
            ?: constructor!!.newInstance(factory)

    inline fun <reified T : Any> ConstructorOrString.buildAny(factory: Factory): T {
        if (string != null) {
            return factory.convert(T::class.java, string).orElseThrow()
        }
        val type = constructor!!.typeName
        val hasPackage = type.contains('.')
        val subtypes = ClassPathScanner.subTypesOf<T>().filter { !Modifier.isAbstract(it.modifiers) }
        val perfectMatches = subtypes.filter { constructor.typeName == if (hasPackage) it.name else it.simpleName }
        when (perfectMatches.size) {
            0 -> KonfBasedLoader.logger.warn("No perfect match for type {} in {}", type, subtypes.map { it.name })
            1 -> return constructor.newInstance(perfectMatches.first().kotlin, factory)
            else -> throw IllegalStateException(
                "Multiple perfect matches for $type: ${perfectMatches.map { it.name }}"
            )
        }
        val subOptimalMatches = subtypes.filter {
            constructor.typeName.equals(if (hasPackage) it.name else it.simpleName, ignoreCase = true)
        }
        return when (subOptimalMatches.size) {
            0 -> throw IllegalStateException(
                """
                No valid match for type $type among subtypes of ${T::class.simpleName}.
                Valid subtypes are: $subtypes
                """.trimMargin()
            )
            1 -> constructor.newInstance(subOptimalMatches.first().kotlin, factory)
            else ->  throw IllegalStateException(
                "Multiple matches for $type as subtype of ${T::class.simpleName}: ${perfectMatches.map { it.name }}. " +
                    "Disambiguation is required."
            )
        }
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
