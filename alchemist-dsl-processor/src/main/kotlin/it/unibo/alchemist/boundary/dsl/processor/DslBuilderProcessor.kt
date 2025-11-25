package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import it.unibo.alchemist.boundary.dsl.BuildDsl
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/**
 * KSP symbol processor that generates DSL builder functions for classes annotated with [BuildDsl].
 */
class DslBuilderProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("DslBuilderProcessor: Starting processing")
        logger.info("DslBuilderProcessor: BuildDsl qualified name: ${BuildDsl::class.qualifiedName}")

        val annotationName = BuildDsl::class.qualifiedName ?: return emptyList()
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        val symbolList = symbols.toList()
        logger.info("DslBuilderProcessor: Found ${symbolList.size} symbols with @BuildDsl annotation")

        symbolList.forEach { symbol ->
            val qualifiedName = when (symbol) {
                is KSClassDeclaration -> symbol.qualifiedName?.asString() ?: "unknown"
                else -> symbol.toString()
            }
            logger.info("DslBuilderProcessor: Found symbol: $qualifiedName")
        }

        val ret = symbolList.filter { !it.validate() }.toList()

        symbolList
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { classDecl ->
                processClass(classDecl as KSClassDeclaration)
            }

        return ret
    }

    private fun shouldInjectType(injectionType: InjectionType, annotationValues: Map<String, Any?>): Boolean =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> annotationValues["injectEnvironment"] as? Boolean ?: true
            InjectionType.GENERATOR -> annotationValues["injectGenerator"] as? Boolean ?: true
            InjectionType.INCARNATION -> annotationValues["injectIncarnation"] as? Boolean ?: true
            InjectionType.NODE -> annotationValues["injectNode"] as? Boolean ?: true
            InjectionType.REACTION -> annotationValues["injectReaction"] as? Boolean ?: true
            InjectionType.TIMEDISTRIBUTION -> true
            InjectionType.FILTER -> true
        }

    private fun processClass(classDecl: KSClassDeclaration) {
        logger.info("DslBuilderProcessor: Processing class ${classDecl.simpleName.asString()}")
        logger.info("DslBuilderProcessor: Class qualified name: ${classDecl.qualifiedName?.asString()}")

        val annotation = classDecl.annotations.firstOrNull {
            it.shortName.asString() == "BuildDsl"
        }
        if (annotation == null) {
            logger.warn("Class ${classDecl.simpleName.asString()} has no @BuildDsl annotation")
            return
        }

        val annotationValues = annotation.arguments
            .mapNotNull { arg -> arg.name?.asString()?.let { it to arg.value } }
            .toMap()

        logger.info("DslBuilderProcessor: Annotation values: $annotationValues")
        val manualScope = annotationValues["scope"] as? String
        logger.info("DslBuilderProcessor: Manual scope from annotation: '$manualScope'")

        val functionName = (annotationValues["functionName"] as? String)?.takeIf { it.isNotEmpty() }
            ?: classDecl.simpleName.asString().replaceFirstChar { it.lowercaseChar() }

        val constructor = ConstructorFinder.findConstructor(classDecl)
        if (constructor == null) {
            logger.warn("Class ${classDecl.simpleName.asString()} has no usable constructor")
            return
        }

        val parameters = constructor.parameters
        logger.info("DslBuilderProcessor: Found constructor with ${parameters.size} parameters")
        parameters.forEachIndexed { index, param ->
            val typeName = param.type.resolve().declaration.qualifiedName?.asString() ?: "unknown"
            logger.info("DslBuilderProcessor: Parameter $index: ${param.name?.asString()} : $typeName")
        }

        val injectionIndices = ParameterInjector.findInjectionIndices(parameters)
        logger.info("DslBuilderProcessor: Injection indices: $injectionIndices")
        val contextType = ParameterInjector.determineContextType(injectionIndices, annotationValues)
        logger.info("DslBuilderProcessor: Determined context type: $contextType (manual scope was: '$manualScope')")
        val paramsToSkip = ParameterInjector.getInjectionParams(injectionIndices, annotationValues, contextType)
        val remainingParams = parameters.filterIndexed { index, _ -> !paramsToSkip.contains(index) }

        val containingFile = classDecl.containingFile
        val dependencies = if (containingFile != null) {
            Dependencies(true, containingFile)
        } else {
            Dependencies.ALL_FILES
        }

        val fileName = functionName.replaceFirstChar { it.uppercaseChar() } + "Helper"

        val file = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = ProcessorConfig.GENERATED_PACKAGE,
            fileName = fileName,
        )

        PrintWriter(file, true, StandardCharsets.UTF_8).use { writer ->
            writeGeneratedCode(
                writer,
                classDecl,
                functionName,
                remainingParams,
                parameters,
                paramsToSkip,
                injectionIndices,
                annotationValues,
                contextType,
            )
        }
    }

    private fun writeGeneratedCode(
        writer: PrintWriter,
        classDecl: KSClassDeclaration,
        functionName: String,
        remainingParams: List<KSValueParameter>,
        allParameters: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        contextType: ContextType,
    ) {
        writeFileHeader(writer, classDecl, contextType)

        val (initialTypeParamNames, initialTypeParamBounds) = TypeExtractor.extractTypeParameters(classDecl)
        val typeParamNames = initialTypeParamNames.toMutableList()
        val typeParamBounds = initialTypeParamBounds.toMutableList()

        val paramTypes = TypeExtractor.extractParamTypes(remainingParams, typeParamNames)
        val defaultValues = DefaultValueAnalyzer.extractAllDefaultValues(remainingParams, classDecl)
        val needsMapEnvironment = checkNeedsMapEnvironment(injectionIndices, allParameters)
        val paramNames = TypeExtractor.extractParamNames(remainingParams)

        val (injectedParams, injectedParamNames, injectedParamTypesMap) = processInjectedParams(
            injectionIndices,
            annotationValues,
            allParameters,
            typeParamNames,
            typeParamBounds,
        )

        val hasInjectedParams = injectedParams.isNotEmpty()
        updateTypeParamsForInjected(
            hasInjectedParams,
            injectionIndices,
            annotationValues,
            allParameters,
            typeParamNames,
            typeParamBounds,
            initialTypeParamNames,
            initialTypeParamBounds,
        )

        addPositionImportIfNeeded(hasInjectedParams, typeParamNames, typeParamBounds, initialTypeParamBounds)
        val constructorParams = writeImportsAndFunction(
            writer,
            typeParamBounds,
            paramTypes,
            defaultValues,
            classDecl,
            needsMapEnvironment,
            injectedParamTypesMap,
            allParameters,
            remainingParams,
            paramsToSkip,
            paramNames,
            injectionIndices,
            injectedParamNames,
            annotationValues,
            typeParamNames,
            contextType,
            hasInjectedParams,
            functionName,
            classDecl.simpleName.asString(),
            injectedParams,
            initialTypeParamNames,
            initialTypeParamBounds,
        )

        val hasNode = injectionIndices.containsKey(InjectionType.NODE) &&
            annotationValues["injectNode"] as? Boolean ?: true
        val hasReaction = injectionIndices.containsKey(InjectionType.REACTION) &&
            annotationValues["injectReaction"] as? Boolean ?: true

        if (hasNode && !hasReaction) {
            writePropertyContextFunction(
                writer,
                typeParamBounds,
                paramTypes,
                defaultValues,
                classDecl,
                needsMapEnvironment,
                injectedParamTypesMap,
                allParameters,
                remainingParams,
                paramsToSkip,
                paramNames,
                injectionIndices,
                injectedParamNames,
                annotationValues,
                typeParamNames,
                functionName,
                classDecl.simpleName.asString(),
                injectedParams,
                initialTypeParamNames,
                initialTypeParamBounds,
                constructorParams,
            )
        }
    }

    private fun writeFileHeader(writer: PrintWriter, classDecl: KSClassDeclaration, contextType: ContextType) {
        writer.println("@file:Suppress(\"UNCHECKED_CAST\", \"DEPRECATION\")")
        writer.println("package ${ProcessorConfig.GENERATED_PACKAGE}")
        writer.println()
        writer.println("import ${classDecl.qualifiedName?.asString()}")

        when (contextType) {
            ContextType.SIMULATION -> writer.println("import ${ProcessorConfig.ContextTypes.SIMULATION_CONTEXT}")
            ContextType.EXPORTER_CONTEXT -> writer.println("import ${ProcessorConfig.ContextTypes.EXPORTER_CONTEXT}")
            ContextType.GLOBAL_PROGRAMS_CONTEXT -> writer.println(
                "import ${ProcessorConfig.ContextTypes.GLOBAL_PROGRAMS_CONTEXT}",
            )
            ContextType.OUTPUT_MONITORS_CONTEXT -> writer.println(
                "import ${ProcessorConfig.ContextTypes.OUTPUT_MONITORS_CONTEXT}",
            )
            ContextType.TERMINATORS_CONTEXT -> writer.println(
                "import ${ProcessorConfig.ContextTypes.TERMINATORS_CONTEXT}",
            )
            ContextType.DEPLOYMENT -> writer.println("import ${ProcessorConfig.ContextTypes.DEPLOYMENTS_CONTEXT}")
            ContextType.DEPLOYMENT_CONTEXT -> writer.println(
                "import ${ProcessorConfig.ContextTypes.DEPLOYMENT_CONTEXT}",
            )
            ContextType.PROGRAM -> {
                writer.println("import ${ProcessorConfig.ContextTypes.PROGRAM_CONTEXT}")
                writer.println("import ${ProcessorConfig.ContextTypes.PROPERTIES_CONTEXT}")
            }
            ContextType.PROPERTY -> {
                writer.println("import ${ProcessorConfig.ContextTypes.PROPERTY_CONTEXT}")
            }
        }
    }

    private fun checkNeedsMapEnvironment(
        injectionIndices: Map<InjectionType, Int>,
        allParameters: List<KSValueParameter>,
    ): Boolean {
        val envParam = getEnvironmentParameter(injectionIndices, allParameters) ?: return false
        val qualifiedName = getQualifiedName(envParam)
        return qualifiedName.contains("MapEnvironment")
    }

    private fun getEnvironmentParameter(
        injectionIndices: Map<InjectionType, Int>,
        allParameters: List<KSValueParameter>,
    ): KSValueParameter? {
        val injectionIndicesForEnv = injectionIndices[InjectionType.ENVIRONMENT] ?: return null
        return allParameters.getOrNull(injectionIndicesForEnv)
    }

    private fun getQualifiedName(param: KSValueParameter): String {
        val resolved = param.type.resolve()
        return resolved.declaration.qualifiedName?.asString().orEmpty()
    }

    private fun processInjectedParams(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        allParameters: List<KSValueParameter>,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ): Triple<List<Pair<String, String>>, Map<InjectionType, String>, Map<InjectionType, String>> {
        val injectedParams = mutableListOf<Pair<String, String>>()
        val injectedParamNames = mutableMapOf<InjectionType, String>()
        val injectedParamTypesMap = mutableMapOf<InjectionType, String>()

        injectionIndices.forEach { (injectionType, index) ->
            if (shouldInjectType(injectionType, annotationValues)) {
                val param = allParameters[index]
                val paramType = FunctionGenerator.buildContextParamType(param.type, typeParamNames, typeParamBounds)
                val paramName = getInjectionParamName(injectionType)
                injectedParams.add(paramName to paramType)
                injectedParamNames[injectionType] = paramName
                injectedParamTypesMap[injectionType] = paramType
            }
        }

        return Triple(injectedParams, injectedParamNames, injectedParamTypesMap)
    }

    private fun getInjectionParamName(injectionType: InjectionType): String = when (injectionType) {
        InjectionType.ENVIRONMENT -> "env"
        InjectionType.GENERATOR -> "generator"
        InjectionType.INCARNATION -> "incarnation"
        InjectionType.NODE -> "node"
        InjectionType.REACTION -> "reaction"
        InjectionType.TIMEDISTRIBUTION -> "td"
        InjectionType.FILTER -> "filter"
    }

    private fun updateTypeParamsForInjected(
        hasInjectedParams: Boolean,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        allParameters: List<KSValueParameter>,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        initialTypeParamNames: List<String>,
        initialTypeParamBounds: List<String>,
    ) {
        if (!hasInjectedParams) {
            return
        }

        val newTypeParams = mutableMapOf<String, String>()

        injectionIndices.forEach { (injectionType, index) ->
            if (shouldInjectType(injectionType, annotationValues)) {
                val param = allParameters[index]
                val initialSize = typeParamNames.size
                FunctionGenerator.buildContextParamType(param.type, typeParamNames, typeParamBounds)

                for (i in initialSize until typeParamNames.size) {
                    val newParam = typeParamNames[i]
                    val newBound = typeParamBounds[i]
                    if (!newTypeParams.containsKey(newParam)) {
                        newTypeParams[newParam] = newBound
                    }
                }
            }
        }

        val originalTypeParams = initialTypeParamNames.toSet()
        val allNewParams = typeParamNames.filter { it !in originalTypeParams }

        typeParamNames.clear()
        typeParamBounds.clear()

        typeParamNames.addAll(initialTypeParamNames)
        typeParamBounds.addAll(initialTypeParamBounds)

        allNewParams.forEach { param ->
            if (!typeParamNames.contains(param)) {
                typeParamNames.add(param)
                typeParamBounds.add(newTypeParams[param] ?: param)
            }
        }
    }

    private fun addPositionImportIfNeeded(
        hasInjectedParams: Boolean,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        classTypeParamBounds: List<String>,
    ) {
        val needsPositionImport = hasInjectedParams && !typeParamBounds.any { it.contains("Position") }
        if (needsPositionImport && !typeParamNames.contains("P")) {
            val classPIndex = classTypeParamBounds.indexOfFirst { it.contains("Position") }
            val pBound = if (classPIndex >= 0) {
                val classBound = classTypeParamBounds[classPIndex]
                if (classBound.contains(":")) {
                    val boundPart = classBound.substringAfter(":")
                    "P:$boundPart"
                } else {
                    "P: ${ProcessorConfig.POSITION_TYPE}<P>"
                }
            } else {
                "P: it.unibo.alchemist.model.Position<P>"
            }
            val tIndex = typeParamNames.indexOf("T")
            if (tIndex >= 0) {
                typeParamNames.add(tIndex + 1, "P")
                typeParamBounds.add(tIndex + 1, pBound)
            } else {
                if (!typeParamNames.contains("T")) {
                    typeParamNames.add("T")
                    typeParamBounds.add("T")
                }
                typeParamNames.add("P")
                typeParamBounds.add(pBound)
            }
        }
    }

    private fun writeImportsAndFunction(
        writer: PrintWriter,
        finalTypeParamBounds: List<String>,
        paramTypes: List<String>,
        defaultValues: List<String>,
        classDecl: KSClassDeclaration,
        needsMapEnvironment: Boolean,
        injectedParamTypesMap: Map<InjectionType, String>,
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        contextType: ContextType,
        hasInjectedParams: Boolean,
        functionName: String,
        className: String,
        injectedParams: List<Pair<String, String>>,
        initialTypeParamNames: List<String>,
        initialTypeParamBounds: List<String>,
    ): List<String> {
        ImportManager.writeImports(
            writer,
            finalTypeParamBounds,
            paramTypes,
            defaultValues,
            classDecl,
            needsMapEnvironment,
            injectedParamTypesMap.values.toList(),
        )

        val constructorParams = FunctionGenerator.buildConstructorParams(
            allParameters,
            remainingParams,
            paramsToSkip,
            paramNames,
            injectionIndices,
            injectedParamNames,
            annotationValues,
            typeParamNames,
            contextType,
            hasInjectedParams,
            "ctx",
            injectedParamTypesMap,
        )

        val functionSignature = FunctionGenerator.buildFunctionSignature(
            functionName,
            finalTypeParamBounds,
            typeParamNames,
            className,
            remainingParams,
            paramNames,
            paramTypes,
            injectedParams,
            contextType,
            initialTypeParamNames,
            initialTypeParamBounds,
        )

        writer.println(functionSignature)
        val constructorCall = FunctionGenerator.buildConstructorCall(
            className,
            typeParamNames,
            constructorParams,
            initialTypeParamNames,
        )
        writer.println("    $constructorCall")
        return constructorParams
    }

    private fun writePropertyContextFunction(
        writer: PrintWriter,
        finalTypeParamBounds: List<String>,
        paramTypes: List<String>,
        @Suppress("UNUSED_PARAMETER") defaultValues: List<String>,
        @Suppress("UNUSED_PARAMETER") classDecl: KSClassDeclaration,
        @Suppress("UNUSED_PARAMETER") needsMapEnvironment: Boolean,
        injectedParamTypesMap: Map<InjectionType, String>,
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        functionName: String,
        className: String,
        @Suppress("UNUSED_PARAMETER") injectedParams: List<Pair<String, String>>,
        initialTypeParamNames: List<String>,
        @Suppress("UNUSED_PARAMETER") initialTypeParamBounds: List<String>,
        @Suppress("UNUSED_PARAMETER") constructorParams: List<String>,
    ) {
        writer.println()

        val (tParam, pParam) = TypeParameterHandler.findTAndPParams(typeParamNames, finalTypeParamBounds)
        val pVariance = FunctionGenerator.extractVarianceFromBound(pParam, finalTypeParamBounds)
        val pWithVariance = if (pVariance.isNotEmpty()) "$pVariance $pParam" else pParam

        val functionTypeParamString = TypeParameterHandler.buildTypeParamString(finalTypeParamBounds)
        val returnType = TypeParameterHandler.buildReturnType(className, initialTypeParamNames)
        val contextPart = "context(ctx: ${ProcessorConfig.ContextTypes.PROPERTY_CONTEXT}<$tParam, $pWithVariance>) "
        val functionParams = FunctionGenerator.buildFunctionParams(remainingParams, paramNames, paramTypes)

        val functionSignature = "${contextPart}fun$functionTypeParamString $functionName$functionParams: $returnType ="
        writer.println(functionSignature)

        val propertyContextConstructorParams = ConstructorParamBuilder.convertToPropertyContextAccessors(
            injectionIndices,
            allParameters,
            remainingParams,
            paramsToSkip,
            paramNames,
            injectedParamNames,
            annotationValues,
            typeParamNames,
            injectedParamTypesMap,
        )
        val constructorCall = FunctionGenerator.buildConstructorCall(
            className,
            typeParamNames,
            propertyContextConstructorParams,
            initialTypeParamNames,
        )
        writer.println("    $constructorCall")
    }
}
