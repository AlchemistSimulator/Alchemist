/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.m2m

import it.unibo.alchemist.loader.EnvironmentAndExports
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.deployments.Deployment
import it.unibo.alchemist.loader.export.GenericExporter
import it.unibo.alchemist.model.implementations.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.implementations.linkingrules.NoLinks
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.jirf.Factory
import it.unibo.alchemist.loader.m2m.LoadingSystemLogger.logger
import java.lang.IllegalStateException
import java.util.concurrent.Semaphore
import java.util.function.Predicate

internal abstract class LoadingSystem(
    private val originalContext: Context,
    private val originalRoot: Map<String, *>
) : Loader {

    override fun <T : Any?, P : Position<P>> getWith(values: MutableMap<String, *>) =
        SingleUseLoader(originalContext, originalRoot).environmentWith<T, P>(values)

    private inner class SingleUseLoader(originalContext: Context, private val originalRoot: Map<String, *>) {

        private val context: Context = originalContext.child()
        private val mutex = Semaphore(1)
        private var consumed = false

        fun <T : Any?, P : Position<P>> environmentWith(values: Map<String, *>): EnvironmentAndExports<T, P> {
            try {
                mutex.acquireUninterruptibly()
                if (consumed) {
                    throw IllegalStateException("This loader has already been consumed! This is a bug in Alchemist")
                }
                consumed = true
            } finally {
                mutex.release()
            }
            val unknownVariableNames = values.keys - variables.keys
            require(unknownVariableNames.isEmpty()) {
                "Unknown variables provided: $unknownVariableNames." +
                    " Valid names: ${variables.keys}. Provided: ${values.keys}"
            }
            var root = originalRoot
            // VARIABLE REIFICATION
            val variableValues = variables.mapValues { (name, previous) ->
                if (values.containsKey(name)) values[name] else previous.default
            }
            val knownValues: Map<String, Any?> = computeAllKnownValues(constants + variableValues)
            logger.debug("Known values: {}", knownValues)
            knownValues.forEach { (name, value) -> context.fixVariableValue(name, value) }
            root = SimulationModel.inject(context, root)
            logger.debug("Complete simulation model: {}", root)
            // SEEDS
            val (scenarioRNG, simulationRNG) = SimulationModel.visitSeeds(context, root[DocumentRoot.seeds])
            setCurrentRandomGenerator(simulationRNG)
            // INCARNATION
            val incarnation = SimulationModel.visitIncarnation<P, T>(root[DocumentRoot.incarnation])
            registerSingleton<Incarnation<T, P>>(incarnation)
            registerImplicit<String, Molecule>(incarnation::createMolecule)
            registerImplicit<String, Any?>(incarnation::createConcentration)
            // ENVIRONMENT
            val environment: Environment<T, P> =
                SimulationModel.visitEnvironment(incarnation, context, root[DocumentRoot.environment])
            logger.info("Created environment: {}", environment)
            registerSingleton<Environment<T, P>>(environment)
            // LAYERS
            val layers: List<Pair<Molecule, Layer<T, P>>> =
                SimulationModel.visitLayers(incarnation, context, root[DocumentRoot.layers])
            layers.groupBy { it.first }
                .mapValues { (_, pair) -> pair.map { it.second } }
                .forEach { (molecule, layers) ->
                    require(layers.size == 1) {
                        "Inconsistent layer definition for molecule $molecule: $layers." +
                            "There must be a single layer per molecule"
                    }
                    val layer = layers.first()
                    environment.addLayer(molecule, layer)
                    logger.debug("Pushed layer {} -> {}", molecule, layer)
                }
            // LINKING RULE
            val linkingRule =
                SimulationModel.visitLinkingRule<P, T>(context, root.getOrEmptyMap(DocumentRoot.linkingRule))
            environment.linkingRule = linkingRule
            registerSingleton<LinkingRule<T, P>>(linkingRule)
            // DISPLACEMENTS
            setCurrentRandomGenerator(scenarioRNG)
            val displacementsSource = root.getOrEmpty(DocumentRoot.deployments)
            val deploymentDescriptors: List<Deployment<P>> =
                SimulationModel.visitRecursively(
                    context,
                    displacementsSource,
                    syntax = DocumentRoot.Deployment
                ) { element ->
                    (element as? Map<*, *>)?.let {
                        setCurrentRandomGenerator(scenarioRNG)
                        SimulationModel.visitBuilding<Deployment<P>>(context, element)?.onSuccess {
                            setCurrentRandomGenerator(simulationRNG)
                            populateDisplacement(simulationRNG, incarnation, environment, it, element)
                        }
                    }
                }
            setCurrentRandomGenerator(simulationRNG)
            val terminators: List<Predicate<Environment<T, P>>> =
                SimulationModel.visitRecursively(context, root.getOrEmpty(DocumentRoot.terminate)) { terminator ->
                    (terminator as? Map<*, *>)?.let { SimulationModel.visitBuilding(context, it) }
                }
            terminators.forEach(environment::addTerminator)
            if (deploymentDescriptors.isEmpty()) {
                logger.warn("There are no displacements in the specification, the environment won't have any node")
            } else {
                logger.debug("Deployment descriptors: {}", deploymentDescriptors)
            }
            // EXPORTS
            val exporters = SimulationModel.visitRecursively<GenericExporter<T,P>>(context, root.getOrEmpty(DocumentRoot.export)) {
                SimulationModel.visitSingleExporter(context, it)
            }
            return EnvironmentAndExports(environment, exporters)
        }

        private fun <T, P : Position<P>> populateDisplacement(
            simulationRNG: RandomGenerator,
            incarnation: Incarnation<T, P>,
            environment: Environment<T, P>,
            deployment: Deployment<P>,
            descriptor: Map<*, *>,
        ) {
            logger.debug("Processing deployment: {} with descriptor: {}", deployment, descriptor)
            val nodeDescriptor = descriptor[DocumentRoot.Deployment.nodes]
            if (descriptor.containsKey(DocumentRoot.Deployment.nodes)) {
                requireNotNull(nodeDescriptor) { "Invalid node type descriptor: $nodeDescriptor" }
                if (nodeDescriptor is Map<*, *>) {
                    DocumentRoot.JavaType.validateDescriptor(nodeDescriptor)
                }
            }
            // ADDITIONAL LINKING RULES
            deployment.getAssociatedLinkingRule<T>()?.let { newLinkingRule ->
                val composedLinkingRule = when (val linkingRule = environment.linkingRule) {
                    is NoLinks -> newLinkingRule
                    is CombinedLinkingRule -> CombinedLinkingRule(linkingRule.subRules + listOf(newLinkingRule))
                    else -> CombinedLinkingRule(listOf(linkingRule, newLinkingRule))
                }
                environment.linkingRule = composedLinkingRule
                registerSingleton<LinkingRule<T, P>>(composedLinkingRule)
            }
            val contents = SimulationModel.visitContents(incarnation, context, descriptor)
            val programDescriptor = descriptor.getOrEmpty(DocumentRoot.Deployment.programs)
            deployment.stream().forEach { position ->
                val node = SimulationModel.visitNode(simulationRNG, incarnation, environment, context, nodeDescriptor)
                registerSingleton<Node<T>>(node)
                // NODE CONTENTS
                contents.forEach { (shapes, molecule, concentrationMaker) ->
                    if (shapes.isEmpty() || shapes.any { position in it }) {
                        val concentration = concentrationMaker()
                        logger.debug("Injecting {} ==> {} in node {}", molecule, concentration, node.id)
                        node.setConcentration(molecule, concentration)
                    }
                }
                // PROGRAMS
                val programs = SimulationModel.visitRecursively<Reaction<T>>(
                    context,
                    programDescriptor,
                    DocumentRoot.Deployment.Program
                ) { program ->
                    requireNotNull(program) {
                        "null is not a valid program in $descriptor. ${DocumentRoot.Deployment.Program.guide}"
                    }
                    (program as? Map<*, *>)?.let {
                        SimulationModel.visitProgram(simulationRNG, incarnation, environment, node, context, it)
                            ?.onSuccess(node::addReaction)
                    }
                }
                logger.debug("Programs: {}", programs)
                environment.addNode(node, position)
                logger.debug("Added node {} at {}", node.id, position)
                factory.deregisterSingleton(node)
            }
        }

        private fun computeAllKnownValues(allVariableValues: Map<String, Any?>): Map<String, *> {
            val knownValues = allVariableValues.toMutableMap()
            var previousToVisitSize: Int? = null
            val toVisit = dependentVariables.toMutableMap()
            val failures = mutableListOf<Throwable>()
            while (toVisit.isNotEmpty() && toVisit.size != previousToVisitSize) {
                logger.debug("Variables to visit: {}", toVisit)
                failures.clear()
                previousToVisitSize = toVisit.size
                val iterator = toVisit.entries.iterator()
                while (iterator.hasNext()) {
                    val (name, variable) = iterator.next()
                    runCatching { variable.getWith(knownValues) }
                        .onSuccess { result ->
                            iterator.remove()
                            assert(previousToVisitSize != toVisit.size)
                            logger.debug("Created {}: {}", name, variable)
                            knownValues[name] = result
                        }
                        .onFailure { exception ->
                            failures.add(exception)
                            logger.debug("Could not create {}: {}", name, exception.message)
                        }
                }
            }
            failures.forEach { throw it }
            require(knownValues.size == constants.size + dependentVariables.size + variables.size) {
                val originalKeys = constants.keys + dependentVariables.keys + variables.keys
                val groups = listOf(knownValues.keys, originalKeys)
                val difference = groups.maxByOrNull { it.size }!! - groups.minByOrNull { it.size }
                "Something very smelly going on (a bug in Alchemist?): there are unknown variables: $difference"
            }
            return knownValues
        }

        private fun setCurrentRandomGenerator(randomGenerator: RandomGenerator) =
            factory.registerSingleton(RandomGenerator::class.java, randomGenerator)

        private val factory: Factory get() = context.factory

        private inline fun <reified T> registerSingleton(target: T) = factory.registerSingleton(T::class.java, target)
        private inline fun <reified T, reified R> registerImplicit(noinline translator: (T) -> R) =
            factory.registerImplicit(T::class.java, R::class.java, translator)
        private fun Map<*, *>.getOrEmpty(key: String) = get(key) ?: emptyList<Any>()
        private fun Map<*, *>.getOrEmptyMap(key: String) = get(key) ?: emptyMap<String, Any>()
    }
}
