/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.m2m

import arrow.core.valid
import com.google.gson.GsonBuilder
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

internal interface SyntaxElement {

    val validKeys: List<String> get() = this::class.declaredMemberProperties
        .filter { it.returnType == String::class.createType() }
        .map { if (it.isConst) it.getter.call() else it.getter.call(this) }
        .map { it.toString() }

    val validDescriptors: Set<ValidDescriptor>

    val guide get() = "Possible configurations are:" +
        validDescriptors.foldIndexed("") { index, previous, element ->
            "$previous\n## Option ${index + 1}:\n$element"
        }

    /**
     * Validates a candidate [descriptor] for a SyntaxElement.
     * If at least one of its [validDescriptors] mandatory keys match,
     * then such a structure is mandated, and an exception is thrown if the syntax is incorrect;
     * otherwise, `true` is returned.
     *
     * If none of the [validDescriptors] match, the function returns `false`.
     */
    fun validateDescriptor(descriptor: Map<*, *>): Boolean {
        val publicKeys = descriptor.keys.asSequence()
            .filterNotNull()
            .map { it.toString() }
            .filterNot { it.startsWith("_") }
            .toSet()
        val problematicSegment by lazy {
            "Problematic segment:\n|" +
                GsonBuilder().setPrettyPrinting().create().toJson(descriptor.mapValues { "..." })
        }
        for (validDescriptor in validDescriptors) {
            val forbidden = validDescriptor.forbiddenKeys.filter { descriptor.containsKey(it) }
            val typeName = this::class.simpleName
            require(forbidden.isEmpty()) {
                """
                |Forbidden keys for $typeName detected: $forbidden.
                |$guide
                |$problematicSegment
                """.trimMargin()
            }
            if (validDescriptor.mandatoryKeys.all { descriptor.containsKey(it) }) {
                val unkownKeys = publicKeys - validDescriptor.mandatoryKeys - validDescriptor.optionalKeys
                require(unkownKeys.isEmpty()) {
                    val matched = descriptor.keys.intersect(validDescriptor.mandatoryKeys)
                    """
                    |Unknown keys $unkownKeys for the provided $typeName descriptor:
                    |$problematicSegment
                    |$typeName syntax was assigned becaused the following mandatory key were detected: $matched.$guide
                    |If you need private keys (e.g. for internal use), prefix them with underscore (_)
                    """.trimMargin()
                }
                return true
            }
        }
        return false
    }

    data class ValidDescriptor(
        val mandatoryKeys: Set<String>,
        val optionalKeys: Set<String> = setOf(),
        val forbiddenKeys: Set<String> = setOf(),
    ) {
        override fun toString(): String {
            fun Set<String>.lines() = joinToString(prefix = "\n  - ", separator = "\n  - ")
            fun Set<String>.describe(name: String) = if (this.isEmpty()) "" else "\n$name keys: ${this.lines()}"
            return mandatoryKeys.describe("required").drop(1) +
                optionalKeys.describe("optional") +
                forbiddenKeys.describe("forbidden")
        }
    }
}

@Suppress("SuspiciousCollectionReassignment")
internal class DescriptorBuilder {
    private var forbiddenKeys = emptySet<String>()
    private var mandatoryKeys = emptySet<String>()
    private var optionalKeys = emptySet<String>()
    fun forbidden(vararg names: String) {
        forbiddenKeys += names.toSet()
    }
    fun mandatory(vararg names: String) {
        mandatoryKeys += names.toSet()
    }
    fun optional(vararg names: String) {
        optionalKeys += names.toSet()
    }
    fun build() = SyntaxElement.ValidDescriptor(mandatoryKeys, optionalKeys, forbiddenKeys)
}

private fun validDescriptor(configuration: DescriptorBuilder.() -> Unit): SyntaxElement.ValidDescriptor =
    DescriptorBuilder().apply(configuration).build()

