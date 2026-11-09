package it.unibo.alchemist.actions

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Capability
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.Collektive
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.path.FullPathFactory
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.jvm.kotlinFunction

/**
 * An Alchemist [Action] that runs a [Collektive] program.
 * Requires the hosting [reaction], a program [name], and the actual [program] to execute.
 */
class RunCollektiveProgram<P : Position<P>>(
    reaction: NodeReaction<Any?>,
    val name: String,
    val program: Aggregate<Int>.(CollektiveDevice<P>) -> Any?,
) : AbstractLocalAction<Any?>(reaction) {
    private val programIdentifier = SimpleMolecule(name)

    /**
     * The [CollektiveDevice] associated with the [targetNode].
     */
    val localDevice: CollektiveDevice<P> = targetNode.asProperty()

    /**
     * The [Collektive] program on which cycles will be executed.
     */
    val collektiveProgram: Collektive<Int, Any?>

    init {
        collektiveProgram =
            Collektive(localDevice.id, network = localDevice, pathFactory = FullPathFactory) {
                program(localDevice)
            }
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

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Any?>): Action<Any?> =
        RunCollektiveProgram(newReaction, name, program)

    override fun execute() {
        collektiveProgram.cycle().also {
            targetNode[programIdentifier] = it
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
