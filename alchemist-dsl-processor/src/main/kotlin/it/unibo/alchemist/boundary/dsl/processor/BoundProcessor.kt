package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference

/**
 * Processes type bounds for type parameters, cleaning up internal Kotlin type representations.
 */
object BoundProcessor {
    /**
     * Processes a type bound reference, cleaning up internal Kotlin type representations and variance annotations.
     *
     * @param bound The type reference to process
     * @param classTypeParamNames List of class type parameter names to replace in the bound
     * @return A cleaned string representation of the bound with fully qualified names
     */
    fun processBound(bound: KSTypeReference, classTypeParamNames: List<String> = emptyList()): String {
        val resolved = bound.resolve()
        val decl = resolved.declaration
        val qualifiedName = decl.qualifiedName?.asString()

        if (qualifiedName != null) {
            val arguments = resolved.arguments
            val typeString = if (arguments.isNotEmpty()) {
                val typeArgs = arguments.joinToString(", ") { arg ->
                    formatTypeArgument(arg, classTypeParamNames)
                }
                "$qualifiedName<$typeArgs>"
            } else {
                qualifiedName
            }

            val nullableSuffix = if (resolved.isMarkedNullable) "?" else ""
            val result = "$typeString$nullableSuffix"
            return replaceClassTypeParamReferences(result, classTypeParamNames)
        }

        val result = TypeExtractor.extractTypeString(bound, emptyList())
        return replaceClassTypeParamReferences(result, classTypeParamNames)
    }

    private fun formatTypeArgument(
        arg: com.google.devtools.ksp.symbol.KSTypeArgument,
        classTypeParamNames: List<String>,
    ): String = when {
        arg.type == null -> "*"
        arg.variance == com.google.devtools.ksp.symbol.Variance.STAR -> "*"
        arg.variance == com.google.devtools.ksp.symbol.Variance.CONTRAVARIANT -> {
            arg.type?.let {
                val typeStr = TypeExtractor.extractTypeString(it, emptyList())
                val replaced = replaceClassTypeParamReferences(typeStr, classTypeParamNames)
                "in $replaced"
            } ?: "*"
        }
        arg.variance == com.google.devtools.ksp.symbol.Variance.COVARIANT -> {
            arg.type?.let {
                val typeStr = TypeExtractor.extractTypeString(it, emptyList())
                val replaced = replaceClassTypeParamReferences(typeStr, classTypeParamNames)
                "out $replaced"
            } ?: "*"
        }
        else -> {
            arg.type?.let {
                val typeStr = TypeExtractor.extractTypeString(it, emptyList())
                replaceClassTypeParamReferences(typeStr, classTypeParamNames)
            } ?: "*"
        }
    }

    private fun replaceClassTypeParamReferences(boundStr: String, classTypeParamNames: List<String>): String {
        if (classTypeParamNames.isEmpty()) {
            return boundStr
        }
        var result = boundStr
        classTypeParamNames.forEach { paramName ->
            val pattern = Regex("""\b[\w.]+\.$paramName\b""")
            result = pattern.replace(result) { matchResult ->
                val matched = matchResult.value
                val prefix = matched.substringBefore(".$paramName")
                if (prefix.contains(".")) {
                    paramName
                } else {
                    matched
                }
            }
        }
        return result
    }
}
