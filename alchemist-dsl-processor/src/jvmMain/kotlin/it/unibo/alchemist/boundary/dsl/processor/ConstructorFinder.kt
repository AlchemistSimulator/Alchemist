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
     * Prefers the primary constructor, otherwise returns the constructor with the most parameters.
     *
     * @param classDecl The class declaration to find a constructor for
     * @return The found constructor, or null if no suitable constructor exists
     */
    fun findConstructor(classDecl: KSClassDeclaration): KSFunctionDeclaration? {
        val primaryConstructor = classDecl.primaryConstructor
        if (primaryConstructor != null && isPublicConstructor(primaryConstructor)) {
            return primaryConstructor
        }

        val constructors = classDecl.getAllFunctions()
            .filter { function ->
                val simpleName = function.simpleName.asString()
                (simpleName == "<init>" || simpleName == classDecl.simpleName.asString()) &&
                    isPublicConstructor(function)
            }
            .sortedByDescending { it.parameters.size }

        return constructors.firstOrNull()
    }

    private fun isPublicConstructor(function: KSFunctionDeclaration): Boolean {
        val modifiers = function.modifiers
        return !modifiers.contains(Modifier.PRIVATE) &&
            !modifiers.contains(Modifier.PROTECTED) &&
            !modifiers.contains(Modifier.INTERNAL)
    }
}
