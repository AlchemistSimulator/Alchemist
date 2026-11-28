/*
 * Copyright (c) 2023-2026, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

package it.unibo.alchemist.actions

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Capability
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.collektive.Collektive
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.path.FullPathFactory
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.jvm.kotlinFunction

/**
 * An Alchemist [Action] that runs a [Collektive] program.
 * Requires a [reaction], a program [name], and the actual [program] to execute.
 */
class RunCollektiveProgram<P : Position<P>>(
    reaction: NodeReaction<Any?>,
    val name: String,
    val program: Aggregate<Int>.(CollektiveDevice<P>) -> Any?,
) : AbstractAction<Any?>(reaction) {

    /**
     * The [CollektiveDevice] associated with the [reaction] host node.
     */
    val localDevice: CollektiveDevice<P> = reaction.host.asProperty()

    /**
     * The [Collektive] program on which cycles will be executed.
     */
    val collektiveProgram: Collektive<Int, Any?> =
        Collektive(localDevice.id, network = localDevice, pathFactory = FullPathFactory) {
            program(localDevice)
        }

    /**
     * Create a [RunCollektiveProgram] with a specific [entrypoint] and a [reaction].
     */
    constructor(
        reaction: NodeReaction<Any?>,
        entrypoint: String,
    ) : this(reaction, entrypoint, findEntrypoint(entrypoint))

    /**
     * Create a [RunCollektiveProgram] with a specific [entrypoint] and a [reaction].
     */
    @JvmOverloads
    constructor(
        reaction: NodeReaction<Any?>,
        entrypoint: Method,
        name: String = entrypoint.name,
    ) : this(reaction, name, buildEntryPoint(entrypoint))

    override fun cloneAction(newReaction: Reaction<Any?>): Action<Any?> {
        require(newReaction is NodeReaction<Any?>) {
            "RunCollektiveProgram can only be cloned on a NodeReaction."
        }
        return RunCollektiveProgram(newReaction, name)
    }

    override fun execute() {
        collektiveProgram.cycle().also {
            localDevice[name] = it
        }
    }

    private companion object {
        private fun <P : Position<P>> findEntrypoint(
            entrypoint: String,
        ): Aggregate<Int>.(CollektiveDevice<P>) -> Any? {
            val className = entrypoint.substringBeforeLast(".")
            val methodName = entrypoint.substringAfterLast(".")
            val clazz = Class.forName(className)
            val method =
                clazz.methods.find { it.name == methodName }
                    ?: error("Entrypoint $entrypoint not found, no method $methodName found in class $className")
            return buildEntryPoint(method)
        }

        private fun <P : Position<P>> buildEntryPoint(method: Method): Aggregate<Int>.(CollektiveDevice<P>) -> Any? {
            val ktFunction =
                checkNotNull(method.kotlinFunction) {
                    "Method ${method.name} in class ${method.declaringClass.name}" +
                        " cannot be converted to a Kotlin function"
                }
            // Build the lambda function to be executed
            return { device: CollektiveDevice<P> ->
                val parameters =
                    method.parameters
                        .map {
                            when {
                                it.type.isAssignableFrom(Aggregate::class.java) -> this
                                it.type.isAssignableFrom(CollektiveDevice::class.java) -> device
                                it.type.isAssignableFrom(Node::class.java) -> device.node
                                device.node.hasPropertyCompatibleWith(it) -> device.node.getPropertyCompatibleWith(it)
                                else -> error("Unsupported type ${it.type} in entrypoint ${ktFunction.name}")
                            }
                        }.toTypedArray()
                ktFunction.call(*parameters)
            }
        }

        private fun Node<*>.hasPropertyCompatibleWith(parameter: Parameter): Boolean =
            properties.any { parameter.type.isAssignableFrom(it::class.java) }

        private fun Node<*>.getPropertyCompatibleWith(property: Parameter): Capability<*> =
            properties.first { property.type.isAssignableFrom(it::class.java) }
    }
}
