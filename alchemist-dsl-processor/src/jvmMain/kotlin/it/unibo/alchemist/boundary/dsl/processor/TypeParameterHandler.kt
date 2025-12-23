package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

/**
 * Handles type parameter preparation and manipulation for DSL builder functions.
 */
internal object TypeParameterHandler {
    /**
     * Prepares type parameters by adding T and P parameters if needed for injected parameters.
     *
     * @param typeParamNames Initial type parameter names
     * @param typeParamBounds Initial type parameter bounds
     * @param injectedParams List of injected parameter names and types
     * @param classTypeParamBounds Type parameter bounds from the class declaration
     * @return Pair of final type parameter names and bounds
     */
    fun prepareTypeParams(
        typeParamNames: List<String>,
        typeParamBounds: List<String>,
        injectedParams: List<Pair<String, String>>,
        classTypeParamBounds: List<String>,
    ): Pair<MutableList<String>, MutableList<String>> {
        val finalTypeParamNames = typeParamNames.toMutableList()
        val finalTypeParamBounds = typeParamBounds.toMutableList()
        if (injectedParams.isNotEmpty()) {
            val (tParam, pParam) = findTAndPParams(typeParamNames, typeParamBounds)
            addTParamIfNeeded(tParam, finalTypeParamNames, finalTypeParamBounds)
            addPParamIfNeeded(pParam, finalTypeParamNames, finalTypeParamBounds, classTypeParamBounds)
        } else if (typeParamNames.isEmpty() && classTypeParamBounds.isNotEmpty()) {
            finalTypeParamNames.clear()
            finalTypeParamBounds.clear()
            classTypeParamBounds.forEachIndexed { index, bound ->
                val paramName = if (bound.contains(":")) bound.substringBefore(":") else bound
                finalTypeParamNames.add(paramName.trim())
                finalTypeParamBounds.add(bound)
            }
        }
        return finalTypeParamNames to finalTypeParamBounds
    }

    private fun addTParamIfNeeded(
        tParam: String,
        finalTypeParamNames: MutableList<String>,
        finalTypeParamBounds: MutableList<String>,
    ) {
        val tIndex = finalTypeParamNames.indexOf(tParam)
        if (tIndex >= 0) {
            val existingTBound = finalTypeParamBounds.getOrNull(tIndex)
            if (existingTBound != null && !existingTBound.contains(":")) {
                finalTypeParamBounds[tIndex] = tParam
            }
        } else {
            finalTypeParamNames.add(tParam)
            val tBound = finalTypeParamBounds.find { it.startsWith("$tParam:") } ?: tParam
            finalTypeParamBounds.add(tBound)
        }
    }

    private fun addPParamIfNeeded(
        pParam: String,
        finalTypeParamNames: MutableList<String>,
        finalTypeParamBounds: MutableList<String>,
        classTypeParamBounds: List<String>,
    ) {
        val pIndex = finalTypeParamNames.indexOf(pParam)
        val classPIndex = classTypeParamBounds.indexOfFirst { it.contains("Position") }
        val expectedBound = if (classPIndex >= 0) {
            val classBound = classTypeParamBounds[classPIndex]
            if (classBound.contains(":")) {
                val boundPart = classBound.substringAfter(":")
                "$pParam:$boundPart"
            } else {
                "$pParam: ${ProcessorConfig.POSITION_TYPE}<$pParam>"
            }
        } else {
            "$pParam: it.unibo.alchemist.model.Position<$pParam>"
        }
        if (pIndex >= 0) {
            val existingBound = finalTypeParamBounds.getOrNull(pIndex)
            if (existingBound == null || !existingBound.contains("Position") || existingBound != expectedBound) {
                if (pIndex < finalTypeParamBounds.size) {
                    finalTypeParamBounds[pIndex] = expectedBound
                } else {
                    finalTypeParamBounds.add(expectedBound)
                }
            }
        } else {
            finalTypeParamNames.add(pParam)
            finalTypeParamBounds.add(expectedBound)
        }
    }

    /**
     * Finds or determines the T and P type parameter names from the given lists.
     *
     * @param typeParamNames List of type parameter names
     * @param typeParamBounds List of type parameter bounds
     * @return Pair of (T parameter name, P parameter name)
     */
    fun findTAndPParams(typeParamNames: List<String>, typeParamBounds: List<String>): Pair<String, String> {
        val pIndex = typeParamBounds.indexOfFirst { it.contains("Position") }
        val pParam = if (pIndex >= 0 && pIndex < typeParamNames.size) {
            typeParamNames[pIndex]
        } else {
            "P"
        }
        val tIndex = typeParamNames.indexOf("T")
        val tParam = when {
            tIndex >= 0 -> typeParamNames[tIndex]
            pIndex == 0 && typeParamNames.size > 1 -> typeParamNames[1]
            pIndex > 0 -> typeParamNames[0]
            typeParamNames.isNotEmpty() && pIndex < 0 -> typeParamNames[0]
            else -> "T"
        }
        return tParam to pParam
    }

    /**
     * Builds the type parameter string for function signatures.
     *
     * @param finalTypeParamBounds List of type parameter bounds
     * @return Type parameter string (e.g., "<T, P: Position<P>>") or empty string
     */
    fun buildTypeParamString(finalTypeParamBounds: List<String>): String = if (finalTypeParamBounds.isNotEmpty()) {
        "<${finalTypeParamBounds.joinToString(", ")}>"
    } else {
        ""
    }

    /**
     * Builds the return type string for function signatures.
     *
     * @param className The name of the class
     * @param classTypeParamNames Type parameter names from the class declaration
     * @return Return type string (e.g., "MyClass<T, P>" or "MyClass")
     */
    fun buildReturnType(className: String, classTypeParamNames: List<String>): String =
        if (classTypeParamNames.isNotEmpty()) {
            "$className<${classTypeParamNames.joinToString(", ")}>"
        } else {
            className
        }

    /**
     * Collects type parameters needed for a type reference.
     *
     * @param typeRef The type reference to analyze
     * @param existingTypeParamNames List of existing type parameter names
     * @return Set of needed type parameter names
     */
    fun collectNeededTypeParams(typeRef: KSTypeReference, existingTypeParamNames: List<String>): Set<String> {
        val needed = mutableSetOf<String>()
        val resolved = typeRef.resolve()
        val arguments = resolved.arguments
        arguments.forEach { arg ->
            processTypeArgForCollection(arg, existingTypeParamNames, needed)
        }
        return needed
    }

    private fun processTypeArgForCollection(
        arg: KSTypeArgument,
        existingTypeParamNames: List<String>,
        needed: MutableSet<String>,
    ) {
        when {
            arg.type == null || arg.variance == Variance.STAR -> {
                if (existingTypeParamNames.isEmpty()) {
                    needed.add("T")
                }
            }
            else -> {
                processConcreteTypeArgForCollection(arg, existingTypeParamNames, needed)
            }
        }
    }

    private fun processConcreteTypeArgForCollection(
        arg: KSTypeArgument,
        existingTypeParamNames: List<String>,
        needed: MutableSet<String>,
    ) {
        val argType = arg.type ?: return
        val argDecl = argType.resolve().declaration
        if (argDecl is KSTypeParameter) {
            val paramName = argDecl.name.asString()
            if (!existingTypeParamNames.contains(paramName)) {
                needed.add(paramName)
            }
        } else {
            val extracted = TypeExtractor.extractTypeString(argType, existingTypeParamNames)
            if ((extracted.contains("*") || !existingTypeParamNames.any { it in extracted }) &&
                existingTypeParamNames.isEmpty()
            ) {
                needed.add("T")
            }
        }
    }
}
