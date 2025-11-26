package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Variance

/**
 * Extracts type information from KSP symbols for code generation.
 */
object TypeExtractor {
    /**
     * Extracts type parameter names and bounds from a class declaration.
     *
     * @param classDecl The class declaration to extract type parameters from
     * @return A pair of (type parameter names, type parameter bounds)
     */
    fun extractTypeParameters(classDecl: KSClassDeclaration): Pair<List<String>, List<String>> {
        val typeParameters = classDecl.typeParameters
        val typeParamNames = typeParameters.map { it.name.asString() }
        val typeParamBounds = typeParameters.map { typeParam ->
            val bounds: List<String> = typeParam.bounds.map { bound ->
                BoundProcessor.processBound(bound, typeParamNames)
            }.toList()
            if (bounds.isNotEmpty()) {
                val boundStr = bounds.joinToString(" & ")
                "${typeParam.name.asString()}: $boundStr"
            } else {
                typeParam.name.asString()
            }
        }
        return typeParamNames to typeParamBounds
    }

    /**
     * Extracts a string representation of a type reference.
     *
     * @param typeRef The type reference to extract
     * @param typeParamNames List of existing type parameter names for substitution
     * @return A string representation of the type
     */
    // Normalize a KSTypeReference into a printable string, respecting existing type parameter names.
    fun extractTypeString(typeRef: KSTypeReference, typeParamNames: List<String> = emptyList()): String {
        val resolved = typeRef.resolve()
        val declaration = resolved.declaration
        if (declaration is KSTypeParameter) {
            val paramName = declaration.name.asString()
            if (typeParamNames.contains(paramName)) {
                return paramName
            }
        }
        val typeName = getTypeName(declaration)
        val arguments = resolved.arguments
        val typeString = if (arguments.isNotEmpty()) {
            val typeArgs = arguments.joinToString(", ") { arg ->
                formatTypeArgument(arg, typeParamNames)
            }
            "$typeName<$typeArgs>"
        } else {
            typeName
        }
        return if (resolved.isMarkedNullable) {
            "$typeString?"
        } else {
            typeString
        }
    }

    private fun getTypeName(declaration: KSDeclaration): String {
        val qualifiedName = declaration.qualifiedName?.asString()
        return if (qualifiedName != null && qualifiedName.isNotEmpty()) {
            qualifiedName
        } else {
            declaration.simpleName.asString()
        }
    }

    // Represent the variance and nested references for each argument.
    private fun formatTypeArgument(arg: KSTypeArgument, typeParamNames: List<String>): String = when {
        arg.type == null -> "*"
        arg.variance == Variance.STAR -> "*"
        arg.variance == Variance.CONTRAVARIANT ->
            arg.type?.let { "in ${extractTypeString(it, typeParamNames)}" } ?: "*"
        arg.variance == Variance.COVARIANT ->
            arg.type?.let { "out ${extractTypeString(it, typeParamNames)}" } ?: "*"
        else -> arg.type?.let { extractTypeString(it, typeParamNames) } ?: "*"
    }

    /**
     * Extracts type strings for a list of value parameters.
     *
     * @param remainingParams The parameters to extract types from
     * @param typeParamNames List of existing type parameter names for substitution
     * @return A list of type strings
     */
    fun extractParamTypes(
        remainingParams: List<KSValueParameter>,
        typeParamNames: List<String> = emptyList(),
    ): List<String> = remainingParams.map { param ->
        if (param.isVararg) {
            val resolved = param.type.resolve()
            val declaration = resolved.declaration
            val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
            if (qualifiedName == "kotlin.Array" || qualifiedName == "Array") {
                resolved.arguments.firstOrNull()?.type?.let { elementType ->
                    extractTypeString(elementType, typeParamNames)
                } ?: extractTypeString(param.type, typeParamNames)
            } else {
                extractTypeString(param.type, typeParamNames)
            }
        } else {
            extractTypeString(param.type, typeParamNames)
        }
    }

    /**
     * Extracts parameter names from a list of value parameters.
     *
     * @param remainingParams The parameters to extract names from
     * @return A list of parameter names
     */
    fun extractParamNames(remainingParams: List<KSValueParameter>): List<String> = remainingParams.map { param ->
        param.name?.asString() ?: "param${remainingParams.indexOf(param)}"
    }
}