internal object DocumentRoot : SyntaxElement {
    object JavaType : SyntaxElement {
        val type by OwnName()
        val parameters by OwnName()
        override val validDescriptors = setOf(
            validDescriptor {
                mandatory(type)
                optional(parameters)
            }
        )
    }
    object DependentVariable : SyntaxElement {
        val language by OwnName()
        val formula by OwnName()
        override val validDescriptors = JavaType.validDescriptors + setOf(
            validDescriptor {
                mandatory(formula)
                optional(language)
            }
        )
    }
    object Deployment : SyntaxElement {
        val contents by OwnName()
        val nodes by OwnName()
        val programs by OwnName()
        override val validDescriptors = setOf(
            validDescriptor {
                mandatory(JavaType.type)
                optional(JavaType.parameters, contents, nodes, programs)
                forbidden(Contents.shapes)
            }
        )
        object Contents : SyntaxElement {
            val molecule by OwnName()
            val concentration by OwnName()
            const val shapes = "in"
            override val validDescriptors = setOf(
                validDescriptor {
                    mandatory(molecule, concentration)
                    optional(shapes)
                }
            )
        }
        object Program : SyntaxElement {
            val program by OwnName()
            val actions by OwnName()
            val conditions by OwnName()
            const val timeDistribution = "time-distribution"
            override val validDescriptors = setOf(
                validDescriptor {
                    mandatory(JavaType.type)
                    optional(JavaType.parameters, conditions, timeDistribution, actions)
                },
                validDescriptor {
                    mandatory(program)
                    optional(timeDistribution)
                }
            )
        }
    }
    /*
    object Export : SyntaxElement {
        val time by OwnName()
        val molecule by OwnName()
        val property by OwnName()
        val aggregators by OwnName()
        const val valueFilter = "value-filter"
        override val validDescriptors = JavaType.validDescriptors + setOf(
            validDescriptor { mandatory(time) },
            validDescriptor {
                mandatory(molecule)
                optional(property, aggregators, valueFilter)
            }
        )
    }*/

    object Export : SyntaxElement{
        val data by OwnName()

        override val validDescriptors = JavaType.validDescriptors + setOf(
            validDescriptor { mandatory(JavaType.type, JavaType.parameters, data) },
        )

        object Data : SyntaxElement {
            val time by OwnName()
            val molecule by OwnName()
            val property by OwnName()
            val aggregators by OwnName()
            const val valueFilter = "value-filter"
            override val validDescriptors = JavaType.validDescriptors + setOf(
                validDescriptor { mandatory(time) },
                validDescriptor {
                    mandatory(molecule)
                    optional(property, aggregators, valueFilter)
                }
            )
        }

    }

    object Layer : SyntaxElement {
        val molecule by OwnName()
        override val validDescriptors = setOf(
            validDescriptor {
                mandatory(JavaType.type, molecule)
                optional(JavaType.parameters)
            }
        )
    }
    object Seeds : SyntaxElement {
        val scenario by OwnName()
        val simulation by OwnName()
        override val validDescriptors = setOf(
            validDescriptor { optional(simulation, scenario) }
        )
    }
    object Variable : SyntaxElement {
        val min by OwnName()
        val max by OwnName()
        val default by OwnName()
        val step by OwnName()
        override val validDescriptors = JavaType.validDescriptors + setOf(
            validDescriptor { mandatory(min, max, default, step) }
        )
    }
    val deployments by OwnName()
    val environment by OwnName()
    val export by OwnName()
    val incarnation by OwnName()
    val layers by OwnName()
    const val linkingRule = "network-model"
    const val remoteDependencies = "remote-dependencies"
    val seeds by OwnName()
    val terminate by OwnName()
    val variables by OwnName()
    override val validDescriptors = setOf(
        validDescriptor {
            mandatory(incarnation)
            optional(*validKeys.toTypedArray())
        }
    )
}

internal class OwnName {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = property.name.lowercase()
}
