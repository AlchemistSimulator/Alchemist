/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader

import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.boundary.loader.LoadingSystemLogger.logger
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot
import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.linkingrules.CombinedLinkingRule
import it.unibo.alchemist.model.linkingrules.NoLinks
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.jirf.Factory
import java.util.concurrent.Semaphore
import java.util.function.Predicate

internal abstract class LoadingSystem(
    private val originalContext: Context,
    private val originalRoot: Map<String, *>,
) : Loader {

    override fun <T, P : Position<P>> getWith(values: Map<String, *>): Simulation<T, P> =
        SingleUseLoader(originalContext, originalRoot).simulationWith(values)

    private inner class SingleUseLoader(originalContext: Context, private val originalRoot: Map<String, *>) {

        private val context: Context = originalContext.child()
        private val mutex = Semaphore(1)
        private var consumed = false

        fun <T : Any?, P : Position<P>> simulationWith(values: Map<String, *>): Simulation<T, P> {
            try {
                mutex.acquireUninterruptibly()
                check(!consumed) {
                    "This loader has already been consumed! This is a bug in Alchemist"
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
            contextualize(incarnation)
            registerImplicit<String, Molecule>(incarnation::createMolecule)
            registerImplicit<String, Any?>(incarnation::createConcentration)
            // ENVIRONMENT
            val environment: Environment<T, P> =
                SimulationModel.visitEnvironment(incarnation, context, root[DocumentRoot.environment])
            logger.info("Created environment: {}", environment)
            contextualize(environment)
            // GLOBAL PROGRAMS
            loadGlobalProgramsOnEnvironment(simulationRNG, incarnation, environment, root)
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
            contextualize(linkingRule)
            // MONITORS
            val monitors = SimulationModel.visitOutputMonitors<P, T>(context, root[DocumentRoot.monitors])
            // DISPLACEMENTS
            setCurrentRandomGenerator(scenarioRNG)
            val displacementsSource = root.getOrEmpty(DocumentRoot.deployments)
            val deploymentDescriptors: List<Deployment<P>> =
                SimulationModel.visitRecursively(
                    context,
                    displacementsSource,
                    syntax = DocumentRoot.Deployment,
                ) { element ->
                    (element as? Map<*, *>)?.let { _ ->
                        setCurrentRandomGenerator(scenarioRNG)
                        SimulationModel.visitBuilding<Deployment<P>>(context, element)?.onSuccess { deployment ->
                            setCurrentRandomGenerator(simulationRNG)
                            populateDeployment(simulationRNG, incarnation, environment, deployment, element)
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
            val exporters = SimulationModel.visitRecursively<Exporter<T, P>>(
                context,
                root.getOrEmpty(DocumentRoot.export),
            ) {
                SimulationModel.visitSingleExporter(incarnation, context, it)
            }
            exporters.forEach { it.bindVariables(variableValues) }
            // ENGINE
            val engineDescriptor = root[DocumentRoot.engine]
            val engine: Simulation<T, P> = SimulationModel.visitBuilding<Simulation<T, P>>(context, engineDescriptor)
                ?.getOrThrow()
                ?: Engine(environment)
            // Attach monitors
            monitors.forEach(engine::addOutputMonitor)
            // Attach data exporters
            if (exporters.isNotEmpty()) {
                engine.addOutputMonitor(GlobalExporter(exporters))
            }
            return engine
        }

        private fun <T, P : Position<P>> loadGlobalProgramsOnEnvironment(
            randomGenerator: RandomGenerator,
            incarnation: Incarnation<T, P>,
            environment: Environment<T, P>,
            descriptor: Map<*, *>,
        ) {
            val environmentDescriptor = descriptor[DocumentRoot.environment]
            if (environmentDescriptor is Map<*, *>) {
                val programDescriptor = environmentDescriptor.getOrEmpty(DocumentRoot.Environment.globalPrograms)
                val globalPrograms = SimulationModel.visitRecursively(
                    context,
                    programDescriptor,
                    DocumentRoot.Environment.GlobalProgram,
                ) { program ->
                    requireNotNull(program) {
                        "null is not a valid program in $descriptor. ${DocumentRoot.Environment.GlobalProgram.guide}"
                    }
                    (program as? Map<*, *>)?.let {
                        SimulationModel.visitProgram(randomGenerator, incarnation, environment, null, context, it)
                            ?.onSuccess { (_, actionable) ->
                                if (actionable is GlobalReaction) {
                                    environment.addGlobalReaction(actionable)
                                }
                            }
                    }
                }
                logger.debug("Global programs: {}", globalPrograms)
            }
        }

        private fun <T, P : Position<P>> loadContentsOnNode(
            incarnation: Incarnation<T, P>,
            node: Node<T>,
            nodePosition: P,
            descriptor: Map<*, *>,
        ) {
            SimulationModel.visitContents(incarnation, context, descriptor)
                .forEach { (filters, molecule, concentrationMaker) ->
                    if (filters.isEmpty() || filters.any { nodePosition in it }) {
                        val concentration = concentrationMaker()
                        logger.debug("Injecting {} ==> {} in node {}", molecule, concentration, node.id)
                        node.setConcentration(molecule, concentration)
                    }
                }
        }

        private fun <T, P : Position<P>> loadPropertiesOnNode(
            node: Node<T>,
            nodePosition: P,
            descriptor: Map<*, *>,
        ) {
            SimulationModel.visitProperty<T, P>(context, descriptor)
                .filter { (filters, _) -> filters.isEmpty() || filters.any { nodePosition in it } }
                .forEach { (_, property) -> node.addProperty(property) }
        }

        private fun <T, P : Position<P>> loadProgramsOnNode(
            randomGenerator: RandomGenerator,
            incarnation: Incarnation<T, P>,
            environment: Environment<T, P>,
            node: Node<T>,
            nodePosition: P,
            descriptor: Map<*, *>,
        ) {
            val programDescriptor = descriptor.getOrEmpty(DocumentRoot.Deployment.programs)
            val programs = SimulationModel.visitRecursively(
                context,
                programDescriptor,
                DocumentRoot.Deployment.Program,
            ) { program ->
                requireNotNull(program) {
                    "null is not a valid program in $descriptor. ${DocumentRoot.Deployment.Program.guide}"
                }
                (program as? Map<*, *>)?.let {
                    SimulationModel.visitProgram(randomGenerator, incarnation, environment, node, context, it)
                        ?.onSuccess { (filters, actionable) ->
                            if (
                                actionable is Reaction &&
                                (filters.isEmpty() || filters.any { shape -> nodePosition in shape })
                            ) {
                                node.addReaction(actionable)
                            }
                        }
                }
            }
            logger.debug("Programs: {}", programs)
        }

        private fun <T, P : Position<P>> populateDeployment(
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
                contextualize<LinkingRule<T, P>>(composedLinkingRule)
            }
            deployment.stream().forEach { position ->
                val node = SimulationModel.visitNode(simulationRNG, incarnation, environment, context, nodeDescriptor)
                contextualize(node)
                // PROPERTIES
                loadPropertiesOnNode(node, position, descriptor)
                node.properties.forEach { contextualize(it) }
                // NODE CONTENTS
                loadContentsOnNode(incarnation, node, position, descriptor)
                // PROGRAMS
                loadProgramsOnNode(simulationRNG, incarnation, environment, node, position, descriptor)
                node.properties.forEach { decontextualize(it) }
                environment.addNode(node, position)
                logger.debug("Added node {} at {}", node.id, position)
                decontextualize(node)
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
            check(knownValues.size == constants.size + dependentVariables.size + variables.size) {
                val originalKeys = constants.keys + dependentVariables.keys + variables.keys
                val groups = listOf(knownValues.keys, originalKeys)
                val difference = groups.maxByOrNull { it.size }.orEmpty() - groups.minByOrNull { it.size }.orEmpty()
                "Something very smelly going on (a bug in Alchemist?): there are unknown variables: $difference"
            }
            return knownValues
        }

        private fun setCurrentRandomGenerator(randomGenerator: RandomGenerator) =
            factory.registerSingleton(RandomGenerator::class.java, randomGenerator)

        private val factory: Factory get() = context.factory

        /*
         * Use this method to register a singleton that should be suitable for any superclass in its hierarchy.
         */
        private inline fun <reified T> contextualize(target: T) = factory.registerSingleton(T::class.java, target)

        /*
         * Contextualize dual operation.
         */
        private inline fun <reified T> decontextualize(target: T) = factory.deregisterSingleton(target)
        private inline fun <reified T, reified R> registerImplicit(noinline translator: (T) -> R) =
            factory.registerImplicit(T::class.java, R::class.java, translator)

        private fun Map<*, *>.getOrEmpty(key: String) = get(key) ?: emptyList<Any>()
        private fun Map<*, *>.getOrEmptyMap(key: String) = get(key) ?: emptyMap<String, Any>()
    }
}
