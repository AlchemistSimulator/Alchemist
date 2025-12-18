package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

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
        // These are fallback names for wildcard/null arguments so we always generate valid type parameters.
        val standardTypeParams = listOf("T", "U", "V", "W")
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

    private fun getTypeName(declaration: KSDeclaration): String {
        val qualifiedName = declaration.qualifiedName?.asString()
        return if (!qualifiedName.isNullOrEmpty()) {
            qualifiedName
        } else {
            declaration.simpleName.asString()
        }
    }

    private fun getDeclarationTypeParams(declaration: KSDeclaration): List<KSTypeParameter> = when (declaration) {
        is KSClassDeclaration -> declaration.typeParameters
        is KSTypeAlias -> declaration.typeParameters
        else -> emptyList()
    }

    private fun processTypeArgument(
        arg: KSTypeArgument,
        declarationTypeParams: List<KSTypeParameter>,
        standardTypeParams: List<String>,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        indexState: IndexState,
    ): String = when {
        arg.type == null || arg.variance == Variance.STAR -> {
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
        declarationTypeParams: List<KSTypeParameter>,
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
                    TypeBoundProcessor.processBound(bound)
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
        arg: KSTypeArgument,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
        standardTypeParams: List<String>,
        indexState: IndexState,
    ): String {
        val argType = arg.type ?: return "T"
        val argDecl = argType.resolve().declaration
        return if (argDecl is KSTypeParameter) {
            addTypeParameterIfNeeded(argDecl, typeParamNames, typeParamBounds)
            argDecl.name.asString()
        } else {
            processExtractedType(argType, typeParamNames, standardTypeParams, indexState, typeParamBounds)
        }
    }

    private fun addTypeParameterIfNeeded(
        argDecl: KSTypeParameter,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ) {
        val paramName = argDecl.name.asString()
        if (!typeParamNames.contains(paramName)) {
            typeParamNames.add(paramName)
            val bounds = argDecl.bounds.map { TypeBoundProcessor.processBound(it) }.toList()
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
