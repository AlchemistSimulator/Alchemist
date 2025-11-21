package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * Processes type arguments for context parameters in DSL builder functions.
 */
object TypeArgumentProcessor {
    /**
     * Builds the type string for a context parameter, handling type arguments and bounds.
     *
     * @param typeRef The type reference to build from
     * @param typeParamNames Mutable list of type parameter names (may be modified)
     * @param typeParamBounds Mutable list of type parameter bounds (may be modified)
     * @return The type string for the context parameter
     */
    fun buildContextParamType(
        typeRef: KSTypeReference,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ): String {
        val resolved = typeRef.resolve()
        val declaration = resolved.declaration
        val typeName = getTypeName(declaration)
        val arguments = resolved.arguments

        if (arguments.isEmpty()) {
            return typeName
        }

        val declarationTypeParams = getDeclarationTypeParams(declaration)
        val standardTypeParams = listOf("T", "U", "V", "W", "X", "Y", "Z")
        val indexState = IndexState(0, 0)

        val typeArgs = arguments.joinToString(", ") { arg ->
            processTypeArgument(
                arg,
                declarationTypeParams,
                standardTypeParams,
                typeParamNames,
                typeParamBounds,
                indexState,
            )
        }

        return "$typeName<$typeArgs>"
    }

    private data class IndexState(var nextStandardIndex: Int, var nextDeclParamIndex: Int)

    private fun getTypeName(declaration: com.google.devtools.ksp.symbol.KSDeclaration): String {
        val qualifiedName = declaration.qualifiedName?.asString()
        return if (qualifiedName != null && qualifiedName.isNotEmpty()) {
            qualifiedName
        } else {
            declaration.simpleName.asString()
        }
    }

    private fun getDeclarationTypeParams(
        declaration: com.google.devtools.ksp.symbol.KSDeclaration,
    ): List<com.google.devtools.ksp.symbol.KSTypeParameter> = when (declaration) {
        is com.google.devtools.ksp.symbol.KSClassDeclaration -> declaration.typeParameters
        is com.google.devtools.ksp.symbol.KSTypeAlias -> declaration.typeParameters
        else -> emptyList()
    }

    private fun processTypeArgument(
        arg: com.google.devtools.ksp.symbol.KSTypeArgument,
        declarationTypeParams: List<com.google.devtools.ksp.symbol.KSTypeParameter>,
        standardTypeParams: List<String>,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        indexState: IndexState,
    ): String = when {
        arg.type == null || arg.variance == com.google.devtools.ksp.symbol.Variance.STAR -> {
            val result = handleNullOrStarTypeArg(
                declarationTypeParams,
                standardTypeParams,
                typeParamNames,
                typeParamBounds,
                indexState.nextDeclParamIndex,
                indexState.nextStandardIndex,
            )
            indexState.nextDeclParamIndex = result.second
            indexState.nextStandardIndex = result.third
            result.first
        }
        else -> processConcreteTypeArgument(arg, typeParamNames, typeParamBounds, standardTypeParams, indexState)
    }

    private fun handleNullOrStarTypeArg(
        declarationTypeParams: List<com.google.devtools.ksp.symbol.KSTypeParameter>,
        standardTypeParams: List<String>,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        nextDeclParamIndex: Int,
        nextStandardIndex: Int,
    ): Triple<String, Int, Int> {
        val (paramName, declParam) = if (nextDeclParamIndex < declarationTypeParams.size) {
            val declParam = declarationTypeParams[nextDeclParamIndex]
            declParam.name.asString() to declParam
        } else if (nextStandardIndex < standardTypeParams.size) {
            standardTypeParams[nextStandardIndex] to null
        } else {
            "T" to null
        }

        val newNextDeclParamIndex = if (nextDeclParamIndex < declarationTypeParams.size) {
            nextDeclParamIndex + 1
        } else {
            nextDeclParamIndex
        }

        val newNextStandardIndex = if (nextDeclParamIndex >= declarationTypeParams.size &&
            nextStandardIndex < standardTypeParams.size
        ) {
            nextStandardIndex + 1
        } else {
            nextStandardIndex
        }

        if (!typeParamNames.contains(paramName)) {
            typeParamNames.add(paramName)
            val boundStr = if (declParam != null) {
                val bounds = declParam.bounds.map { bound ->
                    BoundProcessor.processBound(bound)
                }.toList()
                if (bounds.isNotEmpty()) {
                    "$paramName: ${bounds.joinToString(" & ")}"
                } else {
                    paramName
                }
            } else {
                paramName
            }
            typeParamBounds.add(boundStr)
        }

        return Triple(paramName, newNextDeclParamIndex, newNextStandardIndex)
    }

    private fun processConcreteTypeArgument(
        arg: com.google.devtools.ksp.symbol.KSTypeArgument,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        standardTypeParams: List<String>,
        indexState: IndexState,
    ): String {
        val argType = arg.type ?: return "T"
        val argDecl = argType.resolve().declaration
        return if (argDecl is com.google.devtools.ksp.symbol.KSTypeParameter) {
            addTypeParameterIfNeeded(argDecl, typeParamNames, typeParamBounds)
            argDecl.name.asString()
        } else {
            processExtractedType(argType, typeParamNames, standardTypeParams, indexState, typeParamBounds)
        }
    }

    private fun addTypeParameterIfNeeded(
        argDecl: com.google.devtools.ksp.symbol.KSTypeParameter,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ) {
        val paramName = argDecl.name.asString()
        if (!typeParamNames.contains(paramName)) {
            typeParamNames.add(paramName)
            val bounds = argDecl.bounds.map { BoundProcessor.processBound(it) }.toList()
            val boundStr = if (bounds.isNotEmpty()) {
                "$paramName: ${bounds.joinToString(" & ")}"
            } else {
                paramName
            }
            typeParamBounds.add(boundStr)
        }
    }

    private fun processExtractedType(
        typeRef: KSTypeReference,
        typeParamNames: MutableList<String>,
        standardTypeParams: List<String>,
        indexState: IndexState,
        typeParamBounds: MutableList<String>,
    ): String {
        val extracted = TypeExtractor.extractTypeString(typeRef, typeParamNames)
        return if (extracted.contains("*") || !typeParamNames.any { it in extracted }) {
            val standardName = if (indexState.nextStandardIndex < standardTypeParams.size) {
                standardTypeParams[indexState.nextStandardIndex++]
            } else {
                "T"
            }
            if (!typeParamNames.contains(standardName)) {
                typeParamNames.add(standardName)
                typeParamBounds.add(standardName)
            }
            standardName
        } else {
            extracted
        }
    }
}
