package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Finds a suitable constructor for a class declaration.
 */
object ConstructorFinder {
    /**
     * Finds a public constructor for the given class declaration.
     * Prefers the primary constructor, otherwise returns the constructor
     * with the most parameters.
     *
     * @param classDeclaration The class declaration to find a constructor for
     * @return The found constructor, or null if no suitable constructor exists
     */
    fun findConstructor(classDeclaration: KSClassDeclaration): KSFunctionDeclaration? =
        classDeclaration.primaryConstructor?.takeIf { isPublicConstructor(it) }
            ?: classDeclaration.getAllFunctions()
                .filter { function ->
                    val simpleName = function.simpleName.asString()
                    (simpleName == "<init>" || simpleName == classDeclaration.simpleName.asString()) &&
                        isPublicConstructor(function)
                }
                .maxByOrNull { it.parameters.size }

    private fun isPublicConstructor(function: KSFunctionDeclaration): Boolean =
        function.modifiers.none { it == Modifier.PRIVATE || it == Modifier.PROTECTED || it == Modifier.INTERNAL }
}
