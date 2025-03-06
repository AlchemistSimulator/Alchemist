/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.protelis

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.protelis.actions.RunProtelisProgram
import it.unibo.alchemist.model.protelis.actions.SendToNeighbor
import it.unibo.alchemist.model.protelis.conditions.ComputationalRoundComplete
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice
import it.unibo.alchemist.model.reactions.ChemicalReaction
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import org.protelis.lang.ProtelisLoader
import org.protelis.lang.datatype.DeviceUID
import org.protelis.vm.CodePath
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.ProtelisVM
import org.protelis.vm.impl.AbstractExecutionContext
import org.protelis.vm.impl.SimpleExecutionEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serial
import java.lang.ref.WeakReference
import java.util.Objects
import java.util.Optional
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * @param <P> position type
</P> */
class ProtelisIncarnation<P : Position<P>> : Incarnation<Any, P> {
    private val cache: LoadingCache<CacheKey, SynchronizedVM> =
        CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                object : CacheLoader<CacheKey, SynchronizedVM>() {
                    override fun load(
                        @Nonnull key: CacheKey,
                    ): SynchronizedVM = SynchronizedVM(requireNotNull(key))
                },
            )

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        node: Node<Any>?,
        time: TimeDistribution<Any>,
        actionable: Actionable<Any>,
        additionalParameters: Any?,
    ): Action<Any> {
        val parameters = additionalParameters?.toString().orEmpty()
        require(actionable is Reaction<*>) {
            "The provided actionable must be an instance of ${Reaction::class.simpleName}"
        }
        requireNotNull(additionalParameters)
        val device =
            requireNotNull(node?.asProperty<Any, ProtelisDevice<P>>()) {
                "The node must be a ${ProtelisDevice::class.simpleName}"
            }
        return if (parameters.equals("send", ignoreCase = true)) {
            val alreadyDone =
                node.reactions
                    .asSequence()
                    .flatMap { it.actions.asSequence() }
                    .filterIsInstance<SendToNeighbor>()
                    .map { it.protelisProgram }
                    .toSet()
            val pList = getIncomplete(node, alreadyDone)
            check(pList.isNotEmpty()) { "There is no program requiring a ${SendToNeighbor::class.simpleName} action" }
            check(pList.size == 1) {
                "There are too many programs requiring a ${SendToNeighbor::class.qualifiedName} action: $pList"
            }
            SendToNeighbor(node, actionable as Reaction<Any>, pList.first())
        } else {
            @Suppress("TooGenericExceptionCaught")
            try {
                RunProtelisProgram(randomGenerator, environment, device, actionable as Reaction<Any>, parameters)
            } catch (exception: RuntimeException) {
                throw IllegalArgumentException(
                    "Could not create the requested Protelis program: $additionalParameters",
                    exception,
                )
            }
        }
    }

    override fun createConcentration(descriptor: Any?): Any? =
        try {
            descriptor?.toString()?.let { program ->
                SynchronizedVM(CacheKey(NoNode, createMolecule(program), program)).runCycle()
            } ?: descriptor
        } catch (e: IllegalArgumentException) {
            LOGGER.warn("Invalid Protelis program injected as concentration:\n{}", descriptor, e)
            descriptor
        }

    override fun createConcentration(): Any? = null

    override fun createCondition(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        node: Node<Any>?,
        time: TimeDistribution<Any>,
        actionable: Actionable<Any>,
        additionalParameters: Any?,
    ): Condition<Any> {
        if (actionable is Reaction<*>) {
            requireNotNull(node) {
                "Global protelis programs are not supported"
            }
            requireNotNull(node.asPropertyOrNull<Any, ProtelisDevice<*>>()) {
                "Node $node must be a Protelis device"
            }
            /*
             * The list of ProtelisPrograms that have already been completed with a ComputationalRoundComplete condition
             */
            val alreadyDone =
                node.reactions
                    .asSequence()
                    .flatMap { r: Reaction<Any> -> r.conditions.asSequence() }
                    .filter { c: Condition<Any> -> c is ComputationalRoundComplete }
                    .map { c: Condition<Any> -> (c as ComputationalRoundComplete).program }
                    .toSet()
            val pList: List<RunProtelisProgram<*>> = getIncomplete(node, alreadyDone)
            check(!pList.isEmpty()) {
                "There is no program requiring a " +
                    ComputationalRoundComplete::class.java.getSimpleName() +
                    " condition"
            }
            check(pList.size <= 1) {
                "There are too many programs requiring a " + ComputationalRoundComplete::class.java.getSimpleName() +
                    " condition: " + pList
            }
            return ComputationalRoundComplete(node, pList[0])
        }
        throw IllegalArgumentException(
            "The provided actionable should be an instance of " + Reaction::class.java.getSimpleName(),
        )
    }

    override fun createMolecule(s: String?): Molecule = SimpleMolecule(Objects.requireNonNull<String?>(s))

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        parameter: Any?,
    ): Node<Any> =
        GenericNode<Any>(this, environment).apply {
            addProperty(ProtelisDevice(environment, this))
        }

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        node: Node<Any>,
        timeDistribution: TimeDistribution<Any>,
        parameter: Any?,
    ): Reaction<Any> {
        val parameterString = parameter?.toString()
        val isSend = parameterString.equals("send", ignoreCase = true)
        val result: Reaction<Any> =
            if (isSend) {
                ChemicalReaction(
                    node,
                    timeDistribution,
                )
            } else {
                Event(node, timeDistribution)
            }
        parameter?.let {
            result.actions =
                listOf(createAction(randomGenerator, environment, node, timeDistribution, result, it))
        }
        if (isSend) {
            result.conditions =
                listOf(createCondition(randomGenerator, environment, node, timeDistribution, result, null))
        }
        return result
    }

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, P>,
        node: Node<Any>?,
        parameter: Any?,
    ): TimeDistribution<Any> =
        try {
            val frequency =
                parameter?.let { (it as? Number)?.toDouble() ?: it.toString().toDouble() }
                    ?: return ExponentialTime(Double.POSITIVE_INFINITY, randomGenerator)
            DiracComb(DoubleTime(randomGenerator.nextDouble() / frequency), frequency)
        } catch (e: NumberFormatException) {
            LOGGER.error("Unable to convert {} to a double", parameter)
            throw e
        }

    override fun getProperty(
        node: Node<Any>,
        molecule: Molecule,
        property: String?,
    ): Double =
        @Suppress("TooGenericExceptionCaught")
        try {
            val vm = cache.get(CacheKey(requireNotNull(node), requireNotNull(molecule), property.orEmpty()))
            when (val result = vm.runCycle()) {
                is Number -> result.toDouble()
                is String -> result.toDoubleOrNull() ?: if (result == property) 1.0 else 0.0
                is Boolean -> if (result) 1.0 else 0.0
                else -> Double.NaN
            }
        } catch (e: Exception) {
            LOGGER.error("Intercepted interpreter exception when computing: \n{}\n{}", property, e.message)
            Double.NaN
        }

    override fun toString(): String = this::class.simpleName ?: this::class.java.simpleName

    private class CacheKey(
        node: Node<Any>,
        val molecule: Molecule,
        val property: String,
    ) {
        private val nodeRef = WeakReference(node)
        private val hash = Objects.hash(molecule, property, node)

        val node: Node<Any> get() =
            checkNotNull(nodeRef.get()) {
                "Memory management issue: a Protelis node has been garbage-collected while still in use."
            }

        override fun equals(other: Any?) =
            other is CacheKey &&
                other.nodeRef.get() === nodeRef.get() &&
                other.molecule == molecule &&
                other.property == property

        override fun hashCode(): Int = hash
    }

    /**
     * An [org.protelis.vm.ExecutionContext] that operates over a node but does not modify it.
     */
    private class DummyContext(
        private val node: Node<Any>,
    ) : AbstractExecutionContext<DummyContext?>(
            ProtectedExecutionEnvironment(node),
            object : NetworkManager {
                override fun getNeighborState(): Map<DeviceUID, Map<CodePath, Any>> = emptyMap()

                override fun shareState(toSend: Map<CodePath, Any>) = Unit
            },
        ) {
        override fun getCurrentTime(): Number = 0

        override fun getDeviceUID(): DeviceUID = node.asPropertyOrNull<Any, ProtelisDevice<*>>() ?: NO_NODE_ID

        override fun instance(): DummyContext = this

        override fun nextRandomDouble(): Double =
            MUTEX.run {
                acquireUninterruptibly()
                RNG.nextDouble().also { release() }
            }

        companion object {
            private val MUTEX = Semaphore(1)
            private val RNG = MersenneTwister(-241837578)
            private val NO_NODE_ID =
                object : DeviceUID {
                    override fun toString() =
                        "Node wrapper for non-ProtelisDevices, meant to host local-only computation."
                }
        }
    }

    /**
     * An [ExecutionEnvironment] that can read and shadow the content of a
     * Node, but cannot modify it. This is used to prevent badly written
     * properties from interacting with the simulation flow.
     */
    class ProtectedExecutionEnvironment
        /**
         * @param node the [Node]
         */
        @SuppressFBWarnings(value = ["EI_EXPOSE_REP2"], justification = "This is intentional")
        constructor(
            private val node: Node<*>,
        ) : ExecutionEnvironment {
            private val shadow: ExecutionEnvironment = SimpleExecutionEnvironment()

            override fun commit() = Unit

            override fun get(id: String): Any? = shadow[id, node.getConcentration(SimpleMolecule(id))]

            override fun get(
                id: String,
                defaultValue: Any,
            ): Any = get(id) ?: defaultValue

            override fun has(id: String): Boolean = shadow.has(id) || node.contains(SimpleMolecule(id))

            override fun put(
                id: String,
                v: Any,
            ): Boolean = shadow.put(id, v)

            override fun remove(id: String): Any? = shadow.remove(id)

            override fun setup() = Unit

            override fun keySet(): Set<String> =
                node.contents.keys
                    .map { it.getName() }
                    .toSet() + shadow.keySet()
        }

    private class SynchronizedVM(
        val key: CacheKey,
    ) {
        val mutex = Semaphore(1)
        val vm: Optional<ProtelisVM> =
            key.property
                .takeIf { it.isNotBlank() }
                ?.let {
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        val baseProgram = "env.get(\"${key.molecule.name}\")"
                        ProtelisVM(ProtelisLoader.parse(it.replace(VALUE_TOKEN, baseProgram)), DummyContext(key.node))
                    } catch (ex: RuntimeException) {
                        LOGGER.warn("Program ignored as invalid: \n{}", key.property)
                        LOGGER.debug("Debug information", ex)
                        null
                    }
                }.let { Optional.ofNullable(it) }

        fun runCycle(): Any {
            val node = checkNotNull(key.node) { "The node should never be null" }
            vm.orElse(null)?.let {
                mutex.acquireUninterruptibly()
                return try {
                    it.runCycle()
                    it.currentValue
                } finally {
                    mutex.release()
                }
            }
            return if (node is NoNode) key.property else node.getConcentration(key.molecule)
        }
    }

    private object NoNode : Node<Any> {
        @Serial
        private const val serialVersionUID = 1L

        override val contents: MutableMap<Molecule, Any> get() = notImplemented()

        override val id: Int get() = notImplemented()

        override val moleculeCount: Int get() = notImplemented()

        override val properties: List<NodeProperty<Any>> = emptyList()

        override val reactions: List<Reaction<Any>> = emptyList()

        override fun iterator(): MutableIterator<Reaction<Any>> = notImplemented<MutableIterator<Reaction<Any>>>()

        override fun compareTo(
            @Nonnull o: Node<Any>,
        ): Int = notImplemented()

        override fun addReaction(r: Reaction<Any>) = notImplemented<Unit>()

        override fun cloneNode(currentTime: Time): Node<Any> = notImplemented()

        override fun contains(mol: Molecule): Boolean = notImplemented()

        override fun getConcentration(mol: Molecule): Any = notImplemented()

        override fun removeConcentration(mol: Molecule) = notImplemented<Unit>()

        override fun removeReaction(r: Reaction<Any>) = notImplemented<Unit>()

        override fun setConcentration(
            molecule: Molecule,
            concentration: Any,
        ) = notImplemented<Unit>()

        override fun equals(obj: Any?): Boolean = obj === this

        override fun hashCode(): Int = -1

        override fun addProperty(nodeProperty: NodeProperty<Any>) = notImplemented<Unit>()

        @Suppress("UnusedPrivateMember")
        private fun readResolve(): Any = NoNode

        private fun <A> notImplemented(): A =
            throw UnsupportedOperationException("Method can't be invoked in this context.")
    }

    /**
     * Constants and utilities for the Protelis incarnation.
     */
    companion object {
        /**
         * The name that can be used in a property to refer to the extracted value.
         */
        const val VALUE_TOKEN: String = "<value>"

        /**
         * Statically referencable instance.
         * This incarnation *can* work as a singleton, and doing so may save some memory.
         * However, it is not strictly a singleton (multiple instances do not do harm).
         */
        val INSTANCE: ProtelisIncarnation<*> = ProtelisIncarnation()

        private val LOGGER: Logger = LoggerFactory.getLogger(ProtelisIncarnation::class.java)

        @Nonnull
        private fun getIncomplete(
            protelisNode: Node<*>,
            alreadyDone: Set<RunProtelisProgram<*>>,
        ): List<RunProtelisProgram<*>> =
            protelisNode.reactions
                .asSequence()
                // Get the actions
                .flatMap { it.actions.asSequence() }
                // Get only the ProtelisPrograms
                .filterIsInstance<RunProtelisProgram<*>>()
                // Retain only those ProtelisPrograms that have no associated ComputationalRoundComplete.
                // Only one should be available.
                .filter { !alreadyDone.contains(it) }
                .toList()
    }
}
